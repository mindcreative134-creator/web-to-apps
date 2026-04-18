<template>
  <div class="view-page">
    <div class="page-header">
      <div class="header-content">
        <h1>{{ $t('credentials.title') }}</h1>
        <p class="page-desc">{{ $t('credentials.subtitle') }}</p>
      </div>
      <button class="btn btn-primary" @click="openCreateModal">+ {{ $t('credentials.btn_new') }}</button>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-state">
        <span class="spinner"></span>
        {{ $t('common.loading') }}
      </div>
      <div v-else-if="!items.length" class="empty-state">
        <div class="empty-icon">🛡️</div>
        <p>{{ $t('common.no_data') }}</p>
      </div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>{{ $t('credentials.table.name') }}</th>
              <th>{{ $t('credentials.table.secret') }}</th>
              <th>{{ $t('credentials.table.label') }}</th>
              <th>{{ $t('common.created_at') }}</th>
              <th>{{ $t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in items" :key="item.id">
              <td><code>{{ item.username }}</code></td>
              <td class="password-cell">
                <span v-if="visiblePasswords[item.id]">{{ item.password }}</span>
                <span v-else>••••••••••••••••</span>
                <button class="btn-icon-sm" @click="togglePassword(item.id)">
                  <svg v-if="visiblePasswords[item.id]" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                  <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                </button>
                <button class="btn-icon-sm" @click="copyToClipboard(item.password)" :title="$t('common.active')">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>
                </button>
              </td>
              <td>{{ item.label }}</td>
              <td>{{ formatDate(item.created_at) }}</td>
              <td>
                <button class="btn-text btn-red" @click="deleteItem(item.id)">{{ $t('common.delete') }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal slide-up">
        <div class="modal-header">
          <h3>{{ $t('credentials.btn_new') }}</h3>
          <button class="btn-icon" @click="showModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="modal-desc">{{ $t('credentials.modal.hint') }}</p>
          
          <div class="form-group">
            <label class="form-label">{{ $t('credentials.table.name') }}</label>
            <input v-model="form.username" class="input" :placeholder="$t('credentials.table.name')" />
          </div>
          
          <div class="form-group">
            <label class="form-label">{{ $t('credentials.table.label') }}</label>
            <input v-model="form.label" class="input" :placeholder="$t('credentials.table.label')" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showModal = false">{{ $t('common.cancel') }}</button>
          <button class="btn btn-primary" @click="handleCreate" :disabled="saving">
            {{ saving ? $t('common.loading') : $t('credentials.btn_new') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import { credentialApi } from '../api'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const showToast = inject('showToast')
const items = ref([])
const loading = ref(false)
const showModal = ref(false)
const saving = ref(false)
const visiblePasswords = reactive({})

const form = reactive({
  username: '',
  label: ''
})

async function loadItems() {
  loading.value = true
  try {
    const res = await credentialApi.list()
    items.value = res.data || res
  } catch (e) {
    showToast(t('common.loading') + ' ' + t('common.failed'), 'error')
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  form.username = ''
  form.label = ''
  showModal.value = true
}

async function handleCreate() {
  saving.value = true
  try {
    await credentialApi.create(form)
    showToast(t('common.active'))
    showModal.value = false
    loadItems()
  } catch (e) {
    showToast(e?.detail || t('common.failed'), 'error')
  } finally {
    saving.value = false
  }
}

async function deleteItem(id) {
  if (!confirm(t('common.delete') + '?')) return
  try {
    await credentialApi.remove(id)
    showToast(t('common.active'))
    loadItems()
  } catch (e) {
    showToast(t('common.failed'), 'error')
  }
}

function togglePassword(id) {
  visiblePasswords[id] = !visiblePasswords[id]
}

function copyToClipboard(text) {
  navigator.clipboard.writeText(text)
  showToast(t('common.active'))
}

const dateLocaleMap = { en: 'en-US', zh: 'zh-CN', hi: 'hi-IN' }
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString(dateLocaleMap[locale.value] || 'en-US')
}

onMounted(loadItems)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px; }
.page-desc { color: var(--text-secondary); font-size: 0.87rem; margin-top: 4px; }

.modal-desc { margin-bottom: 20px; font-size: 0.87rem; color: var(--text-secondary); line-height: 1.5; }
code { background: var(--bg-sidebar); padding: 2px 6px; border-radius: 4px; font-family: monospace; color: var(--accent); }

.password-cell { display: flex; align-items: center; gap: 8px; font-family: monospace; }
.btn-icon-sm { 
  background: none; border: none; padding: 4px; cursor: pointer; color: var(--text-muted); 
  display: flex; align-items: center; border-radius: 4px; transition: all 0.2s;
}
.btn-icon-sm:hover { background: rgba(255,255,255,0.05); color: var(--text-primary); }

.btn-red { color: var(--red); }
.btn-red:hover { color: #f87171; }

.loading-state, .empty-state { padding: 60px; text-align: center; color: var(--text-muted); }
.empty-icon { font-size: 3rem; margin-bottom: 16px; opacity: 0.2; }
</style>
