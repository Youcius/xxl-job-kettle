<template>
  <div>
    <div class="page-header">
      <h1><em>{{ t('dashboard.title') }}</em> {{ t('dashboard.subtitle') }}</h1>
    </div>

    <div class="stat-row">
      <div class="stat-card">
        <div class="num" style="color:var(--accent)">{{ stats.taskCount }}</div>
        <div class="lbl">{{ t('dashboard.taskCount') }}</div>
        <div class="delta" style="color:var(--accent)">+0 {{ t('dashboard.thisWeek') }}</div>
      </div>
      <div class="stat-card">
        <div class="num" style="color:var(--success)">{{ stats.runningCount }}</div>
        <div class="lbl">{{ t('dashboard.runningCount') }}</div>
        <div class="delta" style="color:var(--success)">—</div>
      </div>
      <div class="stat-card">
        <div class="num" style="color:var(--warn)">{{ stats.taskCount - stats.runningCount }}</div>
        <div class="lbl">{{ t('dashboard.pausedCount') }}</div>
        <div class="delta" style="color:var(--muted)">—</div>
      </div>
      <div class="stat-card">
        <div class="num" style="color:var(--danger)">{{ stats.failCount }}</div>
        <div class="lbl">{{ t('dashboard.todayFail') }}</div>
        <div class="delta" style="color:var(--danger)">—</div>
      </div>
      <div class="stat-card">
        <div class="num" style="color:var(--accent2)">{{ stats.executorOnline }}</div>
        <div class="lbl">{{ t('dashboard.executorOnline') }}</div>
        <div class="delta" style="color:var(--accent2)">{{ executors.length > 0 ? t('dashboard.onlineCount', { count: onlineExecutorCount }) : '—' }}</div>
      </div>
    </div>

    <div class="grid2">
      <section class="glass-panel">
        <div class="panel-hd"><span>{{ t('dashboard.recentLogs') }}</span><span class="sub">{{ t('dashboard.realtime') }}</span></div>
        <table class="gls" style="flex:1">
          <thead><tr><th>{{ t('job.jobId') }}</th><th>{{ t('job.jobName') }}</th><th>{{ t('joblog.triggerTime') }}</th><th>{{ t('joblog.execResult') }}</th><th>{{ t('common.duration') }}</th></tr></thead>
          <tbody>
            <tr v-if="!recentLogs.length">
              <td colspan="5">
                <div class="empty-state" style="padding:48px">
                  <div class="empty-icon">📋</div>
                  <p>{{ t('dashboard.noLogs') }}</p>
                </div>
              </td>
            </tr>
            <tr v-for="log in recentLogs" :key="log.id">
              <td><span class="job-id">#{{ log.id }}</span></td>
              <td><span class="job-name">{{ log.jobId }}</span></td>
              <td class="dur">{{ fmt(log.triggerTime) }}</td>
              <td><span class="tag" :class="log.handleCode === 200 ? 'ok' : 'fl'">{{ log.handleCode === 200 ? t('common.success') : t('common.failed') }}</span></td>
              <td class="dur">—</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="glass-panel">
        <div class="panel-hd"><span>{{ t('dashboard.executorStatus') }}</span></div>
        <div v-if="!executors.length" style="flex:1;display:flex;align-items:center;justify-content:center;color:var(--muted)">
          <p style="font-size:13px">{{ t('dashboard.noExecutor') }}</p>
        </div>
        <div v-else style="padding:16px;overflow-y:auto;flex:1">
          <div v-for="ex in executors" :key="ex.id" style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--glass-border)">
            <div>
              <div style="font-size:13px;font-weight:600">{{ ex.title }}</div>
              <div style="font-size:11px;color:var(--muted);margin-top:2px">{{ ex.appname }}</div>
            </div>
            <div style="text-align:right">
              <span :class="ex.registryList && ex.registryList.length ? 'status-pill running' : 'status-pill paused'">
                <span class="dot"></span>{{ ex.registryList && ex.registryList.length ? t('common.online') : t('common.offline') }}
              </span>
              <div v-if="ex.registryList" style="font-size:10px;color:var(--muted);margin-top:4px;font-family:var(--font-mono)">{{ ex.registryList[0] }}</div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import request from '@/api/request'
import { formatDateTime } from '@/utils/datetime'

const { t, locale } = useI18n()

const stats = reactive({ taskCount: 0, runningCount: 0, sucCount: 0, failCount: 0, executorOnline: 0 })
const recentLogs = ref([])
const executors = ref([])
const onlineExecutorCount = computed(() => executors.value.filter(executor => executor.registryList?.length).length)

onMounted(async () => {
  try {
    const res = await request.get('/dashboardInfo')
    if (res.data?.code === 200) {
      const d = res.data.content
      stats.taskCount = d.jobInfoCount || 0
      stats.executorOnline = d.executorCount || 0
      stats.failCount = d.jobLogCount - d.jobLogSuccessCount
    }
  } catch (_) { /* */ }

  // 运行中 / 已暂停 数量
  try {
    const jobRes = await request.post('/jobinfo/pageList', null, {
      params: { start: 0, length: 999, jobGroup: -1, triggerStatus: -1, jobDesc: '', executorHandler: '', author: '' }
    })
    if (jobRes.data?.data) {
      stats.runningCount = jobRes.data.data.filter(j => j.triggerStatus === 1).length
    }
  } catch (_) { /* */ }

  // 最近调度日志
  try {
    const logRes = await request.post('/joblog/pageList', null, {
      params: { start: 0, length: 5, jobGroup: 0, jobId: 0, logStatus: -1 }
    })
    if (logRes.data?.data) {
      recentLogs.value = logRes.data.data.slice(0, 5)
    }
  } catch (_) { /* */ }

  // 执行器状态
  try {
    const exRes = await request.post('/jobgroup/pageList', null, {
      params: { start: 0, length: 200, appname: '', title: '' }
    })
    if (exRes.data?.data) {
      executors.value = exRes.data.data
    }
  } catch (_) { /* */ }
})

function fmt(t) {
  return formatDateTime(t, locale.value)
}
</script>
