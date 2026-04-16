<template>
  <div>
    <div class="page-header">
      <button class="btn btn-secondary btn-sm" @click="$router.push('/projects')">← 返回</button>
      <h2 class="page-title" v-if="project">{{ project.project_name }}</h2>
      <div v-if="project" class="page-meta">
        <code class="key-badge">{{ project.project_key }}</code>
        <a :href="shareUrl" target="_blank" class="btn btn-primary btn-sm">📤 分享下载页</a>
      </div>
    </div>

    <div v-if="loading" class="spinner"></div>
    <template v-else-if="project">
      <!-- Tabs -->
      <div class="tabs">
        <button v-for="t in tabs" :key="t.key" :class="['tab', { active: tab === t.key }]"
          @click="tab = t.key; loadTab()">{{ t.icon }} {{ t.label }}</button>
      </div>

      <!-- Tab: Activation Codes -->
      <div v-if="tab === 'codes'" class="card mt-12">
        <div class="card-header">
          <h3>🔑 激活码</h3>
          <div class="flex gap-8">
            <input v-model.number="codeCount" type="number" min="1" max="100" class="input" style="width:80px" placeholder="数量" />
            <input v-model="codeLabel" class="input" style="width:140px" placeholder="标签(可选)" />
            <button class="btn btn-primary btn-sm" @click="genCodes">生成</button>
          </div>
        </div>
        <table v-if="codes.length">
          <thead><tr><th>激活码</th><th>标签</th><th>状态</th><th>设备ID</th><th>激活时间</th></tr></thead>
          <tbody>
            <tr v-for="c in codes" :key="c.id">
              <td><code style="font-size:11px">{{ c.code || c.code_hash?.slice(0,12) + '...' }}</code></td>
              <td>{{ c.label || '-' }}</td>
              <td><span :class="['badge', c.status === 'used' ? 'badge-success' : c.status === 'disabled' ? 'badge-danger' : 'badge-secondary']">{{ c.status }}</span></td>
              <td style="font-size:11px;max-width:120px;overflow:hidden;text-overflow:ellipsis">{{ c.activated_device_id || '-' }}</td>
              <td style="font-size:12px">{{ fmtDate(c.activated_at) || '-' }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state"><p>暂无激活码</p></div>
      </div>

      <!-- Tab: Announcements -->
      <div v-if="tab === 'announcements'" class="card mt-12">
        <div class="card-header">
          <h3>📢 公告</h3>
          <button class="btn btn-primary btn-sm" @click="showAnnForm = !showAnnForm">+ 新公告</button>
        </div>
        <div v-if="showAnnForm" class="form-inline">
          <input v-model="annForm.title" class="input" placeholder="标题" />
          <textarea v-model="annForm.content" class="input" placeholder="内容" rows="3"></textarea>
          <div class="flex gap-8">
            <select v-model="annForm.display_type" class="select">
              <option value="popup">弹窗</option><option value="banner">横幅</option><option value="fullscreen">全屏</option>
            </select>
            <button class="btn btn-primary btn-sm" @click="createAnn">发布</button>
          </div>
        </div>
        <table v-if="announcements.length">
          <thead><tr><th>标题</th><th>类型</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="a in announcements" :key="a.id">
              <td><strong>{{ a.title }}</strong></td>
              <td>{{ a.display_type }}</td>
              <td><span :class="['badge', a.is_active ? 'badge-success' : 'badge-danger']">{{ a.is_active ? '活跃' : '停用' }}</span></td>
              <td>
                <button class="btn btn-secondary btn-sm" @click="toggleAnn(a)">{{ a.is_active ? '停用' : '启用' }}</button>
                <button class="btn btn-danger btn-sm" @click="removeAnn(a.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state"><p>暂无公告</p></div>
      </div>

      <!-- Tab: Versions -->
      <div v-if="tab === 'versions'" class="card mt-12">
        <div class="card-header">
          <h3>📦 版本</h3>
          <button class="btn btn-primary btn-sm" @click="showVerForm = !showVerForm">+ 发布版本</button>
        </div>
        <div v-if="showVerForm" class="form-inline">
          <div class="flex gap-8">
            <input v-model.number="verForm.version_code" type="number" class="input" placeholder="版本号(数字)" style="width:120px" />
            <input v-model="verForm.version_name" class="input" placeholder="版本名(如 1.0.0)" style="width:140px" />
          </div>
          <textarea v-model="verForm.changelog" class="input" placeholder="更新日志" rows="2"></textarea>
          <div class="flex gap-8 items-center">
            <input type="file" accept=".apk" @change="verForm.file = $event.target.files[0]" />
            <label><input type="checkbox" v-model="verForm.is_force_update" /> 强制更新</label>
            <select v-model="verForm.upload_to" class="select" style="width:120px">
              <option value="both">GitHub+Gitee</option><option value="github">仅GitHub</option><option value="gitee">仅Gitee</option>
            </select>
            <button class="btn btn-primary btn-sm" @click="publishVer" :disabled="publishing">{{ publishing ? '上传中...' : '发布' }}</button>
          </div>
        </div>
        <table v-if="versions.length">
          <thead><tr><th>版本</th><th>大小</th><th>GitHub</th><th>Gitee</th><th>强制</th><th>时间</th></tr></thead>
          <tbody>
            <tr v-for="v in versions" :key="v.id">
              <td><strong>v{{ v.version_name }}</strong> ({{ v.version_code }})</td>
              <td>{{ v.file_size ? (v.file_size / 1024 / 1024).toFixed(1) + 'MB' : '-' }}</td>
              <td><a v-if="v.download_url_github" :href="v.download_url_github" target="_blank" class="link">下载</a><span v-else>-</span></td>
              <td><a v-if="v.download_url_gitee" :href="v.download_url_gitee" target="_blank" class="link">下载</a><span v-else>-</span></td>
              <td>{{ v.is_force_update ? '✅' : '' }}</td>
              <td style="font-size:12px">{{ fmtDate(v.created_at) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state"><p>暂无版本</p></div>
      </div>

      <!-- Tab: Remote Config -->
      <div v-if="tab === 'configs'" class="card mt-12">
        <div class="card-header">
          <h3>⚙️ 远程配置</h3>
          <button class="btn btn-primary btn-sm" @click="showCfgForm = !showCfgForm">+ 添加</button>
        </div>
        <div v-if="showCfgForm" class="form-inline">
          <div class="flex gap-8">
            <input v-model="cfgForm.config_key" class="input" placeholder="键名" />
            <input v-model="cfgForm.config_value" class="input" placeholder="值" />
            <select v-model="cfgForm.value_type" class="select" style="width:100px">
              <option value="string">String</option><option value="number">Number</option>
              <option value="boolean">Boolean</option><option value="json">JSON</option>
            </select>
            <button class="btn btn-primary btn-sm" @click="createCfg">添加</button>
          </div>
        </div>
        <table v-if="configs.length">
          <thead><tr><th>键</th><th>值</th><th>类型</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="c in configs" :key="c.id">
              <td><code>{{ c.config_key }}</code></td>
              <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis">{{ c.config_value }}</td>
              <td>{{ c.value_type }}</td>
              <td><button class="btn btn-danger btn-sm" @click="removeCfg(c.id)">删除</button></td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state"><p>暂无配置</p></div>
      </div>

      <!-- Tab: Analytics -->
      <div v-if="tab === 'analytics'" class="card mt-12">
        <div class="card-header">
          <h3>📊 统计分析</h3>
          <div class="flex gap-8">
            <button v-for="d in [7,14,30]" :key="d" :class="['btn btn-sm', analyticsDays === d ? 'btn-primary' : 'btn-secondary']"
              @click="analyticsDays = d; loadAnalytics()">{{ d }}天</button>
          </div>
        </div>
        <template v-if="analytics">
          <div class="stats-row">
            <div class="mini-stat"><span class="mini-value">{{ analytics.summary.total_opens }}</span><span class="mini-label">打开次数</span></div>
            <div class="mini-stat"><span class="mini-value">{{ analytics.summary.total_installs }}</span><span class="mini-label">安装数</span></div>
            <div class="mini-stat"><span class="mini-value">{{ analytics.summary.avg_daily_active }}</span><span class="mini-label">日均活跃</span></div>
            <div class="mini-stat"><span class="mini-value">{{ analytics.summary.total_crashes }}</span><span class="mini-label">崩溃数</span></div>
          </div>
          <div class="chart-container" v-if="analytics.trend.length">
            <Line :data="chartData" :options="chartOpts" />
          </div>
          <div class="dist-grid">
            <div class="dist-card" v-if="Object.keys(analytics.country_distribution || {}).length">
              <h4>🌍 地区分布</h4>
              <div v-for="(v,k) in analytics.country_distribution" :key="k" class="dist-row">
                <span>{{ k }}</span><span>{{ v }}</span>
              </div>
            </div>
            <div class="dist-card" v-if="Object.keys(analytics.device_distribution || {}).length">
              <h4>📱 设备分布</h4>
              <div v-for="(v,k) in analytics.device_distribution" :key="k" class="dist-row">
                <span>{{ k }}</span><span>{{ v }}</span>
              </div>
            </div>
            <div class="dist-card" v-if="Object.keys(analytics.os_distribution || {}).length">
              <h4>🤖 系统版本</h4>
              <div v-for="(v,k) in analytics.os_distribution" :key="k" class="dist-row">
                <span>{{ k }}</span><span>{{ v }}</span>
              </div>
            </div>
            <div class="dist-card" v-if="Object.keys(analytics.version_distribution || {}).length">
              <h4>📦 APP版本分布</h4>
              <div v-for="(v,k) in analytics.version_distribution" :key="k" class="dist-row">
                <span>v{{ k }}</span><span>{{ v }}</span>
              </div>
            </div>
          </div>
          <div v-if="!analytics.trend.length" class="empty-state"><p>暂无统计数据</p></div>
        </template>
      </div>

      <!-- Tab: Webhooks -->
      <div v-if="tab === 'webhooks'" class="card mt-12">
        <div class="card-header">
          <h3>🔗 Webhook</h3>
          <button class="btn btn-primary btn-sm" @click="showWhForm = !showWhForm">+ 添加</button>
        </div>
        <div v-if="showWhForm" class="form-inline">
          <input v-model="whForm.url" class="input" placeholder="Webhook URL (https://...)" />
          <div class="flex gap-8">
            <input v-model="whForm.secret" class="input" placeholder="密钥(可选)" style="width:160px" />
            <select v-model="whForm.events" class="select" multiple style="width:280px;height:80px">
              <option value="code_activated">激活码使用</option>
              <option value="version_published">版本发布</option>
              <option value="daily_report">每日报告</option>
              <option value="announcement_created">公告创建</option>
            </select>
            <button class="btn btn-primary btn-sm" @click="createWh">添加</button>
          </div>
        </div>
        <table v-if="webhooks.length">
          <thead><tr><th>URL</th><th>事件</th><th>状态</th><th>上次触发</th><th>失败</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="w in webhooks" :key="w.id">
              <td style="font-size:12px;max-width:200px;overflow:hidden;text-overflow:ellipsis">{{ w.url }}</td>
              <td><span class="badge badge-info" v-for="e in w.events" :key="e" style="margin:2px">{{ eventLabel(e) }}</span></td>
              <td><span :class="['badge', w.is_active ? 'badge-success' : 'badge-danger']">{{ w.is_active ? '活跃' : '禁用' }}</span></td>
              <td style="font-size:12px">{{ fmtDate(w.last_triggered_at) || '从未' }}</td>
              <td>{{ w.failure_count }}</td>
              <td><button class="btn btn-danger btn-sm" @click="removeWh(w.id)">删除</button></td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state"><p>暂无 Webhook</p></div>
      </div>

      <!-- Tab: Backups -->
      <div v-if="tab === 'backups'" class="card mt-12">
        <div class="card-header">
          <h3>💾 项目备份</h3>
          <div class="flex gap-8">
            <select v-model="backupPlatform" class="select" style="width:120px">
              <option value="github">GitHub</option><option value="gitee">Gitee</option>
            </select>
            <input type="file" accept=".zip" @change="backupFile = $event.target.files[0]" />
            <button class="btn btn-primary btn-sm" @click="doBackup" :disabled="backing">{{ backing ? '上传中...' : '上传备份' }}</button>
          </div>
        </div>
        <table v-if="backups.length">
          <thead><tr><th>平台</th><th>大小</th><th>状态</th><th>链接</th><th>时间</th></tr></thead>
          <tbody>
            <tr v-for="b in backups" :key="b.id">
              <td><span :class="['badge', b.platform === 'github' ? 'badge-secondary' : 'badge-danger']">{{ b.platform }}</span></td>
              <td>{{ b.file_size ? (b.file_size / 1024 / 1024).toFixed(2) + 'MB' : '-' }}</td>
              <td><span :class="['badge', b.status === 'success' ? 'badge-success' : b.status === 'failed' ? 'badge-danger' : 'badge-warning']">{{ b.status }}</span></td>
              <td><a v-if="b.repo_url" :href="b.repo_url" target="_blank" class="link">查看</a><span v-else>-</span></td>
              <td style="font-size:12px">{{ fmtDate(b.created_at) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state"><p>暂无备份记录</p></div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { projectApi } from '../api'
import { Line } from 'vue-chartjs'
import { Chart, CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip } from 'chart.js'

Chart.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip)

const route = useRoute()
const showToast = inject('showToast')
const projectId = computed(() => Number(route.params.id))

const loading = ref(true)
const project = ref(null)
const tab = ref('codes')

// Tab data
const codes = ref([])
const announcements = ref([])
const versions = ref([])
const configs = ref([])
const analytics = ref(null)
const webhooks = ref([])
const backups = ref([])

// Forms
const codeCount = ref(10)
const codeLabel = ref('')
const showAnnForm = ref(false)
const annForm = ref({ title: '', content: '', display_type: 'popup' })
const showVerForm = ref(false)
const verForm = ref({ version_code: 1, version_name: '1.0.0', changelog: '', file: null, is_force_update: false, upload_to: 'both' })
const publishing = ref(false)
const showCfgForm = ref(false)
const cfgForm = ref({ config_key: '', config_value: '', value_type: 'string' })
const analyticsDays = ref(7)
const showWhForm = ref(false)
const whForm = ref({ url: '', secret: '', events: ['code_activated'] })
const backupPlatform = ref('github')
const backupFile = ref(null)
const backing = ref(false)

const tabs = [
  { key: 'codes', icon: '🔑', label: '激活码' },
  { key: 'announcements', icon: '📢', label: '公告' },
  { key: 'versions', icon: '📦', label: '版本' },
  { key: 'configs', icon: '⚙️', label: '配置' },
  { key: 'analytics', icon: '📊', label: '统计' },
  { key: 'webhooks', icon: '🔗', label: 'Webhook' },
  { key: 'backups', icon: '💾', label: '备份' },
]

const shareUrl = computed(() => `https://api.shiaho.sbs/s/${project.value?.project_key || ''}`)

const chartData = computed(() => ({
  labels: (analytics.value?.trend || []).map(d => d.date),
  datasets: [
    { label: '打开', data: (analytics.value?.trend || []).map(d => d.opens), borderColor: '#6366f1', backgroundColor: 'rgba(99,102,241,0.1)', tension: 0.4, fill: true },
    { label: '活跃', data: (analytics.value?.trend || []).map(d => d.active), borderColor: '#10b981', backgroundColor: 'rgba(16,185,129,0.05)', tension: 0.4, fill: true },
  ],
}))
const chartOpts = { responsive: true, maintainAspectRatio: false,
  plugins: { legend: { display: true, labels: { color: '#888' } } },
  scales: { y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.06)' }, ticks: { color: '#888' } },
    x: { grid: { display: false }, ticks: { color: '#888' } } }
}

async function loadProject() {
  try {
    const res = await projectApi.get(projectId.value)
    project.value = res.data
  } catch (e) { showToast('加载项目失败', 'error') }
  finally { loading.value = false }
}

async function loadTab() {
  if (tab.value === 'codes') loadCodes()
  else if (tab.value === 'announcements') loadAnns()
  else if (tab.value === 'versions') loadVers()
  else if (tab.value === 'configs') loadCfgs()
  else if (tab.value === 'analytics') loadAnalytics()
  else if (tab.value === 'webhooks') loadWhs()
  else if (tab.value === 'backups') loadBkps()
}

async function loadCodes() { try { const r = await projectApi.listCodes(projectId.value); codes.value = r.data } catch (e) { console.error('loadCodes failed:', e); showToast('加载激活码失败', 'error') } }
async function loadAnns() { try { const r = await projectApi.listAnns(projectId.value); announcements.value = r.data } catch (e) { console.error('loadAnns failed:', e); showToast('加载公告失败', 'error') } }
async function loadVers() { try { const r = await projectApi.listVersions(projectId.value); versions.value = r.data } catch (e) { console.error('loadVers failed:', e); showToast('加载版本失败', 'error') } }
async function loadCfgs() { try { const r = await projectApi.listCfgs(projectId.value); configs.value = r.data } catch (e) { console.error('loadCfgs failed:', e); showToast('加载配置失败', 'error') } }
async function loadWhs() { try { const r = await projectApi.listWebhooks(projectId.value); webhooks.value = r.data } catch (e) { console.error('loadWhs failed:', e); showToast('加载Webhook失败', 'error') } }
async function loadBkps() { try { const r = await projectApi.listBackups(projectId.value); backups.value = r.data } catch (e) { console.error('loadBkps failed:', e); showToast('加载备份失败', 'error') } }
async function loadAnalytics() { try { const r = await projectApi.analytics(projectId.value, analyticsDays.value); analytics.value = r.data } catch (e) { console.error('loadAnalytics failed:', e); showToast('加载统计失败', 'error') } }

async function genCodes() {
  try { await projectApi.generateCodes(projectId.value, { count: codeCount.value, label: codeLabel.value || undefined }); showToast('激活码已生成'); loadCodes() }
  catch (e) { showToast(e.detail || '生成失败', 'error') }
}
async function createAnn() {
  try { await projectApi.createAnn(projectId.value, annForm.value); showToast('公告已发布'); showAnnForm.value = false; loadAnns() }
  catch (e) { console.error('createAnn failed:', e); showToast('发布失败', 'error') }
}
async function toggleAnn(a) {
  try { await projectApi.updateAnn(projectId.value, a.id, { ...a, is_active: !a.is_active }); loadAnns() } catch (e) { console.error('toggleAnn failed:', e); showToast('操作失败', 'error') }
}
async function removeAnn(id) {
  if (!confirm('删除公告？')) return
  try { await projectApi.removeAnn(projectId.value, id); loadAnns() } catch (e) { console.error('removeAnn failed:', e); showToast('删除失败', 'error') }
}
async function publishVer() {
  if (!verForm.value.file) return showToast('请选择 APK 文件', 'error')
  publishing.value = true
  try {
    const fd = new FormData()
    fd.append('apk_file', verForm.value.file)
    fd.append('version_code', verForm.value.version_code)
    fd.append('version_name', verForm.value.version_name)
    fd.append('changelog', verForm.value.changelog || '')
    fd.append('is_force_update', verForm.value.is_force_update)
    fd.append('upload_to', verForm.value.upload_to)
    await projectApi.publishVersion(projectId.value, fd)
    showToast('版本发布成功'); showVerForm.value = false; loadVers()
  } catch (e) { showToast(e.detail || '发布失败', 'error') }
  finally { publishing.value = false }
}
async function createCfg() {
  try { await projectApi.createCfg(projectId.value, cfgForm.value); showToast('配置已添加'); showCfgForm.value = false; loadCfgs() }
  catch { showToast('添加失败', 'error') }
}
async function removeCfg(id) {
  if (!confirm('删除配置？')) return
  try { await projectApi.removeCfg(projectId.value, id); loadCfgs() } catch (e) { console.error('removeCfg failed:', e); showToast('删除失败', 'error') }
}
async function createWh() {
  if (!whForm.value.url) return showToast('请输入 URL', 'error')
  try {
    await projectApi.createWebhook(projectId.value, {
      url: whForm.value.url, secret: whForm.value.secret || undefined,
      events: Array.isArray(whForm.value.events) ? whForm.value.events.join(',') : whForm.value.events,
    })
    showToast('Webhook 已添加'); showWhForm.value = false; loadWhs()
  } catch (e) { showToast(e.detail || '添加失败', 'error') }
}
async function removeWh(id) {
  if (!confirm('删除 Webhook？')) return
  try { await projectApi.removeWebhook(projectId.value, id); loadWhs() } catch (e) { console.error('removeWh failed:', e); showToast('删除失败', 'error') }
}
async function doBackup() {
  if (!backupFile.value) return showToast('请选择 ZIP 文件', 'error')
  backing.value = true
  try {
    const fd = new FormData()
    fd.append('file', backupFile.value)
    const r = await projectApi.backup(projectId.value, backupPlatform.value, fd)
    showToast(r.message || '备份完成'); loadBkps()
  } catch (e) { showToast(e.detail || '备份失败', 'error') }
  finally { backing.value = false }
}

function eventLabel(e) {
  return { code_activated: '激活', version_published: '发版', daily_report: '日报', announcement_created: '公告' }[e] || e
}
function fmtDate(d) { return d ? new Date(d).toLocaleString('zh-CN') : '' }

onMounted(() => { loadProject(); loadCodes() })
</script>

<style scoped>
.page-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; margin: 0; }
.page-meta { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.key-badge { background: var(--bg-input); padding: 4px 10px; border-radius: 6px; font-size: 11px; }

.tabs { display: flex; gap: 2px; border-bottom: 1px solid var(--border); flex-wrap: wrap; }
.tab {
  padding: 8px 14px; font-size: 13px; color: var(--text-secondary);
  background: none; border: none; border-bottom: 2px solid transparent;
  cursor: pointer; font-family: inherit; transition: var(--transition);
}
.tab:hover { color: var(--text-primary); }
.tab.active { color: var(--accent); border-bottom-color: var(--accent); }

.card-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; border-bottom: 1px solid var(--border); }
.card-header h3 { font-size: 15px; font-weight: 600; margin: 0; }
.form-inline { padding: 14px 16px; display: flex; flex-direction: column; gap: 10px; border-bottom: 1px solid var(--border); }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; padding: 16px; }
.mini-stat { display: flex; flex-direction: column; align-items: center; padding: 12px; background: var(--bg-input); border-radius: var(--radius); }
.mini-value { font-size: 22px; font-weight: 700; }
.mini-label { font-size: 11px; color: var(--text-muted); margin-top: 4px; }

.chart-container { height: 200px; padding: 0 16px 16px; }

.dist-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 10px; padding: 0 16px 16px; }
.dist-card { background: var(--bg-input); border-radius: var(--radius); padding: 12px; }
.dist-card h4 { font-size: 13px; margin: 0 0 8px; }
.dist-row { display: flex; justify-content: space-between; padding: 3px 0; font-size: 12px; border-bottom: 1px solid var(--border); }

.link { color: var(--accent); text-decoration: none; font-size: 13px; }
.link:hover { text-decoration: underline; }
</style>
