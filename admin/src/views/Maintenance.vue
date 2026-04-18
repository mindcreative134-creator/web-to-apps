<template>
  <div class="view-page">
    <div class="page-header">
      <h1>{{ $t('maintenance.title') }}</h1>
      <p class="page-desc">{{ $t('maintenance.subtitle') }}</p>
    </div>

    <!-- System Health -->
    <div class="stats-row">
      <div class="stat-card" :class="health.status === 'healthy' ? 'success' : 'warning'">
        <div class="stat-value">{{ health.status || '...' }}</div>
        <div class="stat-label">{{ $t('maintenance.stats.health') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ health.uptime || '-' }}</div>
        <div class="stat-label">{{ $t('maintenance.stats.uptime') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ health.dbConnections ?? '-' }}</div>
        <div class="stat-label">{{ $t('maintenance.stats.db_conn') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ health.memoryUsage || '-' }}</div>
        <div class="stat-label">{{ $t('maintenance.stats.memory') }}</div>
      </div>
    </div>

    <!-- Maintenance Actions -->
    <div class="actions-grid">
      <div class="action-card">
        <div class="action-icon">🧹</div>
        <h3>{{ $t('maintenance.actions.cache_title') }}</h3>
        <p>{{ $t('maintenance.actions.cache_desc') }}</p>
        <button class="btn btn-primary" @click="clearCache" :disabled="running.cache">
          {{ running.cache ? $t('maintenance.actions.running') : $t('maintenance.actions.exec') }}
        </button>
      </div>

      <div class="action-card">
        <div class="action-icon">💾</div>
        <h3>{{ $t('maintenance.actions.backup_title') }}</h3>
        <p>{{ $t('maintenance.actions.backup_desc') }}</p>
        <button class="btn btn-primary" @click="dbBackup" :disabled="running.backup">
          {{ running.backup ? $t('maintenance.actions.running') : $t('maintenance.actions.backup_title') }}
        </button>
      </div>

      <div class="action-card">
        <div class="action-icon">🗑️</div>
        <h3>{{ $t('maintenance.actions.cleanup_title') }}</h3>
        <p>{{ $t('maintenance.actions.cleanup_desc') }}</p>
        <button class="btn btn-warning" @click="cleanup" :disabled="running.cleanup">
          {{ running.cleanup ? $t('maintenance.actions.running') : $t('maintenance.actions.exec') }}
        </button>
      </div>

      <div class="action-card">
        <div class="action-icon">🔄</div>
        <h3>{{ $t('maintenance.actions.health_title') }}</h3>
        <p>{{ $t('maintenance.actions.health_desc') }}</p>
        <button class="btn btn-ghost" @click="healthCheck" :disabled="running.health">
          {{ running.health ? $t('maintenance.actions.running') : $t('maintenance.actions.recheck') }}
        </button>
      </div>
    </div>

    <!-- Backup Management -->
    <div class="card mb-24">
      <div class="card-header-row">
        <h2 class="card-title">💾 {{ $t('maintenance.backups.title') }}</h2>
        <span v-if="backupData.total_size" class="text-xs text-muted">
          {{ $t('maintenance.backups.summary', { count: backupData.count, size: backupData.total_size }) }}
        </span>
      </div>

      <div v-if="loadingBackups" class="empty-hint">{{ $t('common.loading') }}</div>
      <div v-else-if="!backups.length" class="empty-hint">{{ $t('maintenance.backups.no_data') }}</div>
      <div v-else class="backup-list">
        <div class="backup-item" v-for="b in backups" :key="b.filename">
          <div class="backup-info">
            <span class="backup-name">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              {{ b.filename }}
            </span>
            <span class="backup-meta">{{ b.size }} · {{ formatDate(b.created_at) }}</span>
          </div>
          <div class="backup-actions">
            <button class="btn btn-ghost btn-xs" @click="downloadBackup(b.filename)" :title="$t('projects.details.ver.table.download')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              {{ $t('projects.details.ver.table.download') }}
            </button>
            <button class="btn btn-danger btn-xs" @click="deleteBackup(b.filename)" :title="$t('common.delete')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
              {{ $t('common.delete') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- System Stats -->
    <div class="card" v-if="systemStats">
      <h2 class="card-title">📊 {{ $t('maintenance.stats_title') }}</h2>
      <div class="stats-list">
        <div class="stats-item" v-for="(val, key) in systemStats" :key="key">
          <span class="stats-key">{{ key }}</span>
          <span class="stats-val">{{ val }}</span>
        </div>
      </div>
    </div>

    <!-- Data Export -->
    <div class="card mt-24" style="margin-top:24px">
      <h2 class="card-title">📥 {{ $t('maintenance.export.title') }}</h2>
      <div class="export-grid">
        <div class="action-card">
          <div class="action-icon">👤</div>
          <h3>{{ $t('maintenance.export.users_title') }}</h3>
          <p>{{ $t('maintenance.export.users_desc') }}</p>
          <button class="btn btn-primary" @click="exportUsers" :disabled="running.exportUsers">
            {{ running.exportUsers ? $t('maintenance.actions.running') : $t('maintenance.actions.exec') }}
          </button>
        </div>
        <div class="action-card">
          <div class="action-icon">📁</div>
          <h3>{{ $t('maintenance.export.projects_title') }}</h3>
          <p>{{ $t('maintenance.export.projects_desc') }}</p>
          <button class="btn btn-primary" @click="exportProjects" :disabled="running.exportProjects">
            {{ running.exportProjects ? $t('maintenance.actions.running') : $t('maintenance.actions.exec') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import { maintenanceApi } from '../api/index.js'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const showToast = inject('showToast')
const health = reactive({ status: '', uptime: '', dbConnections: 0, memoryUsage: '' })
const systemStats = ref(null)
const running = reactive({ cache: false, backup: false, cleanup: false, health: false, exportUsers: false, exportProjects: false })
const backups = ref([])
const backupData = reactive({ count: 0, total_size: '' })
const loadingBackups = ref(false)

async function healthCheck() {
  running.health = true
  try {
    const res = await maintenanceApi.healthCheck()
    const d = res?.data || {}
    health.status = d.status || 'unknown'
    health.uptime = d.uptime || '-'
    health.dbConnections = d.db_connections ?? '-'
    health.memoryUsage = d.memory_usage || '-'
  } catch {
    health.status = 'error'
    showToast(t('common.loading') + ' ' + t('common.failed'), 'error')
  } finally {
    running.health = false
  }
}

async function clearCache() {
  running.cache = true
  try {
    const res = await maintenanceApi.clearCache()
    showToast(t('common.active'))
  } catch {
    showToast(t('common.failed'), 'error')
  } finally {
    running.cache = false
  }
}

async function dbBackup() {
  running.backup = true
  try {
    const res = await maintenanceApi.dbBackup()
    if (res?.success === false) {
      showToast(res.message || t('common.failed'), 'error')
    } else {
      showToast(t('common.active'))
      loadBackups()
    }
  } catch {
    showToast(t('common.failed'), 'error')
  } finally {
    running.backup = false
  }
}

async function cleanup() {
  if (!confirm(t('maintenance.actions.confirm_cleanup'))) return
  running.cleanup = true
  try {
    const res = await maintenanceApi.cleanup()
    showToast(t('common.active'))
  } catch {
    showToast(t('common.failed'), 'error')
  } finally {
    running.cleanup = false
  }
}

async function loadStats() {
  try {
    const res = await maintenanceApi.systemStats()
    systemStats.value = res?.data || null
  } catch {
    systemStats.value = null
  }
}

async function loadBackups() {
  loadingBackups.value = true
  try {
    const res = await maintenanceApi.listBackups()
    const d = res?.data || {}
    backups.value = d.backups || []
    backupData.count = d.count || 0
    backupData.total_size = d.total_size || ''
  } catch {
    backups.value = []
  } finally {
    loadingBackups.value = false
  }
}

function downloadBackup(filename) {
  maintenanceApi.downloadBackup(filename)
}

async function deleteBackup(filename) {
  if (!confirm(t('maintenance.backups.confirm_delete', { name: filename }))) return
  try {
    await maintenanceApi.deleteBackup(filename)
    showToast(t('common.active'))
    loadBackups()
  } catch {
    showToast(t('common.failed'), 'error')
  }
}

async function exportUsers() {
  running.exportUsers = true
  try {
    const res = await maintenanceApi.exportUsers()
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url; a.download = 'users_export.csv'; a.click()
    URL.revokeObjectURL(url)
    showToast(t('common.active'))
  } catch {
    showToast(t('common.failed'), 'error')
  } finally {
    running.exportUsers = false
  }
}

async function exportProjects() {
  running.exportProjects = true
  try {
    const res = await maintenanceApi.exportProjects()
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url; a.download = 'projects_export.csv'; a.click()
    URL.revokeObjectURL(url)
    showToast(t('common.active'))
  } catch {
    showToast(t('common.failed'), 'error')
  } finally {
    running.exportProjects = false
  }
}

const dateLocaleMap = { en: 'en-US', zh: 'zh-CN', hi: 'hi-IN' }
function formatDate(d) { return d ? new Date(d).toLocaleString(dateLocaleMap[locale.value] || 'en-US') : '-' }

onMounted(() => {
  healthCheck()
  loadStats()
  loadBackups()
})
</script>

<style scoped>
.view-page { max-width: 1000px; }
.page-header { margin-bottom: 28px; }
.page-header h1 { font-size: 1.5rem; font-weight: 700; letter-spacing: -0.03em; }
.page-desc { color: var(--text-secondary); font-size: 0.87rem; margin-top: 4px; }
.mb-24 { margin-bottom: 24px; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 28px; }
.stat-card {
  padding: 20px; border-radius: var(--r-md);
  background: var(--bg-card); border: 1px solid var(--border);
}
.stat-card.success { border-left: 3px solid var(--green); }
.stat-card.warning { border-left: 3px solid var(--yellow); }
.stat-value { font-size: 1.3rem; font-weight: 700; text-transform: capitalize; }
.stat-label { font-size: 0.78rem; color: var(--text-secondary); margin-top: 4px; }

.actions-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 28px; }
.export-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.action-card {
  padding: 24px; border-radius: var(--r-md);
  background: var(--bg-card); border: 1px solid var(--border);
  transition: border-color var(--t-fast);
}
.action-card:hover { border-color: var(--accent); }
.action-icon { font-size: 1.5rem; margin-bottom: 10px; }
.action-card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 4px; }
.action-card p { font-size: 0.82rem; color: var(--text-secondary); margin-bottom: 14px; }

.btn { padding: 8px 16px; border: none; border-radius: var(--r-xs); font-family: var(--font); font-size: 0.85rem; cursor: pointer; transition: all var(--t-fast); display: inline-flex; align-items: center; gap: 6px; }
.btn-primary { background: var(--accent); color: #fff; }
.btn-primary:hover { filter: brightness(1.1); }
.btn-warning { background: var(--yellow, #fbbf24); color: #000; }
.btn-warning:hover { filter: brightness(1.1); }
.btn-ghost { background: transparent; border: 1px solid var(--border); color: var(--text-secondary); }
.btn-ghost:hover { border-color: var(--text-primary); color: var(--text-primary); }
.btn-danger { background: rgba(239,68,68,0.15); color: #f87171; border: 1px solid rgba(239,68,68,0.2); }
.btn-danger:hover { background: rgba(239,68,68,0.25); }
.btn-xs { padding: 4px 10px; font-size: 0.78rem; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.card {
  padding: 24px; border-radius: var(--r-md);
  background: var(--bg-card); border: 1px solid var(--border);
}
.card-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-title { font-size: 1.05rem; font-weight: 600; }

/* Backup list */
.backup-list { display: flex; flex-direction: column; gap: 2px; }
.backup-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 14px; border-radius: var(--r-xs);
  background: rgba(255,255,255,0.02);
  border: 1px solid transparent;
  transition: all 0.15s;
}
.backup-item:hover { background: rgba(255,255,255,0.04); border-color: var(--border); }
.backup-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; flex: 1; }
.backup-name { display: flex; align-items: center; gap: 8px; font-size: 0.87rem; font-weight: 500; color: var(--text-primary); }
.backup-name svg { opacity: 0.4; flex-shrink: 0; }
.backup-meta { font-size: 0.75rem; color: var(--text-muted); padding-left: 24px; }
.backup-actions { display: flex; gap: 8px; flex-shrink: 0; margin-left: 16px; }

.empty-hint { padding: 32px; text-align: center; color: var(--text-muted); font-size: 0.87rem; }

/* Stats */
.stats-list { display: flex; flex-direction: column; gap: 8px; }
.stats-item { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid var(--border); font-size: 0.87rem; }
.stats-key { color: var(--text-secondary); }
.stats-val { font-weight: 500; font-variant-numeric: tabular-nums; }

@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .actions-grid { grid-template-columns: 1fr; }
}
</style>
