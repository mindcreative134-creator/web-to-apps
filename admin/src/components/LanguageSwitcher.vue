<template>
  <div class="lang-switcher" :class="{ collapsed: collapsed }">
    <button class="lang-btn" @click="showMenu = !showMenu" v-click-outside="() => showMenu = false">
      <span class="lang-icon">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z"/></svg>
      </span>
      <span v-if="!collapsed" class="current-lang">{{ currentLangLabel }}</span>
    </button>
    
    <transition name="fade">
      <div v-if="showMenu" class="lang-menu" :class="{ 'menu-collapsed': collapsed, 'menu-down': direction === 'down' }">
        <button v-for="l in languages" :key="l.code" 
          class="menu-item" :class="{ active: locale === l.code }"
          @click="changeLang(l.code)">
          <span class="menu-label">{{ l.label }}</span>
          <span v-if="locale === l.code" class="check-icon">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
          </span>
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  collapsed: Boolean,
  direction: { type: String, default: 'up' } // 'up' or 'down'
})

const { locale } = useI18n()
const showMenu = ref(false)

const languages = [
  { code: 'en', label: 'English' },
  { code: 'hi', label: 'Hindi (हिन्दी)' },
]

const currentLangLabel = computed(() => {
  return languages.find(l => l.code === locale.value)?.label || 'English'
})

function changeLang(code) {
  locale.value = code
  localStorage.setItem('admin_locale', code)
  showMenu.value = false
}

// Simple directive for click outside
const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = (event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value()
      }
    }
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el) {
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}
</script>

<style scoped>
.lang-switcher { position: relative; padding: 4px 0; }
.lang-btn {
  display: flex; align-items: center; gap: 10px;
  width: 100%; padding: 10px 12px; border: none; background: none;
  color: var(--text-muted); cursor: pointer; border-radius: var(--r-sm);
  transition: all var(--t-fast); font-family: var(--font); font-size: 0.87rem;
}
.lang-btn:hover { background: rgba(255,255,255,0.04); color: var(--text-primary); }

.lang-icon { width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; opacity: 0.7; }
.current-lang { white-space: nowrap; font-weight: 500; }

.lang-menu {
  position: absolute; bottom: 100%; left: 8px; width: 180px;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--r-md); box-shadow: var(--shadow-lg);
  padding: 6px; z-index: 100; margin-bottom: 8px;
  backdrop-filter: blur(20px);
}
.lang-menu.menu-down { bottom: auto; top: 100%; margin-bottom: 0; margin-top: 8px; }
.menu-collapsed { left: 50px; bottom: 0; }
.menu-collapsed.menu-down { bottom: auto; top: 0; }

.menu-item {
  display: flex; align-items: center; justify-content: space-between;
  width: 100%; padding: 8px 10px; border: none; background: none;
  color: var(--text-secondary); cursor: pointer; border-radius: var(--r-sm);
  font-size: 0.8rem; font-family: var(--font);
}
.menu-item:hover { background: rgba(255,255,255,0.06); color: var(--text-primary); }
.menu-item.active { color: var(--accent); font-weight: 600; }
.check-icon { color: var(--accent); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s, transform 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(5px); }
</style>
