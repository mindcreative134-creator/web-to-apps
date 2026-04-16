<template>
  <div class="view-page">
    <div class="page-header">
      <h1>推送通知</h1>
      <p class="page-desc">向用户发送通知消息</p>
    </div>

    <!-- 发送表单 -->
    <div class="card send-card">
      <h2 class="card-title">发送推送</h2>
      <div class="form-group">
        <label>标题</label>
        <input v-model="form.title" type="text" class="input" placeholder="通知标题" />
      </div>
      <div class="form-group">
        <label>内容</label>
        <textarea v-model="form.content" class="input textarea" rows="3" placeholder="通知内容"></textarea>
      </div>
      <div class="form-row">
        <div class="form-group flex-1">
          <label>目标</label>
          <select v-model="form.target" class="input">
            <option value="all">所有用户</option>
            <option value="pro">Pro 用户</option>
            <option value="ultra">Ultra 用户</option>
            <option value="free">免费用户</option>
          </select>
        </div>
        <div class="form-group flex-1">
          <label>类型</label>
          <select v-model="form.type" class="input">
            <option value="info">信息</option>
            <option value="announcement">公告</option>
            <option value="update">更新</option>
            <option value="promotion">促销</option>
          </select>
        </div>
      </div>
      <button class="btn btn-primary" @click="sendPush" :disabled="sending">
        {{ sending ? '发送中...' : '发送推送' }}
      </button>
    </div>

    <!-- 推送历史 -->
    <div class="card">
      <h2 class="card-title">推送历史</h2>
      <div v-if="history.length === 0" class="empty-state">
        <p>暂无推送记录</p>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>类型</th>
            <th>目标</th>
            <th>送达数</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in history" :key="item.id">
            <td>{{ item.title }}</td>
            <td><span class="type-badge" :class="item.type">{{ item.type }}</span></td>
            <td>{{ item.target }}</td>
            <td>{{ item.delivered_count ?? '-' }}</td>
            <td class="text-muted">{{ item.created_at }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import { pushApi } from '../api/index.js'

const showToast = inject('showToast')
const sending = ref(false)
const history = ref([])

const form = reactive({
  title: '',
  content: '',
  target: 'all',
  type: 'info',
})

async function sendPush() {
  if (!form.title || !form.content) { showToast('请填写标题和内容', 'error'); return }
  sending.value = true
  try {
    await pushApi.send({ ...form })
    showToast('推送已发送')
    form.title = ''; form.content = ''
    loadHistory()
  } catch {
    showToast('发送失败', 'error')
  } finally {
    sending.value = false
  }
}

async function loadHistory() {
  try {
    const res = await pushApi.history({ page: 1, size: 50 })
    history.value = res?.data?.items || res?.data || []
  } catch {
    history.value = []
  }
}

onMounted(loadHistory)
</script>

<style scoped>
.view-page { max-width: 900px; }
.page-header { margin-bottom: 28px; }
.page-header h1 { font-size: 1.5rem; font-weight: 700; letter-spacing: -0.03em; }
.page-desc { color: var(--text-secondary); font-size: 0.87rem; margin-top: 4px; }

.card {
  padding: 24px; border-radius: var(--r-md);
  background: var(--bg-card); border: 1px solid var(--border); margin-bottom: 20px;
}
.card-title { font-size: 1.05rem; font-weight: 600; margin-bottom: 18px; }

.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 0.82rem; color: var(--text-secondary); margin-bottom: 6px; font-weight: 500; }
.input {
  width: 100%; padding: 9px 12px; border-radius: var(--r-xs);
  background: var(--bg-input); border: 1px solid var(--border);
  color: var(--text-primary); font-family: var(--font); font-size: 0.87rem;
  transition: border-color var(--t-fast); outline: none;
}
.input:focus { border-color: var(--accent); }
.textarea { resize: vertical; min-height: 80px; }

.form-row { display: flex; gap: 14px; }
.flex-1 { flex: 1; }

.btn { padding: 10px 20px; border: none; border-radius: var(--r-xs); font-family: var(--font); font-size: 0.87rem; cursor: pointer; transition: all var(--t-fast); }
.btn-primary { background: var(--accent); color: #fff; font-weight: 500; }
.btn-primary:hover { filter: brightness(1.1); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { text-align: left; padding: 10px 12px; font-size: 0.85rem; border-bottom: 1px solid var(--border); }
.data-table th { color: var(--text-secondary); font-weight: 500; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.04em; }
.text-muted { color: var(--text-muted); }
.empty-state { text-align: center; padding: 40px 20px; color: var(--text-secondary); }

.type-badge {
  font-size: 0.72rem; padding: 2px 8px; border-radius: 10px; font-weight: 600;
  background: var(--accent-subtle); color: var(--accent);
}
.type-badge.announcement { background: var(--yellow-subtle, rgba(251,191,36,0.1)); color: var(--yellow, #fbbf24); }
.type-badge.update { background: var(--green-subtle, rgba(34,197,94,0.1)); color: var(--green, #22c55e); }
.type-badge.promotion { background: var(--purple-subtle, rgba(168,85,247,0.1)); color: var(--purple, #a855f7); }
</style>
