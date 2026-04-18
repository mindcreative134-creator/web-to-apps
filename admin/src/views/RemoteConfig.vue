<template>
  <div>
    <h2 class="page-title">⚙️ {{ $t('config.title') }}</h2>

    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreate">+ {{ $t('config.btn_add') }}</button>
    </div>

    <div class="card">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>{{ $t('config.table.key') }}</th>
              <th>{{ $t('config.table.value') }}</th>
              <th>{{ $t('config.table.type') }}</th>
              <th>{{ $t('config.table.audience') }}</th>
              <th>{{ $t('common.status') }}</th>
              <th>{{ $t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in items" :key="c.id">
              <td><code>{{ c.config_key }}</code></td>
              <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis">{{ c.config_value }}</td>
              <td><span class="badge badge-info">{{ c.value_type }}</span></td>
              <td><span class="badge badge-purple">{{ $t('announcements.audiences.' + (c.target_audience || 'all')) }}</span></td>
              <td>
                <span :class="['badge', c.is_active ? 'badge-success' : 'badge-danger']">
                  {{ c.is_active ? $t('common.active') : $t('common.inactive') }}
                </span>
              </td>
              <td class="flex gap-8">
                <button class="btn btn-secondary btn-sm" @click="openEdit(c)">{{ $t('common.edit') }}</button>
                <button class="btn btn-danger btn-sm" @click="removeItem(c.id)">{{ $t('common.delete') }}</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!items.length" class="empty-state"><p>{{ $t('common.no_data') }}</p></div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3 class="modal-title">{{ isEdit ? $t('config.modal.edit') : $t('config.modal.create') }}</h3>
        <div class="form-group">
          <label class="form-label">{{ $t('config.table.key') }}</label>
          <input v-model="form.config_key" class="input" :placeholder="$t('config.modal.key_placeholder')" :disabled="isEdit" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('config.table.value') }}</label>
          <textarea v-model="form.config_value" class="textarea" :placeholder="$t('config.modal.value_placeholder')"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('config.table.type') }}</label>
            <select v-model="form.value_type" class="select">
              <option value="string">String</option>
              <option value="number">Number</option>
              <option value="boolean">Boolean</option>
              <option value="json">JSON</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('config.table.audience') }}</label>
            <select v-model="form.target_audience" class="select">
              <option value="all">{{ $t('announcements.audiences.all') }}</option>
              <option value="free">{{ $t('announcements.audiences.free') }}</option>
              <option value="pro">{{ $t('announcements.audiences.pro') }}</option>
              <option value="ultra">{{ $t('announcements.audiences.ultra') }}</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('config.modal.desc') }}</label>
          <input v-model="form.description" class="input" :placeholder="$t('config.modal.desc_placeholder')" />
        </div>
        <div v-if="isEdit" class="form-group">
          <label class="form-label"><input type="checkbox" v-model="form.is_active" /> {{ $t('common.active') }}</label>
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
import { configApi } from '../api'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const showToast = inject('showToast')
const items = ref([])
const loading = ref(true)
const showModal = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)

const defaultForm = () => ({
  config_key: '', config_value: '', value_type: 'string',
  target_audience: 'all', description: '', is_active: true,
})
const form = ref(defaultForm())

function openCreate() { isEdit.value = false; form.value = defaultForm(); showModal.value = true }
function openEdit(c) {
  isEdit.value = true; editId.value = c.id
  form.value = { ...c }
  showModal.value = true
}

async function loadItems() {
  loading.value = true
  try {
    const res = await configApi.list()
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
    if (isEdit.value) await configApi.update(editId.value, form.value)
    else await configApi.create(form.value)
    showToast(t('common.active'))
    showModal.value = false; loadItems()
  } catch (e) { showToast(e?.detail || t('common.failed'), 'error') }
  finally { saving.value = false }
}

async function removeItem(id) {
  if (!confirm(t('common.delete') + '?')) return
  try { await configApi.remove(id); showToast(t('common.active')); loadItems() }
  catch (e) { showToast(t('common.failed'), 'error') }
}

onMounted(loadItems)
</script>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin: 0; }
.toolbar { margin: 20px 0; display: flex; justify-content: flex-end; }
code { background: var(--bg-sidebar); padding: 2px 6px; border-radius: 4px; font-family: monospace; color: var(--accent); }
</style>
