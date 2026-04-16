<template>
  <div>
    <h2 class="page-title">⚙️ 远程配置</h2>

    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreate">+ 新建配置</button>
    </div>

    <div class="card">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>键名</th><th>值</th><th>类型</th><th>受众</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="c in items" :key="c.id">
              <td><code>{{ c.config_key }}</code></td>
              <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis">{{ c.config_value }}</td>
              <td><span class="badge badge-info">{{ c.value_type }}</span></td>
              <td><span class="badge badge-purple">{{ audienceLabel(c.target_audience) }}</span></td>
              <td>
                <span :class="['badge', c.is_active ? 'badge-success' : 'badge-danger']">
                  {{ c.is_active ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="flex gap-8">
                <button class="btn btn-secondary btn-sm" @click="openEdit(c)">编辑</button>
                <button class="btn btn-danger btn-sm" @click="removeItem(c.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!items.length" class="empty-state"><p>暂无配置</p></div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3 class="modal-title">{{ isEdit ? '编辑配置' : '新建配置' }}</h3>
        <div class="form-group">
          <label class="form-label">键名</label>
          <input v-model="form.config_key" class="input" placeholder="如 free_daily_build_limit" :disabled="isEdit" />
        </div>
        <div class="form-group">
          <label class="form-label">值</label>
          <textarea v-model="form.config_value" class="textarea" placeholder="配置值"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">类型</label>
            <select v-model="form.value_type" class="select">
              <option value="string">String</option>
              <option value="number">Number</option>
              <option value="boolean">Boolean</option>
              <option value="json">JSON</option>
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
        <div class="form-group">
          <label class="form-label">描述</label>
          <input v-model="form.description" class="input" placeholder="配置说明" />
        </div>
        <div v-if="isEdit" class="form-group">
          <label class="form-label"><input type="checkbox" v-model="form.is_active" /> 启用</label>
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
import { configApi } from '../api'

const showToast = inject('showToast')
const items = ref([])
const loading = ref(true)
const showModal = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)

const defaultForm = () => ({
  config_key: '', config_value: '', value_type: 'string',
  target_audience: 'all', description: '', is_active: true,
})
const form = ref(defaultForm())

function openCreate() { isEdit.value = false; form.value = defaultForm(); showModal.value = true }
function openEdit(c) {
  isEdit.value = true; editId.value = c.id
  form.value = { ...c }
  showModal.value = true
}

async function loadItems() {
  loading.value = true
  try { const res = await configApi.list(); items.value = res.data }
  catch (e) { console.error(e) } finally { loading.value = false }
}

async function saveItem() {
  saving.value = true
  try {
    if (isEdit.value) await configApi.update(editId.value, form.value)
    else await configApi.create(form.value)
    showToast(isEdit.value ? '配置已更新' : '配置已创建')
    showModal.value = false; loadItems()
  } catch (e) { showToast(e?.detail || '操作失败', 'error') }
  finally { saving.value = false }
}

async function removeItem(id) {
  if (!confirm('确定删除此配置？')) return
  try { await configApi.remove(id); showToast('已删除'); loadItems() }
  catch (e) { showToast('删除失败', 'error') }
}

function audienceLabel(a) { return { all: '全部', free: '免费用户', pro: 'Pro 用户', ultra: 'Ultra 用户' }[a] || a }

onMounted(loadItems)
</script>
