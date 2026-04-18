<template>
  <div class="slide-up">
    <div class="page-header">
      <div>
        <h1>{{ $t('users.title') }}</h1>
        <p class="text-secondary text-sm">{{ $t('users.subtitle') }}</p>
      </div>
      <span class="badge badge-secondary">{{ $t('users.user_count', { count: total }) }}</span>
    </div>

    <!-- Filters -->
    <div class="filter-row">
      <div class="search-wrap">
        <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input v-model="search" class="input search-input" :placeholder="$t('users.search_placeholder')" @input="debouncedLoad" />
      </div>
      <select v-model="planFilter" class="select" style="width:150px" @change="loadUsers">
        <option value="">{{ $t('users.all_packages') }}</option>
        <option value="free">{{ $t('users.plans.free') }}</option>
        <option value="pro_monthly">{{ $t('users.plans.pro_monthly') }}</option>
        <option value="pro_yearly">{{ $t('users.plans.pro_yearly') }}</option>
        <option value="ultra_monthly">{{ $t('users.plans.ultra_monthly') }}</option>
        <option value="ultra_yearly">{{ $t('users.plans.ultra_yearly') }}</option>
        <option value="lifetime">{{ $t('users.plans.legacy_lifetime') }}</option>
      </select>
      <select v-model="statusFilter" class="select" style="width:110px" @change="loadUsers">
        <option value="">{{ $t('users.all_statuses') }}</option>
        <option value="active">{{ $t('common.active') }}</option>
        <option value="banned">{{ $t('users.banned') }}</option>
      </select>
    </div>

    <!-- Table -->
    <div class="card mt-16">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead><tr>
            <th>{{ $t('users.table.user') }}</th><th>{{ $t('users.table.password') }}</th><th>{{ $t('users.table.duration') }}</th><th>{{ $t('users.table.package') }}</th><th>{{ $t('users.table.expiry') }}</th><th>{{ $t('users.table.status') }}</th><th style="width:70px"></th>
          </tr></thead>
          <tbody>
            <tr v-for="u in users" :key="u.id" class="user-row">
              <td>
                <div class="user-cell">
                  <div class="avatar">{{ (u.username || u.email)[0].toUpperCase() }}</div>
                  <div>
                    <div class="user-name">{{ u.username }}</div>
                    <div class="text-xs text-muted">{{ u.email }}</div>
                    <div v-if="u.google_email && u.google_email !== u.email" class="text-xs" style="color: #4285F4;">
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor" style="vertical-align: middle; margin-right: 2px;"><path d="M12.545,10.239v3.821h5.445c-0.712,2.315-2.647,3.972-5.445,3.972c-3.332,0-6.033-2.701-6.033-6.032s2.701-6.032,6.033-6.032c1.498,0,2.866,0.549,3.921,1.453l2.814-2.814C17.503,2.988,15.139,2,12.545,2C7.021,2,2.543,6.477,2.543,12s4.478,10,10.002,10c8.396,0,10.249-7.85,9.426-11.748L12.545,10.239z"/></svg>
                      {{ u.google_email }}
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <div class="password-cell" v-if="u.password && u.password !== '—'">
                  <code v-if="u._showPw" class="pw-text">{{ u.password }}</code>
                  <span v-else class="pw-masked">••••••</span>
                  <button class="btn-icon-tiny" @click="togglePw(u)" :title="u._showPw ? $t('users.modal.hide_pw') : $t('users.modal.show_pw')">
                    <svg v-if="u._showPw" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                    <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  </button>
                </div>
                <span v-else class="text-xs text-muted" :title="$t('users.password_hint')">{{ $t('users.password_hint') }}</span>
              </td>
              <td class="text-sm">{{ fmtDuration(u.total_online_seconds) }}</td>
              <td><span :class="['badge', planColor(u.pro_plan)]">{{ planName(u.pro_plan) }}</span></td>
              <td class="text-sm">
                <span v-if="u.pro_expires_at">{{ fmtDate(u.pro_expires_at) }}</span>
                <span v-else-if="u.pro_plan === 'lifetime'" class="text-success">{{ $t('users.forever') }}</span>
                <span v-else class="text-muted">—</span>
              </td>
              <td><span :class="['badge', u.is_active ? 'badge-success' : 'badge-danger']">{{ u.is_active ? $t('common.active') : $t('users.banned') }}</span></td>
              <td><button class="btn btn-ghost btn-sm" @click="openDetail(u.id)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
              </button></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="totalPages > 1" class="pagination">
        <button class="btn btn-ghost btn-sm" :disabled="page <= 1" @click="page--; loadUsers()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
        </button>
        <span class="text-xs text-muted">{{ page }} / {{ totalPages }}</span>
        <button class="btn btn-ghost btn-sm" :disabled="page >= totalPages" @click="page++; loadUsers()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>
    </div>

    <!-- Detail Modal -->
    <div v-if="detailUser" class="modal-overlay" @click.self="detailUser = null">
      <div class="modal modal-lg">
        <div class="flex justify-between items-center" style="margin-bottom:20px">
          <h2>{{ detailUser.user.username }}</h2>
          <button class="btn-icon" @click="detailUser = null">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="detail-grid">
          <div class="detail-section">
            <h4 class="section-label">{{ $t('users.modal.basic_info') }}</h4>
            <div class="info-list">
              <div class="info-item"><span>{{ $t('users.modal.email') }}</span><span>{{ detailUser.user.email }}</span></div>
              <div v-if="detailUser.user.google_email" class="info-item">
                <span>{{ $t('users.modal.google_account') }}</span>
                <span style="color: #4285F4;">{{ detailUser.user.google_email }}</span>
              </div>
              <div class="info-item">
                <span>{{ $t('users.modal.password') }}</span>
                <span v-if="detailUser.user.password && detailUser.user.password !== '—'" class="password-cell">
                  <code v-if="showDetailPw" class="pw-text">{{ detailUser.user.password }}</code>
                  <span v-else class="pw-masked">••••••</span>
                  <button class="btn-icon-tiny" @click="showDetailPw = !showDetailPw">
                    <svg v-if="showDetailPw" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                    <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  </button>
                </span>
                <span v-else class="text-xs text-muted">{{ $t('users.password_hint') }}</span>
              </div>
              <div class="info-item"><span>{{ $t('users.modal.registered') }}</span><span>{{ fmtDate(detailUser.user.created_at) }}</span></div>
              <div class="info-item"><span>{{ $t('users.modal.last_login') }}</span><span>{{ fmtDate(detailUser.user.last_login_at) || '—' }}</span></div>
              <div class="info-item"><span>{{ $t('users.modal.login_count') }}</span><span>{{ detailUser.user.login_count }}</span></div>
              <div class="info-item">
                <span>{{ $t('users.modal.online_duration') }}</span>
                <span class="online-duration">{{ fmtDuration(detailUser.user.total_online_seconds) }}</span>
              </div>
              <div class="info-item"><span>{{ $t('users.modal.apps_created') }}</span><span>{{ detailUser.user.apps_created }}</span></div>
              <div class="info-item"><span>{{ $t('users.modal.apks_built') }}</span><span>{{ detailUser.user.apks_built }}</span></div>
            </div>
          </div>
          <div class="detail-section">
            <h4 class="section-label">{{ $t('users.modal.subscription') }}</h4>
            <div class="form-group">
              <label class="form-label">{{ $t('users.modal.plan') }}</label>
              <select v-model="editForm.pro_plan" class="select">
                <option value="free">{{ $t('users.plans.free') }}</option>
                <option value="pro_monthly">{{ $t('users.plans.pro_monthly') }} $3</option>
                <option value="pro_yearly">{{ $t('users.plans.pro_yearly') }} $28.80</option>
                <option value="pro_lifetime">{{ $t('users.plans.pro_lifetime') }} $99</option>
                <option value="ultra_monthly">{{ $t('users.plans.ultra_monthly') }} $9</option>
                <option value="ultra_yearly">{{ $t('users.plans.ultra_yearly') }} $86.40</option>
                <option value="ultra_lifetime">{{ $t('users.plans.ultra_lifetime') }} $199</option>
                <option value="lifetime">{{ $t('users.plans.legacy_lifetime') }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">{{ $t('users.modal.expiry') }}</label>
              <input v-model="editForm.pro_expires_at" type="datetime-local" class="input" />
            </div>
            <div class="flex gap-12">
              <div class="form-group" style="flex:1">
                <label class="form-label">{{ $t('users.modal.max_devices') }}</label>
                <input v-model.number="editForm.max_devices" type="number" class="input" min="1" />
              </div>
              <div class="form-group" style="flex:1">
                <label class="form-label">{{ $t('users.modal.project_limit') }}</label>
                <input v-model.number="editForm.custom_project_limit" type="number" class="input" min="0" />
              </div>
            </div>
            <div class="flex gap-16 mt-8">
              <label class="toggle-label"><input type="checkbox" v-model="editForm.is_active" class="toggle" /> {{ $t('users.modal.account_status') }}</label>
            </div>
            <button class="btn btn-primary btn-sm mt-16" @click="saveUser" :disabled="saving" style="width:100%">{{ $t('users.modal.save_changes') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, reactive, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { adminApi } from '../api'
import api from '../api'

const { t, locale } = useI18n()
const showToast = inject('showToast')
const users = ref([]), loading = ref(false), search = ref('')
const planFilter = ref(''), statusFilter = ref('')
const page = ref(1), total = ref(0), totalPages = ref(1)
const detailUser = ref(null), editForm = ref({}), saving = ref(false)
const showDetailPw = ref(false)

let timer
function debouncedLoad() { clearTimeout(timer); timer = setTimeout(() => { page.value = 1; loadUsers() }, 400) }

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminApi.listUsers({ page: page.value, page_size: 20, search: search.value || undefined, plan_filter: planFilter.value || undefined, status_filter: statusFilter.value || undefined })
    // Add _showPw reactive property to each user
    users.value = res.data.map(u => reactive({ ...u, _showPw: false }))
    total.value = res.total; totalPages.value = res.total_pages
  } catch {} finally { loading.value = false }
}

async function openDetail(id) {
  showDetailPw.value = false
  try {
    const res = await adminApi.getUser(id)
    detailUser.value = res.data
    editForm.value = {
      is_pro: res.data.user.is_pro, pro_plan: res.data.user.pro_plan || 'free',
      pro_expires_at: res.data.user.pro_expires_at?.slice(0, 16) || '',
      max_devices: res.data.user.max_devices, custom_project_limit: res.data.user.custom_project_limit || 0,
      is_active: res.data.user.is_active,
    }
  } catch { showToast(t('common.failed'), 'error') }
}

async function saveUser() {
  saving.value = true
  try {
    const payload = {
      is_pro: editForm.value.is_pro,
      pro_plan: editForm.value.pro_plan,
      is_active: editForm.value.is_active,
      max_devices: editForm.value.max_devices,
      custom_project_limit: editForm.value.custom_project_limit || 0,
    }
    if (editForm.value.pro_expires_at) {
      payload.pro_expires_at = editForm.value.pro_expires_at
    }
    await adminApi.updateUser(detailUser.value.user.id, payload)
    showToast(t('common.save_success')); loadUsers(); openDetail(detailUser.value.user.id)
  } catch { showToast(t('common.save_failed'), 'error') }
  finally { saving.value = false }
}

function togglePw(u) { u._showPw = !u._showPw }

function planName(p) { 
  const plans = { 
    free: 'Free',
    pro_monthly: t('users.plans.pro_monthly'),
    pro_yearly: t('users.plans.pro_yearly'),
    pro_lifetime: t('users.plans.pro_lifetime'),
    ultra_monthly: t('users.plans.ultra_monthly'),
    ultra_yearly: t('users.plans.ultra_yearly'),
    ultra_lifetime: t('users.plans.ultra_lifetime'),
    lifetime: t('users.plans.legacy_lifetime')
  }
  return plans[p] || p 
}
function planColor(p) { if (p?.startsWith('ultra')) return 'badge-warning'; if (p?.startsWith('pro') || p === 'lifetime') return 'badge-info'; return 'badge-secondary' }
function pLimit(u) { if (u.custom_project_limit) return u.custom_project_limit; if (u.pro_plan?.startsWith('ultra')) return 50; if (u.pro_plan?.startsWith('pro') || u.pro_plan === 'lifetime') return 10; return 0 }


const membershipLabel = computed(() => {
  const p = editForm.value.pro_plan
  if (p?.startsWith('ultra')) return 'Ultra'
  if (p?.startsWith('pro') || p === 'lifetime') return 'Pro'
  return ''
})
// 选择非 free 套餐时自动勾选会员标识
watch(() => editForm.value.pro_plan, (plan) => {
  if (plan && plan !== 'free') editForm.value.is_pro = true
  else editForm.value.is_pro = false
})
function fmtDate(d) { 
  if (!d) return ''
  const loc = locale.value === 'zh' ? 'zh-CN' : (locale.value === 'hi' ? 'hi-IN' : 'en-US')
  return new Date(d).toLocaleString(loc, { month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' }) 
}

function fmtDuration(seconds) {
  if (!seconds || seconds <= 0) return '—'
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (d > 0) return `${d}${t('users.duration.days')} ${h}${t('users.duration.hours')}`
  if (h > 0) return `${h}${t('users.duration.hours')} ${m}${t('users.duration.minutes')}`
  return `${m}${t('users.duration.minutes')}`
}

onMounted(loadUsers)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.filter-row { display: flex; gap: 10px; flex-wrap: wrap; }
.search-wrap { position: relative; flex: 1; min-width: 200px; max-width: 320px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--text-muted); pointer-events: none; }
.search-input { padding-left: 36px; }

.user-row { transition: background var(--t-fast); }
.user-cell { display: flex; align-items: center; gap: 10px; }
.avatar {
  width: 32px; height: 32px; min-width: 32px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.75rem; font-weight: 600;
  background: linear-gradient(135deg, rgba(99,102,241,0.2), rgba(167,139,250,0.2));
  color: var(--accent-hover);
}
.user-name { font-weight: 500; font-size: 0.87rem; color: var(--text-primary); }

.password-cell { display: flex; align-items: center; gap: 4px; }
.pw-text { font-size: 0.8rem; padding: 2px 6px; background: var(--bg-input); border-radius: 4px; color: var(--text-primary); font-family: 'SF Mono', 'Fira Code', monospace; }
.pw-masked { font-size: 0.8rem; color: var(--text-muted); letter-spacing: 2px; }
.btn-icon-tiny {
  background: none; border: none; cursor: pointer; padding: 2px;
  color: var(--text-muted); opacity: 0.6; transition: opacity 0.2s;
  display: flex; align-items: center;
}
.btn-icon-tiny:hover { opacity: 1; }

.online-duration { font-weight: 500; color: var(--accent); }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.detail-section { background: var(--bg-input); border-radius: var(--r-md); padding: 18px; }
.section-label { font-size: 0.8rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 14px; }
.info-list { display: flex; flex-direction: column; gap: 0; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; font-size: 0.85rem; border-bottom: 1px solid var(--border-light); }
.info-item span:first-child { color: var(--text-muted); }
.info-item:last-child { border-bottom: none; }
.toggle-label { display: flex; align-items: center; gap: 6px; font-size: 0.85rem; color: var(--text-secondary); cursor: pointer; }
.toggle { accent-color: var(--accent); }
</style>
