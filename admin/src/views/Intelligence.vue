<template>
  <div class="view-page">
    <div class="page-header">
      <h1>{{ $t('intelligence.title') }}</h1>
      <p class="page-desc">{{ $t('intelligence.subtitle') }}</p>
    </div>

    <!-- Security Overview -->
    <div class="stats-row">
      <div class="stat-card" :class="dashboard.threat_level === 'low' ? 'success' : 'warning'">
        <div class="stat-value">{{ dashboard.threat_level || '...' }}</div>
        <div class="stat-label">{{ $t('intelligence.stats.threat_level') }}</div>
      </div>
      <div class="stat-card accent">
        <div class="stat-value">{{ dashboard.active_threats ?? 0 }}</div>
        <div class="stat-label">{{ $t('intelligence.stats.active_threats') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ dashboard.blocked_requests_24h ?? 0 }}</div>
        <div class="stat-label">{{ $t('intelligence.stats.blocked_24h') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ dashboard.circuit_breakers_open ?? 0 }}</div>
        <div class="stat-label">{{ $t('intelligence.stats.open_breakers') }}</div>
      </div>
    </div>

    <!-- Circuit Breakers Panel -->
    <div class="card">
      <h2 class="card-title">{{ $t('intelligence.breakers.title') }}</h2>
      <div v-if="circuitBreakers.length === 0" class="empty-state">
        <p>{{ $t('intelligence.breakers.no_data') }}</p>
      </div>
      <div v-for="cb in circuitBreakers" :key="cb.name" class="cb-row">
        <div class="cb-info">
          <span class="cb-name">{{ cb.name }}</span>
          <span class="cb-status" :class="cb.state">{{ cb.state }}</span>
        </div>
        <div class="cb-meta">
          <span>{{ $t('intelligence.breakers.failure_rate') }}: {{ cb.failure_rate ?? '-' }}%</span>
          <span>{{ $t('intelligence.breakers.req_count') }}: {{ cb.request_count ?? 0 }}</span>
        </div>
        <button v-if="cb.state === 'open'" class="btn btn-sm btn-ghost"
          @click="resetCB(cb.name)">{{ $t('intelligence.breakers.reset') }}</button>
      </div>
    </div>

    <!-- Recent Threats -->
    <div class="card">
      <h2 class="card-title">{{ $t('intelligence.threats.title') }}</h2>
      <div v-if="threats.length === 0" class="empty-state">
        <p>🎉 {{ $t('intelligence.threats.no_data') }}</p>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>{{ $t('intelligence.threats.table.ip') }}</th>
            <th>{{ $t('intelligence.threats.table.type') }}</th>
            <th>{{ $t('intelligence.threats.table.score') }}</th>
            <th>{{ $t('intelligence.threats.table.status') }}</th>
            <th>{{ $t('intelligence.threats.table.time') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in threats" :key="t.id">
            <td><code>{{ t.ip }}</code></td>
            <td>{{ t.type }}</td>
            <td>
              <span class="score-badge" :class="getScoreClass(t.score)">{{ t.score }}</span>
            </td>
            <td>
              <span class="threat-status" :class="t.status">{{ t.status }}</span>
            </td>
            <td class="text-muted">{{ formatDate(t.detected_at) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import { intelligenceApi } from '../api/index.js'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const showToast = inject('showToast')
const dashboard = reactive({ threat_level: '', active_threats: 0, blocked_requests_24h: 0, circuit_breakers_open: 0 })
const circuitBreakers = ref([])
const threats = ref([])

function getScoreClass(score) {
  if (score >= 80) return 'critical'
  if (score >= 50) return 'high'
  if (score >= 20) return 'medium'
  return 'low'
}

async function loadDashboard() {
  try {
    const res = await intelligenceApi.dashboard()
    const d = res.data || {}
    Object.assign(dashboard, d)
  } catch {
    dashboard.threat_level = 'unknown'
  }
}

async function loadCircuitBreakers() {
  try {
    const res = await intelligenceApi.circuitBreakers()
    circuitBreakers.value = res.data?.breakers || res.data || []
  } catch {
    circuitBreakers.value = []
  }
}

async function loadThreats() {
  try {
    const res = await intelligenceApi.threats({ page: 1, size: 20 })
    threats.value = res.data?.threats || res.data || []
  } catch {
    threats.value = []
  }
}

async function resetCB(name) {
  try {
    await intelligenceApi.resetCircuitBreaker(name)
    showToast(t('intelligence.breakers.reset_success', { name }))
    loadCircuitBreakers()
  } catch {
    showToast(t('common.failed'), 'error')
  }
}

const dateLocaleMap = { en: 'en-US', zh: 'zh-CN', hi: 'hi-IN' }
function formatDate(d) { return d ? new Date(d).toLocaleString(dateLocaleMap[locale.value] || 'en-US') : '-' }

onMounted(() => {
  loadDashboard()
  loadCircuitBreakers()
  loadThreats()
})
</script>

<style scoped>
.view-page { max-width: 1000px; }
.page-header { margin-bottom: 28px; }
.page-header h1 { font-size: 1.5rem; font-weight: 700; letter-spacing: -0.03em; }
.page-desc { color: var(--text-secondary); font-size: 0.87rem; margin-top: 4px; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 28px; }
.stat-card {
  padding: 20px; border-radius: var(--r-md);
  background: var(--bg-card); border: 1px solid var(--border);
}
.stat-card.success { border-left: 3px solid var(--green); }
.stat-card.warning { border-left: 3px solid var(--yellow); }
.stat-card.accent { border-left: 3px solid var(--accent); }
.stat-value { font-size: 1.3rem; font-weight: 700; text-transform: capitalize; }
.stat-label { font-size: 0.78rem; color: var(--text-secondary); margin-top: 4px; }

.card {
  padding: 24px; border-radius: var(--r-md);
  background: var(--bg-card); border: 1px solid var(--border); margin-bottom: 20px;
}
.card-title { font-size: 1.05rem; font-weight: 600; margin-bottom: 16px; }
.empty-state { text-align: center; padding: 40px 20px; color: var(--text-secondary); }

/* Circuit Breakers */
.cb-row {
  display: flex; align-items: center; gap: 16px; padding: 12px 0;
  border-bottom: 1px solid var(--border);
}
.cb-row:last-child { border-bottom: none; }
.cb-info { flex: 1; display: flex; align-items: center; gap: 10px; }
.cb-name { font-weight: 500; font-size: 0.9rem; }
.cb-status {
  font-size: 0.72rem; padding: 2px 8px; border-radius: 10px; font-weight: 600;
  text-transform: uppercase;
}
.cb-status.closed { background: var(--green-subtle, rgba(34,197,94,0.1)); color: var(--green); }
.cb-status.open { background: var(--red-subtle); color: var(--red); }
.cb-status.half_open, .cb-status.half-open { background: var(--yellow-subtle, rgba(251,191,36,0.1)); color: var(--yellow, #fbbf24); }
.cb-meta { font-size: 0.8rem; color: var(--text-muted); display: flex; gap: 16px; }

/* Threats table */
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { text-align: left; padding: 10px 12px; font-size: 0.85rem; border-bottom: 1px solid var(--border); }
.data-table th { color: var(--text-secondary); font-weight: 500; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.04em; }
.text-muted { color: var(--text-muted); }
code { font-family: 'SF Mono', monospace; font-size: 0.82rem; }

.score-badge {
  font-size: 0.75rem; padding: 2px 8px; border-radius: 10px; font-weight: 700;
}
.score-badge.critical { background: var(--red-subtle); color: var(--red); }
.score-badge.high { background: rgba(249,115,22,0.15); color: #f97316; }
.score-badge.medium { background: var(--yellow-subtle, rgba(251,191,36,0.1)); color: var(--yellow, #fbbf24); }
.score-badge.low { background: var(--green-subtle, rgba(34,197,94,0.1)); color: var(--green); }

.threat-status {
  font-size: 0.72rem; padding: 2px 8px; border-radius: 10px; font-weight: 600;
}
.threat-status.active { background: var(--red-subtle); color: var(--red); }
.threat-status.resolved { background: var(--green-subtle, rgba(34,197,94,0.1)); color: var(--green); }
.threat-status.monitoring { background: var(--yellow-subtle, rgba(251,191,36,0.1)); color: var(--yellow, #fbbf24); }

.btn { padding: 7px 14px; border: none; border-radius: var(--r-xs); font-family: var(--font); font-size: 0.82rem; cursor: pointer; transition: all var(--t-fast); }
.btn-sm { padding: 5px 10px; font-size: 0.78rem; }
.btn-ghost { background: transparent; border: 1px solid var(--border); color: var(--text-secondary); }
.btn-ghost:hover { border-color: var(--text-primary); color: var(--text-primary); }
</style>
