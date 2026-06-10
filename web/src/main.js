import { createApp } from 'vue'
import { createPinia } from 'pinia'
import i18n from '@/i18n'
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

app.use(createPinia())
app.use(router)

app.use(i18n)
app.mount('#app')
