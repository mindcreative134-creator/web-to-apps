<template>
  <div class="slide-up">
    <div class="page-header">
      <div>
        <h1>商店管理</h1>
        <p class="text-secondary text-sm">管理应用商店和模块商店中的所有项目</p>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card" v-for="(s, i) in statCards" :key="i" :style="{ animationDelay: i * 50 + 'ms' }">
        <div class="stat-icon" :style="{ background: s.bg }">
          <span v-html="s.svg"></span>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ s.value }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-row mt-20">
      <div class="search-wrap">
        <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input v-model="search" class="input search-input" placeholder="搜索名称、描述或标签" @input="debouncedLoad" />
      </div>
      <select v-model="typeFilter" class="select" style="width:140px" @change="page=1;loadItems()">
        <option value="">所有类型</option>
        <option value="app">应用</option>
        <option value="extension">扩展模块</option>
        <option value="browser_extension">浏览器扩展</option>
      </select>
      <select v-model="statusFilter" class="select" style="width:120px" @change="page=1;loadItems()">
        <option value="">全部状态</option>
        <option value="approved">已通过</option>
        <option value="pending">待审核</option>
        <option value="featured">推荐</option>
      </select>
      <select v-model="sortField" class="select" style="width:120px" @change="loadItems()">
        <option value="created_at">创建时间</option>
        <option value="downloads">下载量</option>
        <option value="rating">评分</option>
        <option value="like_count">点赞数</option>
        <option value="view_count">浏览量</option>
      </select>

      <!-- Batch Actions -->
      <div v-if="selectedIds.length" class="batch-actions">
        <span class="batch-count">已选 {{ selectedIds.length }} 项</span>
        <button class="btn btn-sm btn-primary" @click="batchAction('approve')">批准</button>
        <button class="btn btn-sm btn-secondary" @click="batchAction('feature')">推荐</button>
        <button class="btn btn-sm btn-danger" @click="batchAction('delete')">删除</button>
      </div>
    </div>

    <!-- Table -->
    <div class="card mt-16">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead><tr>
            <th style="width:36px"><input type="checkbox" :checked="allSelected" @change="toggleAll" class="check-all" /></th>
            <th>项目</th>
            <th>类型</th>
            <th>分类</th>
            <th>数据</th>
            <th>状态</th>
            <th>作者</th>
            <th>创建时间</th>
            <th style="width:100px"></th>
          </tr></thead>
          <tbody>
            <tr v-for="item in items" :key="item.id" class="item-row">
              <td><input type="checkbox" :value="item.id" v-model="selectedIds" class="row-check" /></td>
              <td>
                <div class="item-cell">
                  <div class="item-icon" :style="{ background: typeGradient(item.module_type) }">
                    {{ item.icon || item.name[0]?.toUpperCase() }}
                  </div>
                  <div class="item-info">
                    <div class="item-name">{{ item.name }}</div>
                    <div class="text-xs text-muted">{{ item.package_name || `v${item.version_name || '1.0'}` }}</div>
                  </div>
                </div>
              </td>
              <td><span :class="['badge', typeBadge(item.module_type)]">{{ typeLabel(item.module_type) }}</span></td>
              <td class="text-sm">{{ item.category || '—' }}</td>
              <td>
                <div class="metrics-mini">
                  <span title="下载">⬇ {{ fmtNum(item.downloads) }}</span>
                  <span title="浏览">👁 {{ fmtNum(item.view_count) }}</span>
                  <span title="评分">⭐ {{ (item.rating || 0).toFixed(1) }}</span>
                </div>
              </td>
              <td>
                <div class="status-tags">
                  <span v-if="item.is_featured" class="badge badge-warning">推荐</span>
                  <span :class="['badge', item.is_approved ? 'badge-success' : 'badge-danger']">
                    {{ item.is_approved ? '已通过' : '待审核' }}
                  </span>
                </div>
              </td>
              <td class="text-sm">{{ item.author_name }}</td>
              <td class="text-sm text-muted">{{ fmtDate(item.created_at) }}</td>
              <td>
                <div class="action-btns">
                  <button class="btn btn-ghost btn-sm" @click="toggleFeatured(item)" :title="item.is_featured ? '取消推荐' : '设为推荐'">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" :stroke="item.is_featured ? '#fbbf24' : 'currentColor'" stroke-width="2">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                  </button>
                  <button class="btn btn-ghost btn-sm" @click="openDetail(item)" title="查看详情">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  </button>
                  <button class="btn btn-ghost btn-sm" @click="deleteItem(item)" title="删除">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!items.length" class="empty-state"><p>暂无项目</p></div>
      </div>
      <div v-if="totalPages > 1" class="pagination">
        <button class="btn btn-ghost btn-sm" :disabled="page <= 1" @click="page--; loadItems()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
        </button>
        <span class="text-xs text-muted">{{ page }} / {{ totalPages }}</span>
        <button class="btn btn-ghost btn-sm" :disabled="page >= totalPages" @click="page++; loadItems()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>
    </div>

    <!-- Detail / Edit Modal -->
    <div v-if="detailItem" class="modal-overlay" @click.self="detailItem = null">
      <div class="modal modal-lg">
        <div class="flex justify-between items-center" style="margin-bottom:20px">
          <div>
            <h2 class="modal-title">{{ detailItem.name }}</h2>
            <span :class="['badge', typeBadge(detailItem.module_type)]" style="margin-top:6px">{{ typeLabel(detailItem.module_type) }}</span>
          </div>
          <button class="btn-icon" @click="detailItem = null">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="detail-grid">
          <!-- Left: Info -->
          <div class="detail-section">
            <h4 class="section-label">项目信息</h4>
            <div class="info-list">
              <div class="info-item"><span>ID</span><span># {{ detailItem.id }}</span></div>
              <div class="info-item"><span>作者</span><span>{{ detailItem.author_name }} (#{{ detailItem.author_id }})</span></div>
              <div class="info-item"><span>分类</span><span>{{ detailItem.category || '—' }}</span></div>
              <div class="info-item"><span>版本</span><span>{{ detailItem.version_name || '—' }} ({{ detailItem.version_code }})</span></div>
              <div class="info-item"><span>包名</span><span>{{ detailItem.package_name || '—' }}</span></div>
              <div class="info-item"><span>标签</span><span>{{ detailItem.tags || '—' }}</span></div>
              <div class="info-item"><span>下载量</span><span class="text-success">{{ detailItem.downloads }}</span></div>
              <div class="info-item"><span>浏览量</span><span>{{ detailItem.view_count }}</span></div>
              <div class="info-item"><span>点赞</span><span>{{ detailItem.like_count }}</span></div>
              <div class="info-item"><span>评分</span><span>⭐ {{ (detailItem.rating || 0).toFixed(2) }} ({{ detailItem.rating_count }} 评价)</span></div>
              <div class="info-item"><span>评论数</span><span>{{ detailItem.comment_count }}</span></div>
              <div class="info-item"><span>创建时间</span><span>{{ fmtDate(detailItem.created_at) }}</span></div>
              <div class="info-item"><span>更新时间</span><span>{{ fmtDate(detailItem.updated_at) }}</span></div>
            </div>
          </div>

          <!-- Right: Edit -->
          <div class="detail-section">
            <h4 class="section-label">管理操作</h4>
            <div class="form-group">
              <label class="form-label">名称</label>
              <input v-model="editForm.name" class="input" />
            </div>
            <div class="form-group">
              <label class="form-label">分类</label>
              <select v-model="editForm.category" class="select">
                <option value="">未分类</option>
                <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">标签</label>
              <input v-model="editForm.tags" class="input" placeholder="逗号分隔" />
            </div>
            <div class="form-group">
              <label class="form-label">版本名</label>
              <input v-model="editForm.version_name" class="input" />
            </div>
            <div class="flex gap-16 mt-8">
              <label class="toggle-label"><input type="checkbox" v-model="editForm.is_approved" class="toggle" /> 审核通过</label>
              <label class="toggle-label"><input type="checkbox" v-model="editForm.is_featured" class="toggle" /> 推荐</label>
            </div>
            <button class="btn btn-primary btn-sm mt-16" @click="saveItem" :disabled="saving" style="width:100%">保存修改</button>

            <!-- Quick Actions -->
            <div class="quick-actions mt-16">
              <button v-if="!detailItem.is_approved" class="btn btn-sm" style="background:var(--green);color:#fff;width:100%" @click="quickApprove">
                ✓ 通过审核
              </button>
              <button v-else class="btn btn-sm btn-danger" style="width:100%" @click="quickReject">
                ✗ 撤回审核
              </button>
            </div>
          </div>
        </div>

        <!-- Description preview -->
        <div v-if="detailItem.description" class="desc-preview mt-16">
          <h4 class="section-label">描述内容</h4>
          <div class="desc-content">{{ detailItem.description }}</div>
        </div>

        <!-- Screenshots -->
        <div v-if="detailItem.screenshots?.length" class="screenshots-section mt-16">
          <h4 class="section-label">截图 ({{ detailItem.screenshots.length }})</h4>
          <div class="screenshots-grid">
            <img v-for="(url, i) in detailItem.screenshots" :key="i" :src="url" class="screenshot-img" @error="$event.target.style.display='none'" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject, onMounted, reactive } from 'vue'
