<template>
  <div>
    <el-page-header @back="$router.push('/joblog')" title="返回" content="日志详情"/>
    <el-card shadow="never" style="margin-top:16px">
      <div class="log-console" ref="logRef">
        <pre v-if="logText">{{ logText }}</pre>
        <div v-else class="empty">加载中...</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getLogDetail } from '@/api/log'

const route = useRoute()
const logText = ref('')
const logRef = ref()

onMounted(async () => {
  const id = route.params.id
  const res = await getLogDetail({ logId: id, fromLineNum: 0 })
  if (res.data?.content) {
    logText.value = res.data.content
    await nextTick()
    if (logRef.value) {
      logRef.value.scrollTop = logRef.value.scrollHeight
    }
  }
})
</script>

<style scoped>
.log-console {
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: 'DejaVu Sans Mono', 'Courier New', monospace;
  font-size: 13px;
  padding: 16px;
  border-radius: 6px;
  min-height: 400px;
  max-height: 600px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
.log-console pre { margin: 0; }
.empty { color: #888; text-align: center; padding-top: 160px; }
</style>
