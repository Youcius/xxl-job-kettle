import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import ElementPlus from 'element-plus'
import zhCN from '@/i18n/zh-CN'
import en from '@/i18n/en'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import '@fontsource/outfit/400.css'
import '@fontsource/outfit/500.css'
import '@fontsource/outfit/600.css'
import '@fontsource/outfit/700.css'
import '@fontsource/inter/400.css'
import '@fontsource/inter/500.css'
import '@fontsource/inter/600.css'
import '@fontsource/inter/700.css'
import '@fontsource/jetbrains-mono/400.css'
import '@fontsource/jetbrains-mono/500.css'
import '@fontsource/jetbrains-mono/600.css'
import '@/assets/styles/theme.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// v-tip 指令（悬停提示，配合 .tip CSS 使用）
app.directive('tip', {
  mounted(el, binding) {
    el.setAttribute('data-tip', binding.value)
  }
})

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { size: 'default' })

// i18n
const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('xxl_lang') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: { 'zh-CN': zhCN, 'en': en },
})
app.use(i18n)
app.mount('#app')
