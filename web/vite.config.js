import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    rollupOptions: {
      onwarn(warning, warn) {
        const isVueUsePureAnnotationWarning =
          warning.id?.includes('@vueuse/core/dist/index.js') &&
          warning.message?.includes('contains an annotation that Rollup cannot interpret due to the position of the comment')

        if (isVueUsePureAnnotationWarning) {
          return
        }

        warn(warning)
      }
    }
  },
  // 生产 base 用 context-path，开发 base 用根（Vite 代理到 8081）
  base: process.env.NODE_ENV === 'production' ? '/xxl-job-admin/' : '/',
  server: {
    port: 5173,
    proxy: {
      '/xxl-job-admin': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      }
    }
  }
})
