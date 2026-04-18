<template>
  <div>
    <div class="flex items-center justify-between mb-20">
      <h2 class="page-title">☁️ {{ $t('projects.title') }}</h2>
      <button class="btn btn-primary" @click="showCreate = true">+ {{ $t('projects.modal.btn_create') }}</button>
    </div>

    <div v-if="loading" class="spinner"></div>

    <div v-else-if="!projects.length" class="empty-hero">
      <div class="empty-icon">☁️</div>
      <h3>{{ $t('projects.no_projects') }}</h3>
      <p>{{ $t('projects.description_hint') }}</p>
      <button class="btn btn-primary" @click="showCreate = true">{{ $t('projects.btn_create_first') }}</button>
    </div>

    <div v-else class="projects-grid">
      <div v-for="p in projects" :key="p.id" class="project-card" @click="$router.push(`/projects/${p.id}`)">
        <div class="project-card-head">
          <div class="project-icon">{{ p.project_name[0]?.toUpperCase() }}</div>
          <div>
            <h3>{{ p.project_name }}</h3>
            <span class="text-muted" style="font-size:12px">{{ p.package_name || $t('projects.no_package') }}</span>
          </div>
        </div>
        <p class="project-desc">{{ p.description || $t('projects.no_desc') }}</p>
        <div class="project-meta">
          <code class="project-key">{{ p.project_key }}</code>
          <span :class="['badge', p.is_active ? 'badge-success' : 'badge-danger']">
            {{ p.is_active ? $t('common.active') : $t('common.inactive') }}
          </span>
        </div>
        <div class="project-date text-muted">{{ $t('projects.table.created') }} {{ formatDate(p.created_at) }}</div>
      </div>
    </div>

    <!-- Create Modal -->
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <h3 class="modal-title">{{ $t('projects.modal.title') }}</h3>
        <div class="form-group">
          <label class="form-label">{{ $t('projects.modal.name') }}</label>
          <input v-model="form.project_name" class="input" :placeholder="$t('projects.modal.name')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('projects.modal.package') }}</label>
          <input v-model="form.package_name" class="input" :placeholder="$t('projects.modal.package')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('projects.modal.desc') }}</label>
          <textarea v-model="form.description" class="textarea" :placeholder="$t('projects.modal.desc')"></textarea>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="showCreate = false">{{ $t('common.cancel') }}</button>
          <button class="btn btn-primary" @click="createProject" :disabled="creating">
            {{ creating ? $t('projects.modal.creating') : $t('projects.modal.btn_create') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { projectApi } from '../api'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
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
  } catch (e) { 
    console.error(e)
    showToast(t('common.loading') + ' ' + t('common.failed'), 'error')
  }
  finally { loading.value = false }
}

async function createProject() {
  if (!form.value.project_name) return showToast(t('projects.modal.name') + '?', 'error')
  creating.value = true
  try {
    await projectApi.create(form.value)
    showToast(t('common.active'))
    showCreate.value = false
    form.value = { project_name: '', package_name: '', description: '' }
    loadProjects()
  } catch (e) { showToast(e?.detail || t('common.failed'), 'error') }
  finally { creating.value = false }
}

const dateLocaleMap = { en: 'en-US', zh: 'zh-CN', hi: 'hi-IN' }
function formatDate(d) { return d ? new Date(d).toLocaleDateString(dateLocaleMap[locale.value] || 'en-US') : '-' }

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
