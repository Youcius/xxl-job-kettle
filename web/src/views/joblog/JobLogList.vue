<template>
  <div>
    <div class="page-header">
      <h1><em>{{ t('joblog.title') }}</em></h1>
    </div>

    <!-- {{ t('common.search') }}工具栏 -->
    <div class="toolbar">
      <select v-model="filter.jobGroup" class="tool-select" @change="search">
        <option value="">{{ t('joblog.allGroup') }}</option>
        <option v-for="g in groups" :key="g.id" :value="g.id">{{ g.title }}</option>
      </select>
      <div class="search-box">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input v-model="filter.jobId" :placeholder="t('job.jobId')" @keyup.enter="search" />
      </div>
      <select v-model="filter.logStatus" class="tool-select" @change="search">
        <option value="">{{ t('joblog.allStatus') }}</option>
        <option :value="1">{{ t('common.success') }}</option>
        <option :value="2">{{ t('common.failed') }}</option>
        <option :value="3">{{ t('common.running') }}</option>
      </select>
      <button class="btn-new" @click="search">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        {{ t('common.search') }}
      </button>
      <button class="tool-refresh" @click="load">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
      </button>
    </div>

    <div class="glass-panel wide">
      <div v-if="loading" class="loading-state">{{ t('joblog.loading') }}</div>
      <table v-else-if="tableData.length" class="gls">
        <thead>
          <tr>
            <th>{{ t('joblog.logId') }}</th>
            <th>{{ t('job.jobId') }}</th>
            <th>{{ t('group.addressList') }}</th>
            <th>{{ t('joblog.triggerTime') }}</th>
            <th>{{ t('joblog.dispatchStatus') }}</th>
            <th>{{ t('joblog.execResult') }}</th>
            <th>{{ t('joblog.execTime') }}</th>
            <th>{{ t('common.operate') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in tableData" :key="row.id">
            <td>{{ row.id }}</td>
            <td><span class="job-id">{{ row.jobId }}</span></td>
            <td>{{ row.executorAddress }}</td>
            <td>{{ fmtTime(row.triggerTime) }}</td>
            <td>
              <span class="tag" :class="row.triggerCode === 200 ? 'ok' : 'fl'">
                {{ row.triggerCode === 200 ? t('common.success') : t('common.failed') }}
              </span>
            </td>
            <td>
              <span v-if="row.handleCode === 200 && row.triggerCode === 200" class="tag ok">{{ t('common.success') }}</span>
              <span v-else-if="row.handleCode === 0 && row.triggerCode === 200" class="status-pill running"><span class="dot"></span>{{ t('common.running') }}</span>
              <span v-else class="tag fl">{{ t('common.failed') }}</span>
            </td>
            <td>{{ fmtTime(row.handleTime) }}</td>
            <td>
              <div class="actions">
                <button class="act-btn" @click="showDetail(row.id)">
                  <svg viewBox="0 0 24 24" fill="none"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  <span class="tip">{{ t('joblog.viewDetail') }}</span>
                </button>
                <button v-if="row.handleCode === 0" class="act-btn danger" @click="handleKill(row)">
                  <svg viewBox="0 0 24 24" fill="none"><rect x="6" y="6" width="12" height="12" rx="1"/></svg>
                  <span class="tip">{{ t('joblog.killJob') }}</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <EmptyState v-else :title="t('joblog.noLogs')" :desc="t('joblog.noLogsTip')" />
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pager">
      <span class="pager-info">{{ t('common.totalItems', { count: total }) }}</span>
      <div class="pager-btns">
        <button :disabled="page <= 1" @click="page--; load()">&lt;</button>
        <button
          v-for="p in visiblePages"
          :key="p"
          :class="{ active: p === page }"
          @click="page = p; load()"
        >{{ p }}</button>
        <button :disabled="page >= totalPages" @click="page++; load()">&gt;</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getLogList, killJob } from '@/api/log'
import { getGroupList } from '@/api/group'
import EmptyState from '@/components/EmptyState.vue'
import { formatDateTime } from '@/utils/datetime'

const { t, locale } = useI18n()
const router = useRouter()
const groups = ref([])
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filter = reactive({ jobGroup: '', jobId: '', logStatus: '' })

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const visiblePages = computed(() => {
  const tp = totalPages.value
  if (tp <= 7) return Array.from({ length: tp }, (_, i) => i + 1)
  const cur = page.value
  const pages = []
  pages.push(1)
  let start = Math.max(2, cur - 2)
  let end = Math.min(tp - 1, cur + 2)
  if (cur <= 4) end = Math.min(5, tp - 1)
  if (cur >= tp - 3) start = Math.max(tp - 4, 2)
  if (start > 2) pages.push('...')
  for (let i = start; i <= end; i++) pages.push(i)
  if (end < tp - 1) pages.push('...')
  pages.push(tp)
  return pages
})

onMounted(async () => {
  const res = await getGroupList()
  groups.value = res.data?.data || []
  load()
})

async function load() {
  loading.value = true
  const params = {
    start: (page.value - 1) * pageSize.value,
    length: pageSize.value,
    jobGroup: filter.jobGroup || 0,
    jobId: filter.jobId || 0,
    logStatus: filter.logStatus || 0
  }
  const res = await getLogList(params)
  tableData.value = res.data?.data || []
  total.value = res.data?.recordsTotal || 0
  loading.value = false
}

function search() { page.value = 1; load() }

function fmtTime(t) {
  return formatDateTime(t, locale.value)
}

function showDetail(id) {
  router.push(`/joblog/${id}`)
}

async function handleKill(row) {
  await killJob(row.executorAddress, row.id)
  ElMessage.success(t('joblog.killed'))
  load()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.tool-select {
  padding: 8px 14px;
  font-family: var(--font-body);
  font-size: 12px;
  color: var(--fg);
  background: rgba(255,255,255,0.03);
  border: 1px solid var(--glass-border);
  border-radius: 10px;
  outline: none;
  cursor: pointer;
  transition: all .2s;
  min-width: 140px;
}
.tool-select:focus {
  border-color: var(--accent);
  background: rgba(124,131,255,0.06);
  box-shadow: 0 0 20px rgba(124,131,255,0.08);
}
.tool-select option {
  background: #1a1a40;
  color: var(--fg);
}
.tool-refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 10px;
  border: 1px solid var(--glass-border);
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  transition: all .2s;
  flex-shrink: 0;
}
.tool-refresh:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: rgba(124,131,255,0.08);
}
.loading-state {
  text-align: center;
  padding: 60px;
  color: var(--muted);
  font-size: 13px;
}
/* 分页 */
.pager {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}
.pager-info {
  font-size: 12px;
  color: var(--muted);
}
.pager-btns {
  display: flex;
  gap: 4px;
}
.pager-btns button {
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  font-family: var(--font-body);
  font-size: 12px;
  font-weight: 500;
  color: var(--muted);
  background: transparent;
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all .15s;
}
.pager-btns button:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: rgba(124,131,255,0.06);
}
.pager-btns button.active {
  background: rgba(124,131,255,0.15);
  border-color: rgba(124,131,255,0.2);
  color: var(--accent);
}
.pager-btns button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
</style>
