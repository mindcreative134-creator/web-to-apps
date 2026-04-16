<template>
  <div>
    <h2 class="page-title">📢 公告管理</h2>

    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreate">+ 新建公告</button>
    </div>

    <div class="card">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>标题</th><th>类型</th><th>受众</th><th>状态</th><th>开始时间</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="a in items" :key="a.id">
              <td><strong>{{ a.title }}</strong></td>
              <td><span class="badge badge-info">{{ a.display_type }}</span></td>
              <td><span class="badge badge-purple">{{ audienceLabel(a.target_audience) }}</span></td>
              <td>
                <span :class="['badge', a.is_active ? 'badge-success' : 'badge-danger']">
                  {{ a.is_active ? '活跃' : '停用' }}
                </span>
              </td>
              <td>{{ formatDate(a.start_at) }}</td>
              <td class="flex gap-8">
                <button class="btn btn-secondary btn-sm" @click="openEdit(a)">编辑</button>
                <button class="btn btn-danger btn-sm" @click="removeItem(a.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!items.length" class="empty-state"><p>暂无公告</p></div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3 class="modal-title">{{ isEdit ? '编辑公告' : '新建公告' }}</h3>
        <div class="form-group">
          <label class="form-label">标题</label>
          <input v-model="form.title" class="input" placeholder="公告标题" />
        </div>
        <div class="form-group">
          <label class="form-label">内容</label>
          <textarea v-model="form.content" class="textarea" placeholder="公告内容（支持 Markdown）"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">英文内容 (可选)</label>
          <textarea v-model="form.content_en" class="textarea" placeholder="English content"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">显示类型</label>
            <select v-model="form.display_type" class="select">
              <option value="popup">弹窗</option>
              <option value="banner">横幅</option>
              <option value="fullscreen">全屏</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">目标受众</label>
            <select v-model="form.target_audience" class="select">
              <option value="all">全部</option>
              <option value="free">免费用户</option>
              <option value="pro">Pro 用户</option>
              <option value="ultra">Ultra 用户</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">开始时间</label>
            <input v-model="form.start_at" type="datetime-local" class="input" />
          </div>
          <div class="form-group">
            <label class="form-label">结束时间 (可选)</label>
            <input v-model="form.end_at" type="datetime-local" class="input" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">操作链接 (可选)</label>
          <input v-model="form.action_url" class="input" placeholder="https://..." />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label"><input type="checkbox" v-model="form.is_active" /> 立即激活</label>
          </div>
          <div class="form-group">
            <label class="form-label"><input type="checkbox" v-model="form.dismissible" /> 可关闭</label>
          </div>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="showModal=false">取消</button>
          <button class="btn btn-primary" @click="saveItem" :disabled="saving">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { announcementApi } from '../api'

const showToast = inject('showToast')
const items = ref([])
const loading = ref(true)
const showModal = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)

const defaultForm = () => ({
  title: '', content: '', content_en: '', display_type: 'popup',
  target_audience: 'all', start_at: new Date().toISOString().slice(0,16),
  end_at: '', action_url: '', is_active: true, dismissible: true,
})
const form = ref(defaultForm())

function openCreate() { isEdit.value = false; form.value = defaultForm(); showModal.value = true }
function openEdit(a) {
  isEdit.value = true; editId.value = a.id
  form.value = { ...a, start_at: a.start_at?.slice(0,16) || '', end_at: a.end_at?.slice(0,16) || '' }
  showModal.value = true
}

async function loadItems() {
  loading.value = true
  try {
    const res = await announcementApi.list({ page: 1, page_size: 50 })
    items.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function saveItem() {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (!payload.end_at) delete payload.end_at
    if (isEdit.value) await announcementApi.update(editId.value, payload)
    else await announcementApi.create(payload)
    showToast(isEdit.value ? '公告已更新' : '公告已创建')
    showModal.value = false; loadItems()
  } catch (e) { showToast(e?.detail || '操作失败', 'error') }
  finally { saving.value = false }
}

async function removeItem(id) {
  if (!confirm('确定删除此公告？')) return
  try { await announcementApi.remove(id); showToast('已删除'); loadItems() }
  catch (e) { showToast('删除失败', 'error') }
}

function audienceLabel(a) { return { all: '全部', free: '免费用户', pro: 'Pro 用户', ultra: 'Ultra 用户' }[a] || a }
function formatDate(d) { return d ? new Date(d).toLocaleString('zh-CN') : '-' }

onMounted(loadItems)
</script>