import { storeApi } from '../api'

const showToast = inject('showToast')
const items = ref([])
const loading = ref(true)
const search = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const sortField = ref('created_at')
const page = ref(1)
const total = ref(0)
const totalPages = ref(1)
const selectedIds = ref([])
const detailItem = ref(null)
const editForm = ref({})
const saving = ref(false)
const stats = ref({})

const categories = [
  'tools', 'social', 'education', 'entertainment', 'productivity',
  'lifestyle', 'business', 'news', 'finance', 'health', 'other'
]

const allSelected = computed(() =>
  items.value.length > 0 && selectedIds.value.length === items.value.length
)

const statCards = computed(() => {
  const s = stats.value
  return [
    { label: '总项目', value: s.total_items || 0, bg: 'linear-gradient(135deg, rgba(99,102,241,0.15), rgba(99,102,241,0.05))', svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#818cf8" stroke-width="1.8"><path d="M16.5 9.4l-9-5.19"/><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/></svg>' },
    { label: '应用', value: s.total_apps || 0, bg: 'linear-gradient(135deg, rgba(52,211,153,0.15), rgba(52,211,153,0.05))', svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="1.8"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>' },
    { label: '扩展模块', value: s.total_extensions || 0, bg: 'linear-gradient(135deg, rgba(96,165,250,0.15), rgba(96,165,250,0.05))', svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#60a5fa" stroke-width="1.8"><path d="M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z"/></svg>' },
    { label: '推荐', value: s.total_featured || 0, bg: 'linear-gradient(135deg, rgba(251,191,36,0.15), rgba(251,191,36,0.05))', svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fbbf24" stroke-width="1.8"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>' },
    { label: '待审核', value: s.pending_count || 0, bg: 'linear-gradient(135deg, rgba(248,113,113,0.15), rgba(248,113,113,0.05))', svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f87171" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>' },
    { label: '总下载', value: fmtNum(s.total_downloads || 0), bg: 'linear-gradient(135deg, rgba(167,139,250,0.15), rgba(167,139,250,0.05))', svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>' },
  ]
})

let timer
function debouncedLoad() { clearTimeout(timer); timer = setTimeout(() => { page.value = 1; loadItems() }, 400) }

function toggleAll(e) {
  selectedIds.value = e.target.checked ? items.value.map(i => i.id) : []
}

async function loadStats() {
  try { const res = await storeApi.stats(); stats.value = res.data } catch {}
}

async function loadItems() {
  loading.value = true
  try {
    const params = {
      page: page.value, page_size: 20,
      sort: sortField.value, order: 'desc',
    }
    if (search.value) params.search = search.value
    if (typeFilter.value) params.module_type = typeFilter.value
    if (statusFilter.value) params.status = statusFilter.value

    const res = await storeApi.listItems(params)
    items.value = res.data.items
    total.value = res.data.total
    totalPages.value = res.data.total_pages
    selectedIds.value = []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function openDetail(item) {
  try {
    const res = await storeApi.getItem(item.id)
    detailItem.value = res.data
    editForm.value = {
      name: res.data.name,
      category: res.data.category || '',
      tags: res.data.tags || '',
      version_name: res.data.version_name || '',
      is_approved: res.data.is_approved,
      is_featured: res.data.is_featured,
    }
  } catch { showToast('加载失败', 'error') }
}

async function saveItem() {
  saving.value = true
  try {
    await storeApi.updateItem(detailItem.value.id, editForm.value)
    showToast('已保存')
    loadItems()
    loadStats()
    // Refresh detail
    const res = await storeApi.getItem(detailItem.value.id)
    detailItem.value = res.data
  } catch { showToast('保存失败', 'error') }
  finally { saving.value = false }
}

async function quickApprove() {
  try {
    await storeApi.updateItem(detailItem.value.id, { is_approved: true })
    showToast('已通过审核')
    detailItem.value.is_approved = true
    editForm.value.is_approved = true
    loadItems(); loadStats()
  } catch { showToast('操作失败', 'error') }
}

async function quickReject() {
  if (!confirm('确定撤回审核？项目将不再在商店中显示。')) return
  try {
    await storeApi.updateItem(detailItem.value.id, { is_approved: false })
    showToast('已撤回')
    detailItem.value.is_approved = false
    editForm.value.is_approved = false
    loadItems(); loadStats()
  } catch { showToast('操作失败', 'error') }
}

async function toggleFeatured(item) {
  try {
    await storeApi.updateItem(item.id, { is_featured: !item.is_featured })
    showToast(item.is_featured ? '已取消推荐' : '已设为推荐')
    loadItems(); loadStats()
  } catch { showToast('操作失败', 'error') }
}

async function deleteItem(item) {
  if (!confirm(`确定删除「${item.name}」？此操作不可撤销。`)) return
  try {
    await storeApi.deleteItem(item.id)
    showToast('已删除')
    loadItems(); loadStats()
  } catch { showToast('删除失败', 'error') }
}

async function batchAction(action) {
  if (!selectedIds.value.length) return
  const labels = { approve: '批准', feature: '推荐', delete: '删除' }
  if (action === 'delete' && !confirm(`确定删除选中的 ${selectedIds.value.length} 个项目？`)) return
  try {
    await storeApi.batch({ ids: selectedIds.value, action })
    showToast(`已${labels[action]} ${selectedIds.value.length} 个项目`)
    selectedIds.value = []
    loadItems(); loadStats()
  } catch { showToast('批量操作失败', 'error') }
}

function typeLabel(t) {
  return { app: '应用', extension: '扩展', browser_extension: '浏览器扩展' }[t] || t
}
function typeBadge(t) {
  return { app: 'badge-success', extension: 'badge-info', browser_extension: 'badge-warning' }[t] || 'badge-secondary'
}
function typeGradient(t) {
  return {
    app: 'linear-gradient(135deg, #34d399, #059669)',
    extension: 'linear-gradient(135deg, #60a5fa, #3b82f6)',
    browser_extension: 'linear-gradient(135deg, #fbbf24, #d97706)',
  }[t] || 'linear-gradient(135deg, #6366f1, #4f46e5)'
}
function fmtNum(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
function fmtDate(d) {
  return d ? new Date(d).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''
}

onMounted(() => { loadStats(); loadItems() })
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-header h1 { margin-bottom: 4px; }

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 12px;
}
.stat-card {
  background: var(--bg-card); backdrop-filter: blur(40px);
  border: 1px solid var(--border); border-radius: var(--r-lg);
  padding: 16px; display: flex; align-items: center; gap: 12px;
  animation: slideUp 0.5s var(--ease-spring) both;
  transition: transform var(--t-normal), box-shadow var(--t-normal);
}
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.stat-icon {
  width: 38px; height: 38px; min-width: 38px; border-radius: var(--r-sm);
  display: flex; align-items: center; justify-content: center;
}
.stat-body { display: flex; flex-direction: column; }
.stat-value { font-size: 1.3rem; font-weight: 700; letter-spacing: -0.03em; }
.stat-label { font-size: 0.72rem; color: var(--text-muted); margin-top: 1px; }

/* Filter Row */
.filter-row { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
.search-wrap { position: relative; flex: 1; min-width: 200px; max-width: 320px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: var(--text-muted); pointer-events: none; }
.search-input { padding-left: 36px; }

.batch-actions {
  display: flex; align-items: center; gap: 8px;
  margin-left: auto;
  padding: 6px 14px; border-radius: var(--r-sm);
  background: var(--accent-subtle); border: 1px solid rgba(99,102,241,0.2);
  animation: fadeIn 0.3s var(--ease-out);
}
.batch-count { font-size: 0.8rem; color: var(--accent); font-weight: 500; white-space: nowrap; }

/* Table */
.item-row { transition: background var(--t-fast); }
.item-cell { display: flex; align-items: center; gap: 10px; }
.item-icon {
  width: 34px; height: 34px; min-width: 34px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.75rem; font-weight: 600; color: #fff;
}
.item-info { min-width: 0; }
.item-name { font-weight: 500; font-size: 0.87rem; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px; }

.metrics-mini { display: flex; gap: 10px; font-size: 0.75rem; color: var(--text-muted); }
.status-tags { display: flex; gap: 4px; }

.action-btns { display: flex; gap: 2px; }

.check-all, .row-check { accent-color: var(--accent); cursor: pointer; }

/* Detail Modal */
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.detail-section { background: var(--bg-input); border-radius: var(--r-md); padding: 18px; }
.section-label { font-size: 0.8rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 14px; }
.info-list { display: flex; flex-direction: column; }
.info-item { display: flex; justify-content: space-between; padding: 7px 0; font-size: 0.83rem; border-bottom: 1px solid var(--border-light); }
.info-item span:first-child { color: var(--text-muted); white-space: nowrap; margin-right: 12px; }
.info-item span:last-child { text-align: right; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }
.info-item:last-child { border-bottom: none; }

.toggle-label { display: flex; align-items: center; gap: 6px; font-size: 0.85rem; color: var(--text-secondary); cursor: pointer; }
.toggle { accent-color: var(--accent); }

.quick-actions { display: flex; flex-direction: column; gap: 8px; }

.desc-preview { background: var(--bg-input); border-radius: var(--r-md); padding: 18px; }
.desc-content { font-size: 0.85rem; color: var(--text-secondary); white-space: pre-wrap; max-height: 200px; overflow-y: auto; line-height: 1.6; }

.screenshots-section { background: var(--bg-input); border-radius: var(--r-md); padding: 18px; }
.screenshots-grid { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 8px; }
.screenshot-img { height: 160px; border-radius: 8px; border: 1px solid var(--border); object-fit: cover; flex-shrink: 0; }

@media (max-width: 900px) {
  .detail-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
