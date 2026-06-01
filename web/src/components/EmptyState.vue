<template>
  <div class="empty-state">
    <svg class="empty-icon" viewBox="0 0 80 60" xmlns="http://www.w3.org/2000/svg">
      <!-- 盒子 -->
      <rect x="14" y="24" width="52" height="32" rx="4" :stroke="accent" stroke-width="1.5" fill="none"/>
      <!-- 盒盖 -->
      <path :d="boxLid" :stroke="accent" stroke-width="1.5" fill="none" stroke-linecap="round"/>
      <!-- 星光1 -->
      <circle cx="68" cy="8" r="1.5" :fill="accent" opacity="0.6"/>
      <!-- 星光2 -->
      <circle cx="72" cy="18" r="1" :fill="accent" opacity="0.4"/>
      <!-- 盒内纸张 -->
      <rect x="28" y="32" width="24" height="16" rx="2" :fill="accent" opacity="0.1"/>
      <line x1="32" y1="37" x2="48" y2="37" :stroke="accent" stroke-width="0.8" opacity="0.4"/>
      <line x1="32" y1="42" x2="44" y2="42" :stroke="accent" stroke-width="0.8" opacity="0.4"/>
    </svg>
    <p class="empty-title">{{ title }}</p>
    <p class="empty-desc">{{ desc }}</p>
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '暂无数据' },
  desc: { type: String, default: '这里还没有内容，去创建一个吧' },
  type: { type: String, default: 'default' } // default | upload | task
})

const accent = '#4a6fa5'

const boxLid = computed(() => {
  if (props.type === 'upload') {
    // 开盖状态 — 盖子飘起
    return 'M10 24 L40 12 L70 24'
  }
  // 关盖
  return 'M10 24 L40 18 L70 24'
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 20px;
}
.empty-icon {
  width: 80px;
  height: 60px;
  margin-bottom: 16px;
}
.empty-title {
  font-size: 15px;
  color: var(--text-primary);
  margin: 0 0 8px;
  font-weight: 600;
}
.empty-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 20px;
  word-break: keep-all;
  overflow-wrap: normal;
}
</style>
