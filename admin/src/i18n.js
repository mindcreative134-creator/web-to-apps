import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import hi from './locales/hi.json'

const messages = { en, hi }

// Get persisted language or default to 'en'
const savedLoc = localStorage.getItem('admin_locale')
// Always default to English for a professional experience if not set to a supported locale
const locale = (savedLoc === 'en' || savedLoc === 'hi') ? savedLoc : 'en'

const i18n = createI18n({
  legacy: false,
  locale: locale || 'en',
  fallbackLocale: 'en',
  messages,
})

export default i18n
