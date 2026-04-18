<template>
  <div>
    <h2 class="page-title">📦 {{ $t('versions.title') }}</h2>

    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreate">+ {{ $t('versions.btn_create') }}</button>
    </div>

    <div class="card">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>{{ $t('versions.table.code') }}</th>
              <th>{{ $t('versions.table.name') }}</th>
              <th>{{ $t('versions.table.title') }}</th>
              <th>{{ $t('versions.table.force') }}</th>
              <th>{{ $t('versions.table.status') }}</th>
              <th>{{ $t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in items" :key="v.id">
              <td><strong>{{ v.version_code }}</strong></td>
              <td>v{{ v.version_name }}</td>
              <td>{{ v.title }}</td>
              <td>
                <span :class="['badge', v.is_force_update ? 'badge-danger' : 'badge-success']">
                  {{ v.is_force_update ? $t('versions.table.force') : $t('common.status') }}
                </span>
              </td>
              <td>
                <span :class="['badge', v.is_published ? 'badge-success' : 'badge-warning']">
                  {{ v.is_published ? $t('versions.status.published') : $t('versions.status.draft') }}
                </span>
              </td>
              <td class="flex gap-8">
                <button class="btn btn-sm" :class="v.is_published ? 'btn-secondary' : 'btn-success'"
                  @click="togglePublish(v.id)">
                  {{ v.is_published ? $t('versions.status.unpublish') : $t('versions.status.publish') }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!items.length" class="empty-state"><p>{{ $t('versions.no_data') }}</p></div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3 class="modal-title">{{ $t('versions.modal.title') }}</h3>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('versions.modal.code') }}</label>
            <input v-model.number="form.version_code" type="number" class="input" placeholder="200" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('versions.modal.name') }}</label>
            <input v-model="form.version_name" class="input" placeholder="2.0.0" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('versions.modal.update_title') }}</label>
          <input v-model="form.title" class="input" :placeholder="$t('versions.modal.update_title')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('versions.modal.changelog') }}</label>
          <textarea v-model="form.changelog" class="textarea" :placeholder="$t('versions.modal.changelog')"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('versions.modal.url') }}</label>
          <input v-model="form.download_url" class="input" placeholder="https://..." />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('versions.modal.size') }}</label>
            <input v-model.number="form.file_size" type="number" class="input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('versions.modal.min_code') }}</label>
            <input v-model.number="form.min_version_code" type="number" class="input" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label"><input type="checkbox" v-model="form.is_force_update" /> {{ $t('versions.modal.force') }}</label>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="showModal=false">{{ $t('common.cancel') }}</button>
          <button class="btn btn-primary" @click="createVersion" :disabled="saving">{{ $t('common.save') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { versionApi } from '../api'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const showToast = inject('showToast')
const items = ref([])
const loading = ref(true)
const showModal = ref(false)
const saving = ref(false)
const form = ref({
  version_code: 0, version_name: '', title: '', changelog: '',
  download_url: '', file_size: 0, min_version_code: 0, is_force_update: false,
})

function openCreate() {
  form.value = { version_code: 0, version_name: '', title: '', changelog: '',
    download_url: '', file_size: 0, min_version_code: 0, is_force_update: false }
  showModal.value = true
}

async function loadItems() {
  loading.value = true
  try {
    const res = await versionApi.list({ page: 1, page_size: 50 })
    items.value = res.data
  } catch (e) {
    console.error(e)
    showToast(t('common.loading') + ' ' + t('common.failed'), 'error')
  }
  finally { loading.value = false }
}

async function createVersion() {
  saving.value = true
  try {
    await versionApi.create(form.value)
    showToast(t('common.active'))
    showModal.value = false; loadItems()
  } catch (e) { showToast(e?.detail || t('common.failed'), 'error') }
  finally { saving.value = false }
}

async function togglePublish(id) {
  try {
    await versionApi.togglePublish(id)
    showToast(t('common.active'))
    loadItems()
  } catch (e) { showToast(t('common.failed'), 'error') }
}

onMounted(loadItems)
</script>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin: 0; }
.toolbar { margin: 20px 0; display: flex; justify-content: flex-end; }
</style>
