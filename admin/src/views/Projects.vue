<template>
  <div>
    <div class="flex items-center justify-between mb-20">
      <h2 class="page-title">☁️ 云项目管理</h2>
      <button class="btn btn-primary" @click="showCreate = true">+ 新建项目</button>
    </div>

    <div v-if="loading" class="spinner"></div>

    <div v-else-if="!projects.length" class="empty-hero">
      <div class="empty-icon">☁️</div>
      <h3>还没有云项目</h3>
      <p>创建项目后，你可以为构建的 APP 提供激活码、公告推送、版本更新和远程配置服务</p>
      <button class="btn btn-primary" @click="showCreate = true">创建第一个项目</button>
    </div>

    <div v-else class="projects-grid">
      <div v-for="p in projects" :key="p.id" class="project-card" @click="$router.push(`/projects/${p.id}`)">
        <div class="project-card-head">
          <div class="project-icon">{{ p.project_name[0]?.toUpperCase() }}</div>
          <div>
            <h3>{{ p.project_name }}</h3>
            <span class="text-muted" style="font-size:12px">{{ p.package_name || '未设置包名' }}</span>
          </div>
        </div>
        <p class="project-desc">{{ p.description || '暂无描述' }}</p>
        <div class="project-meta">
          <code class="project-key">{{ p.project_key }}</code>
          <span :class="['badge', p.is_active ? 'badge-success' : 'badge-danger']">
            {{ p.is_active ? '活跃' : '禁用' }}
          </span>
        </div>
        <div class="project-date text-muted">创建于 {{ formatDate(p.created_at) }}</div>
      </div>
    </div>

    <!-- Create Modal -->
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <h3 class="modal-title">新建云项目</h3>
        <div class="form-group">
          <label class="form-label">项目名称</label>
          <input v-model="form.project_name" class="input" placeholder="例如：CoolApp" />
        </div>
        <div class="form-group">
          <label class="form-label">包名 (可选)</label>
          <input v-model="form.package_name" class="input" placeholder="例如：com.example.coolapp" />
        </div>
        <div class="form-group">
          <label class="form-label">描述 (可选)</label>
          <textarea v-model="form.description" class="textarea" placeholder="项目简介"></textarea>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="showCreate = false">取消</button>
          <button class="btn btn-primary" @click="createProject" :disabled="creating">
            {{ creating ? '创建中...' : '创建项目' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { projectApi } from '../api'

const showToast = inject('showToast')
const projects = ref([])
const loading = ref(true)
const showCreate = ref(false)
const creating = ref(false)
const form = ref({ project_name: '', package_name: '', description: '' })

async function loadProjects() {
  loading.value = true
  try {
    const res = await projectApi.list()
    projects.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function createProject() {
  if (!form.value.project_name) return showToast('请输入项目名称', 'error')
  creating.value = true
  try {
    await projectApi.create(form.value)
    showToast('项目已创建')
    showCreate.value = false
    form.value = { project_name: '', package_name: '', description: '' }
    loadProjects()
  } catch (e) { showToast(e?.detail || '创建失败', 'error') }
  finally { creating.value = false }
}

function formatDate(d) { return d ? new Date(d).toLocaleDateString('zh-CN') : '-' }

onMounted(loadProjects)
</script>

<style scoped>
.page-title { font-size: 22px; font-weight: 700; margin: 0; }

.empty-hero {
  text-align: center; padding: 80px 20px;
  background: var(--bg-card); border: 1px dashed var(--border);
  border-radius: var(--radius);
}
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-hero h3 { font-size: 18px; margin-bottom: 8px; }
.empty-hero p { color: var(--text-muted); font-size: 14px; max-width: 400px; margin: 0 auto 20px; }

.projects-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }

.project-card {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); padding: 20px; cursor: pointer;
  transition: var(--transition);
}
.project-card:hover { border-color: var(--accent); transform: translateY(-2px); box-shadow: 0 4px 20px rgba(99,102,241,0.1); }

.project-card-head { display: flex; align-items: center; gap: 14px; margin-bottom: 12px; }
.project-icon {
  width: 44px; height: 44px; border-radius: 12px;
  background: var(--gradient-2); display: flex;
  align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; color: white; flex-shrink: 0;
}
.project-card-head h3 { font-size: 16px; font-weight: 600; margin: 0; }
.project-desc { font-size: 13px; color: var(--text-secondary); margin-bottom: 14px; line-height: 1.5; }
.project-meta { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.project-key {
  font-size: 11px; padding: 3px 8px;
  background: var(--bg-input); border: 1px solid var(--border);
  border-radius: 4px; color: var(--accent);
}
.project-date { font-size: 12px; }
</style>
