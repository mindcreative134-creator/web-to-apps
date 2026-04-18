<template>
  <div>
    <h2 class="page-title">🔑 {{ $t('activation.title') }}</h2>

    <!-- Generate section -->
    <div class="card mb-20">
      <div class="card-header">
        <span class="card-title">{{ $t('activation.generate.title') }}</span>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">{{ $t('activation.generate.tier') }}</label>
          <select v-model="genForm.tier" class="select">
            <option value="pro">Pro</option>
            <option value="ultra">Ultra</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('activation.generate.plan_type') }}</label>
          <select v-model="genForm.plan_type" class="select">
            <option :value="genForm.tier + '_monthly'">{{ $t('common.active') }} ({{ $t('activation.types.days_30') }})</option>
            <option :value="genForm.tier + '_yearly'">{{ $t('users.table.duration') }} ({{ $t('activation.types.days_365') }})</option>
            <option :value="genForm.tier + '_lifetime'">{{ $t('activation.types.lifetime') }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('activation.generate.count') }}</label>
          <input v-model.number="genForm.count" type="number" class="input" min="1" max="500" />
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">{{ $t('activation.generate.note') }}</label>
        <input v-model="genForm.batch_note" class="input" :placeholder="$t('activation.generate.note_placeholder')" />
      </div>
      <div class="flex items-center gap-8">
        <button class="btn btn-primary" @click="generateCodes" :disabled="generating">
          {{ generating ? $t('activation.generate.generating') : '🔑 ' + $t('activation.generate.btn_generate') }}
        </button>
      </div>

      <!-- Generated codes -->
      <div v-if="generatedCodes.length" class="generated-codes mt-20">
        <div class="flex items-center justify-between mb-12">
          <span class="text-success">✅ {{ $t('activation.generate.success', { count: generatedCodes.length }) }}</span>
          <button class="btn btn-secondary btn-sm" @click="copyCodes">📋 {{ $t('activation.generate.copy_all') }}</button>
        </div>
        <div class="codes-list">
          <code v-for="c in generatedCodes" :key="c">{{ c }}</code>
        </div>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-grid mb-20">
      <div class="stat-card purple">
        <div class="stat-label">{{ $t('activation.stats.total') }}</div>
        <div class="stat-value">{{ stats.total || 0 }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">{{ $t('activation.stats.unused') }}</div>
        <div class="stat-value">{{ stats.unused || 0 }}</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">{{ $t('activation.stats.used') }}</div>
        <div class="stat-value">{{ stats.used || 0 }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">{{ $t('activation.stats.usage_rate') }}</div>
        <div class="stat-value">{{ stats.usage_rate || 0 }}%</div>
      </div>
    </div>

    <!-- List -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">{{ $t('activation.list.title') }}</span>
        <button class="btn btn-secondary btn-sm" @click="exportCodes">📥 {{ $t('activation.list.export') }}</button>
      </div>

      <div class="toolbar">
        <select v-model="filter.status" class="select" style="max-width:140px" @change="page=1;loadCodes()">
          <option :value="null">{{ $t('users.all_statuses') }}</option>
          <option value="unused">{{ $t('activation.stats.unused') }}</option>
          <option value="used">{{ $t('activation.stats.used') }}</option>
          <option value="disabled">{{ $t('activation.list.disabled_msg') }}</option>
        </select>
        <select v-model="filter.plan_type" class="select" style="max-width:140px" @change="page=1;loadCodes()">
          <option :value="null">{{ $t('users.all_packages') }}</option>
          <option value="monthly">{{ $t('activation.types.monthly') }}</option>
          <option value="pro_monthly">{{ $t('activation.types.pro_monthly') }}</option>
          <option value="pro_yearly">{{ $t('activation.types.pro_yearly') }}</option>
          <option value="pro_lifetime">{{ $t('activation.types.pro_lifetime') }}</option>
          <option value="ultra_monthly">{{ $t('activation.types.ultra_monthly') }}</option>
          <option value="ultra_yearly">{{ $t('activation.types.ultra_yearly') }}</option>
          <option value="ultra_lifetime">{{ $t('activation.types.ultra_lifetime') }}</option>
          <option value="quarterly">{{ $t('activation.types.quarterly') }}</option>
          <option value="yearly">{{ $t('activation.types.yearly') }}</option>
          <option value="lifetime">{{ $t('activation.types.lifetime') }}</option>
        </select>
      </div>

      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>{{ $t('activation.table.code') }}</th><th>{{ $t('activation.table.type') }}</th><th>{{ $t('activation.table.days') }}</th><th>{{ $t('activation.table.status') }}</th><th>{{ $t('activation.table.user') }}</th><th>{{ $t('activation.table.batch') }}</th><th>{{ $t('activation.table.actions') }}</th></tr>
          </thead>
          <tbody>
            <tr v-for="c in codes" :key="c.id">
              <td><code style="font-size:12px">{{ c.code }}</code></td>
              <td>{{ c.plan_type }}</td>
              <td>{{ c.duration_days }}</td>
              <td>
                <span :class="['badge', statusBadge(c.status)]">{{ c.status }}</span>
              </td>
              <td>{{ c.used_by || '-' }}</td>
              <td class="text-muted" style="font-size:12px">{{ c.batch_id || '-' }}</td>
              <td>
                <button v-if="c.status === 'unused'" class="btn btn-danger btn-sm"
                  @click="disableCode(c.id)">{{ $t('common.delete') }}</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!codes.length" class="empty-state"><p>{{ $t('activation.list.no_codes') }}</p></div>
      </div>

      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page <= 1" @click="page--; loadCodes()">{{ $t('activation.list.prev') }}</button>
        <span>{{ page }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages" @click="page++; loadCodes()">{{ $t('activation.list.next') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { activationApi } from '../api'

const { t } = useI18n()
const showToast = inject('showToast')
const genForm = ref({ tier: 'pro', plan_type: 'pro_monthly', count: 10, batch_note: '' })

// Automatically update plan_type when membership tier changes
watch(() => genForm.value.tier, (newTier, oldTier) => {
  const current = genForm.value.plan_type
  if (current.endsWith('_lifetime')) {
    genForm.value.plan_type = newTier + '_lifetime'
    return
  }
  if (current === 'lifetime') return // Legacy lifetime doesn't need toggle
  // Replace old tier prefix with new tier prefix
  if (current.startsWith(oldTier + '_')) {
    genForm.value.plan_type = current.replace(oldTier + '_', newTier + '_')
  } else {
    genForm.value.plan_type = newTier + '_monthly'
  }
})
const generatedCodes = ref([])
const generating = ref(false)
const stats = ref({})
const codes = ref([])
const loading = ref(true)
const page = ref(1)
const totalPages = ref(1)
const filter = ref({ status: null, plan_type: null })

async function generateCodes() {
  generating.value = true
  try {
    const res = await activationApi.generate(genForm.value)
    generatedCodes.value = res.data.codes
    showToast(t('activation.generate.success', { count: res.data.count }))
    loadStats()
    loadCodes()
  } catch (e) { showToast(e?.detail || t('common.loading'), 'error') }
  finally { generating.value = false }
}

function copyCodes() {
  navigator.clipboard.writeText(generatedCodes.value.join('\n'))
  showToast(t('activation.generate.copied'))
}

async function loadStats() {
  try {
    const res = await activationApi.stats()
    stats.value = res.data
  } catch (e) { console.error(e) }
}

async function loadCodes() {
  loading.value = true
  try {
    const params = { page: page.value, page_size: 20 }
    if (filter.value.status) params.status = filter.value.status
    if (filter.value.plan_type) params.plan_type = filter.value.plan_type
    const res = await activationApi.list(params)
    codes.value = res.data || []
    totalPages.value = res.meta?.total_pages || 1
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function disableCode(id) {
  if (!confirm(t('activation.list.disable_confirm'))) return
  try {
    await activationApi.disable(id)
    showToast(t('activation.list.disabled_msg'))
    loadCodes()
    loadStats()
  } catch (e) { showToast(e?.detail || t('common.loading'), 'error') }
}

async function exportCodes() {
  try {
    const res = await activationApi.exportCsv({ status: filter.value.status || 'unused' })
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url; a.download = 'activation_codes.csv'; a.click()
    URL.revokeObjectURL(url)
  } catch (e) { showToast(t('common.failed'), 'error') }
}

function statusBadge(s) {
  return { unused: 'badge-success', used: 'badge-info', disabled: 'badge-danger', expired: 'badge-warning' }[s] || 'badge-info'
}

onMounted(() => { loadStats(); loadCodes() })
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; }
.codes-list { display: flex; flex-wrap: wrap; gap: 8px; max-height: 200px; overflow-y: auto; }
.codes-list code {
  padding: 6px 12px; background: var(--bg-input); border: 1px solid var(--border);
  border-radius: 6px; font-size: 13px; color: var(--accent);
}
@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2,1fr); } }
</style>
