<template>
  <div>
    <div class="page-header">
      <div>
        <h1><em>{{ t('kettle.title') }}</em> {{ t('kettle.file') }}</h1>
        <div style="font-size:12px;color:var(--muted);margin-top:4px">
          <a href="#" @click.prevent="$router.push('/kettle/group')" style="color:var(--accent);text-decoration:none">{{ t('kettle.backGroup') }}</a>
        </div>
      </div>
      <div style="display:flex;align-items:center;gap:8px">
        <div class="search-box"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg><input v-model="keyword" :placeholder="t('common.search')" @input="load" /></div>
        <select v-model="fileType" @change="load" style="padding:8px 12px;background:rgba(255,255,255,0.04);border:1px solid var(--glass-border);border-radius:10px;color:var(--fg);outline:none;font-family:var(--font-body);font-size:13px">
          <option value="">{{ t('kettle.allFileTypes') }}</option><option value="KTR">KTR</option><option value="KJB">KJB</option>
        </select>
        <button class="btn-new" @click="triggerUpload">{{ t('kettle.uploadFile') }}</button>
      </div>
    </div>

    <section class="glass-panel wide">
      <table class="gls">
        <thead>
          <tr><th style="width:60px">ID</th><th>{{ t('kettle.fileName') }}</th><th style="width:80px">{{ t('kettle.fileType') }}</th><th style="width:80px">{{ t('kettle.fileSize') }}</th><th style="width:60px">{{ t('kettle.version') }}</th><th style="width:80px">{{ t('kettle.relatedJob') }}</th><th style="width:160px">{{ t('kettle.uploadTime') }}</th><th style="width:280px">{{ t('common.operate') }}</th></tr>
        </thead>
        <tbody>
          <tr v-if="!tableData.length">
            <td colspan="8">
              <div class="empty-state">
                <div class="empty-icon">📄</div>
                <h3>{{ t('kettle.noFile') }}</h3>
                 <p>{{ t('kettle.noFileTip') }}</p>
              </div>
            </td>
          </tr>
          <tr v-for="row in tableData" :key="row.id">
            <td><span class="job-id">#{{ row.id }}</span></td>
            <td><span class="job-name">{{ row.fileName }}</span></td>
            <td><span class="tag" :class="row.fileType === 'KTR' ? 'ok' : ''" style="font-size:10px">{{ row.fileType }}</span></td>
            <td class="dur">{{ fmtSize(row.fileSize) }}</td>
            <td><span style="font-family:var(--font-mono);font-size:14px;color:var(--accent);font-weight:600">v{{ row.version }}</span></td>
            <td><span class="job-id" v-if="row.jobId">#{{ row.jobId }}</span><span v-else style="color:var(--muted);font-size:12px">—</span></td>
            <td class="dur">{{ fmt(row.addTime) }}</td>
            <td>
              <div class="actions">
                <button class="act-btn primary" @click="openCreateJob(row)" v-tip="t('kettle.createJob')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  <span class="tip">{{ t('kettle.createJob') }}</span>
                </button>
                <button class="act-btn" @click="handleDownload(row.id)" v-tip="t('common.download')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                  <span class="tip">{{ t('common.download') }}</span>
                </button>
                <button class="act-btn danger" @click="handleRemove(row.id)" v-tip="t('common.delete')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
                  <span class="tip">{{ t('common.delete') }}</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <input type="file" ref="fileInput" multiple accept=".ktr,.kjb" style="display:none" @change="handleUpload" />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getFileList, uploadFile, deleteFile, downloadFile, createKettleJob } from '@/api/kettle'
import { formatDateTime } from '@/utils/datetime'

const { t, locale } = useI18n()
const route = useRoute()
const tableData = ref([])
const keyword = ref('')
const fileType = ref('')
const fileInput = ref(null)

onMounted(() => load())

async function load() {
  const groupId = route.params.groupId
  try {
    const res = await getFileList({ start: 0, length: 200, groupId, keyword: keyword.value, fileType: fileType.value })
    tableData.value = res.data?.content?.data || []
  } catch (_) {}
}

function triggerUpload() { fileInput.value?.click() }

async function handleUpload(e) {
  const files = e.target.files
  if (!files.length) return
  const groupId = route.params.groupId
  let ok = 0
  for (const f of files) {
    try {
      const fd = new FormData(); fd.append('file', f); fd.append('groupId', groupId)
      await uploadFile(fd)
      ok++
    } catch (_) {}
  }
  ElMessage.success(t('kettle.uploadResult', { ok, total: files.length }))
  e.target.value = ''
  load()
}

function handleDownload(id) { window.open(`/xxl-job-admin/api/kettle/file/download?id=${id}`) }

async function handleRemove(id) {
  try { await deleteFile(id); ElMessage.success(t('job.deleted')); load() } catch (_) {}
}

function fmtSize(b) { if (!b) return '0 B'; return b < 1024 ? b+' B' : b < 1048576 ? (b/1024).toFixed(1)+' KB' : (b/1048576).toFixed(1)+' MB' }
function fmt(t) { return formatDateTime(t, locale.value) }
function buildJobName(fileName = '') {
  const baseName = fileName.replace(/\.[^.]+$/, '').trim()
  return baseName ? `${baseName}-job` : 'kettle-job'
}
async function openCreateJob(row) {
  try {
    await createKettleJob({
      fileId: row.id,
      jobDesc: buildJobName(row.fileName),
      author: 'admin',
      scheduleType: 'CRON',
      scheduleConf: '0 */5 * * * ?',
      executorRouteStrategy: 'FIRST',
      executorBlockStrategy: 'SERIAL_EXECUTION',
      executorTimeout: 0,
      executorFailRetryCount: 0,
      misfireStrategy: 'DO_NOTHING'
    })
    ElMessage.success(t('kettle.jobCreated'))
    load()
  } catch (_) {
    ElMessage.error(t('kettle.jobFailed'))
  }
}
</script>
