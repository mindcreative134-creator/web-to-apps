import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import zh from './locales/zh.json'
import hi from './locales/hi.json'

const messages = { en, zh, hi }

// Get persisted language or default to 'en'
const savedLoc = localStorage.getItem('admin_locale')
// Always prefer English unless the user explicitly saved 'zh' or if the browser is strictly Chinese and no preference exists
const locale = savedLoc || (navigator.language.startsWith('zh') ? 'en' : 'en') // Defaulting to EN for now to ensure English first experience

const i18n = createI18n({
  legacy: false,
  locale: locale || 'en',
  fallbackLocale: 'en',
  messages,
})

export default i18n
