<template>
  <div>
    <h2 class="page-title">📦 版本更新管理</h2>

    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreate">+ 发布新版本</button>
    </div>

    <div class="card">
      <div v-if="loading" class="spinner"></div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr><th>版本号</th><th>版本名</th><th>标题</th><th>强制更新</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="v in items" :key="v.id">
              <td><strong>{{ v.version_code }}</strong></td>
              <td>v{{ v.version_name }}</td>
              <td>{{ v.title }}</td>
              <td>
                <span :class="['badge', v.is_force_update ? 'badge-danger' : 'badge-success']">
                  {{ v.is_force_update ? '强制' : '可选' }}
                </span>
              </td>
              <td>
                <span :class="['badge', v.is_published ? 'badge-success' : 'badge-warning']">
                  {{ v.is_published ? '已发布' : '草稿' }}
                </span>
              </td>
              <td class="flex gap-8">
                <button class="btn btn-sm" :class="v.is_published ? 'btn-secondary' : 'btn-success'"
                  @click="togglePublish(v.id)">
                  {{ v.is_published ? '取消发布' : '发布' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="!items.length" class="empty-state"><p>暂无版本</p></div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3 class="modal-title">发布新版本</h3>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">版本号 (code)</label>
            <input v-model.number="form.version_code" type="number" class="input" placeholder="如 200" />
          </div>
          <div class="form-group">
            <label class="form-label">版本名</label>
            <input v-model="form.version_name" class="input" placeholder="如 2.0.0" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">标题</label>
          <input v-model="form.title" class="input" placeholder="更新标题" />
        </div>
        <div class="form-group">
          <label class="form-label">更新日志</label>
          <textarea v-model="form.changelog" class="textarea" placeholder="- 新功能 A\n- 修复 Bug B"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">下载链接</label>
          <input v-model="form.download_url" class="input" placeholder="https://..." />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">文件大小 (bytes)</label>
            <input v-model.number="form.file_size" type="number" class="input" />
          </div>
          <div class="form-group">
            <label class="form-label">最低版本号</label>
            <input v-model.number="form.min_version_code" type="number" class="input" placeholder="低于此版本强制更新" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label"><input type="checkbox" v-model="form.is_force_update" /> 强制更新</label>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="showModal=false">取消</button>
          <button class="btn btn-primary" @click="createVersion" :disabled="saving">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { versionApi } from '../api'

const showToast = inject('showToast')
const items = ref([])
const loading = ref(true)
const showModal = ref(false)
const saving = ref(false)
const form = ref({
  version_code: 0, version_name: '', title: '', changelog: '',
  download_url: '', file_size: 0, min_version_code: 0, is_force_update: false,
})

function openCreate() {
  form.value = { version_code: 0, version_name: '', title: '', changelog: '',
    download_url: '', file_size: 0, min_version_code: 0, is_force_update: false }
  showModal.value = true
}

async function loadItems() {
  loading.value = true
  try { const res = await versionApi.list({ page: 1, page_size: 50 }); items.value = res.data }
  catch (e) { console.error(e) } finally { loading.value = false }
}

async function createVersion() {
  saving.value = true
  try {
    await versionApi.create(form.value)
    showToast('版本已创建'); showModal.value = false; loadItems()
  } catch (e) { showToast(e?.detail || '创建失败', 'error') }
  finally { saving.value = false }
}

async function togglePublish(id) {
  try { await versionApi.togglePublish(id); showToast('状态已更新'); loadItems() }
  catch (e) { showToast('操作失败', 'error') }
}

onMounted(loadItems)
</script>
