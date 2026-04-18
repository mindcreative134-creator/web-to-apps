<template>
  <div>
    <h2 class="page-title">📢 {{ $t('announcements.title') }}</h2>

    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreate">+ {{ $t('announcements.btn_create') }}</button>
    </div>

    <div class="card">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>{{ $t('announcements.table.title') }}</th>
              <th>{{ $t('announcements.table.type') }}</th>
              <th>{{ $t('announcements.table.audience') }}</th>
              <th>{{ $t('announcements.table.status') }}</th>
              <th>{{ $t('announcements.table.start') }}</th>
              <th>{{ $t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in items" :key="a.id">
              <td><strong>{{ a.title }}</strong></td>
              <td><span class="badge badge-info">{{ $t('projects.details.ann.form.types.' + (a.display_type || 'popup')) }}</span></td>
              <td><span class="badge badge-purple">{{ $t('announcements.audiences.' + (a.target_audience || 'all')) }}</span></td>
              <td>
                <span :class="['badge', a.is_active ? 'badge-success' : 'badge-danger']">
                  {{ a.is_active ? $t('common.active') : $t('common.inactive') }}
                </span>
              </td>
              <td>{{ formatDate(a.start_at) }}</td>
              <td class="flex gap-8">
                <button class="btn btn-secondary btn-sm" @click="openEdit(a)">{{ $t('common.edit') }}</button>
                <button class="btn btn-danger btn-sm" @click="removeItem(a.id)">{{ $t('common.delete') }}</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!items.length" class="empty-state"><p>{{ $t('announcements.no_data') }}</p></div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3 class="modal-title">{{ isEdit ? $t('announcements.modal.edit_title') : $t('announcements.modal.create_title') }}</h3>
        <div class="form-group">
          <label class="form-label">{{ $t('announcements.modal.title') }}</label>
          <input v-model="form.title" class="input" :placeholder="$t('announcements.modal.title')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('announcements.modal.content') }}</label>
          <textarea v-model="form.content" class="textarea" :placeholder="$t('announcements.modal.content')"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('announcements.modal.content_en') }}</label>
          <textarea v-model="form.content_en" class="textarea" placeholder="English content"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('announcements.modal.display_type') }}</label>
            <select v-model="form.display_type" class="select">
              <option value="popup">{{ $t('projects.details.ann.form.types.popup') }}</option>
              <option value="banner">{{ $t('projects.details.ann.form.types.banner') }}</option>
              <option value="fullscreen">{{ $t('projects.details.ann.form.types.fullscreen') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('announcements.modal.audience') }}</label>
            <select v-model="form.target_audience" class="select">
              <option value="all">{{ $t('announcements.audiences.all') }}</option>
              <option value="free">{{ $t('announcements.audiences.free') }}</option>
              <option value="pro">{{ $t('announcements.audiences.pro') }}</option>
              <option value="ultra">{{ $t('announcements.audiences.ultra') }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('announcements.modal.start') }}</label>
            <input v-model="form.start_at" type="datetime-local" class="input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('announcements.modal.end') }}</label>
            <input v-model="form.end_at" type="datetime-local" class="input" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('announcements.modal.action_url') }}</label>
          <input v-model="form.action_url" class="input" :placeholder="$t('announcements.modal.action_url')" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label"><input type="checkbox" v-model="form.is_active" /> {{ $t('announcements.modal.activate') }}</label>
          </div>
          <div class="form-group">
            <label class="form-label"><input type="checkbox" v-model="form.dismissible" /> {{ $t('announcements.modal.dismissible') }}</label>
          </div>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="showModal=false">{{ $t('common.cancel') }}</button>
          <button class="btn btn-primary" @click="saveItem" :disabled="saving">{{ $t('common.save') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { announcementApi } from '../api'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const showToast = inject('showToast')
const items = ref([])
const loading = ref(true)
const showModal = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)

const defaultForm = () => ({
  title: '', content: '', content_en: '', display_type: 'popup',
  target_audience: 'all', start_at: new Date().toISOString().slice(0,16),
  end_at: '', action_url: '', is_active: true, dismissible: true,
})
const form = ref(defaultForm())

function openCreate() { isEdit.value = false; form.value = defaultForm(); showModal.value = true }
function openEdit(a) {
  isEdit.value = true; editId.value = a.id
  form.value = { ...a, start_at: a.start_at?.slice(0,16) || '', end_at: a.end_at?.slice(0,16) || '' }
  showModal.value = true
}

async function loadItems() {
  loading.value = true
  try {
    const res = await announcementApi.list({ page: 1, page_size: 50 })
    items.value = res.data
  } catch (e) {
    console.error(e)
    showToast(t('common.loading') + ' ' + t('common.failed'), 'error')
  }
  finally { loading.value = false }
}

async function saveItem() {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (!payload.end_at) delete payload.end_at
    if (isEdit.value) await announcementApi.update(editId.value, payload)
    else await announcementApi.create(payload)
    showToast(t('common.active'))
    showModal.value = false; loadItems()
  } catch (e) { showToast(e?.detail || t('common.failed'), 'error') }
  finally { saving.value = false }
}

async function removeItem(id) {
  if (!confirm(t('common.delete') + '?')) return
  try { await announcementApi.remove(id); showToast(t('common.active')); loadItems() }
  catch (e) { showToast(t('common.failed'), 'error') }
}

const dateLocaleMap = { en: 'en-US', zh: 'zh-CN', hi: 'hi-IN' }
function formatDate(d) { return d ? new Date(d).toLocaleString(dateLocaleMap[locale.value] || 'en-US') : '-' }

onMounted(loadItems)
</script>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin: 0; }
.toolbar { margin: 20px 0; display: flex; justify-content: flex-end; }
</style>
