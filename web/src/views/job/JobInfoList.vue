<template>
  <div>
    <div class="page-header">
      <div>
        <h1><em>{{ t('job.title') }}</em> {{ t('job.subtitle') }}</h1>
        <div style="font-size:12px;color:var(--muted);margin-top:4px">{{ t('common.total', { count: total }) }}</div>
      </div>
      <div style="display:flex;align-items:center;gap:12px">
        <div class="search-box">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="filter.jobDesc" :placeholder="t('job.searchPlaceholder')" @input="search" />
        </div>
        <button class="btn-new" @click="showAdd">{{ t('job.newJob') }}</button>
      </div>
    </div>

    <section class="glass-panel wide">
      <table class="gls">
        <thead>
          <tr>
            <th style="width:80px">{{ t('job.jobId') }}</th>
            <th>{{ t('job.jobName') }}</th>
            <th style="width:140px">{{ t('job.cron') }}</th>
            <th style="width:100px">{{ t('job.status') }}</th>
            <th style="width:160px">{{ t('common.operate') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!tableData.length">
            <td colspan="5">
              <div class="empty-state">
                <div class="empty-icon">📋</div>
                <h3>{{ t('job.noJob') }}</h3>
                <p>{{ t('job.noJobTip') }}</p>
              </div>
            </td>
          </tr>
          <tr v-for="row in tableData" :key="row.id">
            <td><span class="job-id">#{{ row.id }}</span></td>
            <td>
              <span class="job-name">{{ row.jobDesc }}</span>
              <div class="job-desc">{{ row.author }} · {{ row.executorHandler }}</div>
            </td>
            <td>
              <div class="schedule-cell">
                <span class="schedule-text">{{ describeCron(row.scheduleConf) }}</span>
                <span class="schedule-cron-tip">{{ row.scheduleConf }}</span>
              </div>
            </td>
            <td>
              <span class="status-pill" :class="row.triggerStatus === 1 ? 'running' : 'paused'">
                <span class="dot"></span>{{ row.triggerStatus === 1 ? t('common.running') : t('common.stopped') }}
              </span>
            </td>
            <td>
              <div class="actions">
                <button class="act-btn" @click="showEdit(row)" v-tip="t('common.edit')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
                  <span class="tip">{{ t('common.edit') }}</span>
                </button>
                <button class="act-btn" v-if="row.triggerStatus === 1" @click="handleStop(row.id)" v-tip="t('job.stop')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16" rx="1"/><rect x="14" y="4" width="4" height="16" rx="1"/></svg>
                  <span class="tip">{{ t('job.stop') }}</span>
                </button>
                <button class="act-btn primary" v-else @click="handleStart(row.id)" v-tip="t('job.start')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polygon points="6 3 20 12 6 21 6 3"/></svg>
                  <span class="tip">{{ t('job.start') }}</span>
                </button>
                <button class="act-btn primary" @click="handleTrigger(row.id)" v-tip="t('job.trigger')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                  <span class="tip">{{ t('job.trigger') }}</span>
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
    </section>

    <!-- 弹窗 -->
    <div class="modal-overlay" :class="{ open: dialog.visible }" @click.self="dialog.visible = false">
      <div class="modal">
        <h3>{{ dialog.isEdit ? t('job.editJob') : t('job.newJobTitle') }}</h3>
        <div class="modal-sub">{{ t('job.configParams') }}</div>
        <div class="form-grid">
          <div class="form-group"><label>{{ t('job.taskName') }} *</label><input v-model="dialog.form.jobDesc" :placeholder="t('job.taskName')" maxlength="64"></div>
          <div class="form-group"><label>{{ t('job.author') }}</label><input v-model="dialog.form.author" :placeholder="t('job.author')" maxlength="32"></div>
          <div class="form-group full"><label>{{ t('job.cronExpr') }}</label>
            <div class="cron-editor">
              <div class="field-row">
                <div><div class="field-label">{{ t('common2.second') }}</div><input class="field-input" v-model="cronFields.sec"></div>
                <div><div class="field-label">{{ t('common2.minute') }}</div><input class="field-input" v-model="cronFields.min"></div>
                <div><div class="field-label">{{ t('common2.hour') }}</div><input class="field-input" v-model="cronFields.hour"></div>
                <div><div class="field-label">{{ t('common2.day') }}</div><input class="field-input" v-model="cronFields.day"></div>
                <div><div class="field-label">{{ t('common2.month') }}</div><input class="field-input" v-model="cronFields.month"></div>
                <div><div class="field-label">{{ t('common2.week') }}</div><input class="field-input" v-model="cronFields.week"></div>
              </div>
              <div class="preset-row">
                <button v-for="p in presets" :key="p.cron" class="preset-chip" :class="{ on: cronExpr === p.cron }" @click="applyCron(p.cron)">{{ p.label }}</button>
              </div>
              <div class="nl-preview">📅 {{ cronDesc }}</div>
            </div>
          </div>
          <div class="form-group"><label>{{ t('job.executor') }}</label><select v-model="dialog.form.jobGroup"><option v-for="g in groups" :key="g.id" :value="g.id">{{ g.title }}</option></select></div>
          <div class="form-group"><label>{{ t('job.routeStrategy') }}</label><select v-model="dialog.form.executorRouteStrategy"><option>FIRST</option><option>LAST</option><option>ROUND</option><option>RANDOM</option><option>CONSISTENT_HASH</option><option>FAILOVER</option></select></div>
          <div class="form-group"><label>{{ t('job.blockStrategy') }}</label><select v-model="dialog.form.executorBlockStrategy"><option>SERIAL_EXECUTION</option><option>DISCARD_LATER</option><option>COVER_EARLY</option></select></div>
          <div class="form-group"><label>{{ t('job.timeout') }}</label><input v-model="dialog.form.executorTimeout" type="number" value="0" :placeholder="'0'"></div>
          <div class="form-group"><label>{{ t('job.executorHandler') }} *</label><input v-model="dialog.form.executorHandler" :placeholder="t('job.executorHandlerPlaceholder')" maxlength="255"></div>
          <div class="form-group full"><label>{{ t('job.param') }}</label><textarea v-model="dialog.form.executorParam" :placeholder="t('job.param')" rows="2"></textarea></div>
          <div class="form-group"><label>{{ t('job.alarmEmail') }}</label><input v-model="dialog.form.alarmEmail" placeholder="user@company.com" maxlength="128"></div>
          <div class="form-group"><label>{{ t('job.childJob') }}</label><input v-model="dialog.form.childJobId" :placeholder="t('job.childJob')" maxlength="64"></div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="dialog.visible = false">{{ t('common.cancel') }}</button>
          <button class="btn-confirm" @click="saveJob">{{ t('job.saveJob') }}</button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div class="modal-overlay" :class="{ open: deleteModal.visible }" @click.self="deleteModal.visible = false">
      <div class="confirm-modal">
        <div class="warn-icon">!</div>
        <h3>{{ t('common.confirmDelete') }}</h3>
        <div class="target-info">Job ID: #{{ deleteModal.id }}</div>
        <p style="font-size:12px;color:var(--muted);margin-bottom:24px">{{ t('common.deleteConfirm') }}</p>
        <div class="modal-actions" style="justify-content:center">
          <button class="btn-cancel" @click="deleteModal.visible = false">{{ t('common.cancel') }}</button>
          <button class="btn-danger" @click="confirmDelete">{{ t('common.confirmDelete') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getJobList, addJob, updateJob, removeJob, startJob as startJobApi, stopJob as stopJobApi, triggerJob } from '@/api/job'
import { getGroupList } from '@/api/group'

const { t } = useI18n()
const groups = ref([])
const tableData = ref([])
const total = ref(0)

const filter = reactive({ jobDesc: '' })

const dialog = reactive({
  visible: false,
  isEdit: false,
  form: {}
})

const deleteModal = reactive({ visible: false, id: null })

const cronFields = reactive({ sec: '0', min: '*/1', hour: '*', day: '*', month: '*', week: '?' })

const cronExpr = computed(() =>
  `${cronFields.sec} ${cronFields.min} ${cronFields.hour} ${cronFields.day} ${cronFields.month} ${cronFields.week}`
)

const cronDesc = computed(() => describeCron(cronExpr.value))

const presets = computed(() => [
  { label: t('job.cronPresets.everyMin'), cron: '0 */1 * * * ?' },
  { label: t('job.cronPresets.every5Min'), cron: '0 */5 * * * ?' },
  { label: t('job.cronPresets.every10Min'), cron: '0 */10 * * * ?' },
  { label: t('job.cronPresets.everyHour'), cron: '0 0 * * * ?' },
  { label: t('job.cronPresets.at2am'), cron: '0 0 2 * * ?' },
  { label: t('job.cronPresets.at7am'), cron: '0 0 7 * * ?' },
  { label: t('job.cronPresets.workday'), cron: '0 30 9 * * 1-5' },
  { label: t('job.cronPresets.midnight'), cron: '0 0 0 * * ?' },
])

function applyCron(cron) {
  const s = cron.split(/\s+/)
  cronFields.sec = s[0]; cronFields.min = s[1]; cronFields.hour = s[2]
  cronFields.day = s[3]; cronFields.month = s[4]; cronFields.week = s[5]
}

function describeCron(cron) {
  const s = cron.split(/\s+/)
  if (s.length < 6) return t('job.invalidCron')
  if (s[1] === '*/1' && s[0] === '0') return t('job.cronPresets.everyMin')
  if (s[1].startsWith('*/')) {
    const m = parseInt(s[1].split('/')[1])
    return t('job.cronPresets.everyMin') + ' x' + m
  }
  if (s[1] === '0' && s[0] === '0' && s[2] === '*') return t('job.cronPresets.everyHour')
  if (s[1] === '0' && s[2] !== '*') return s[2] + ':' + s[1].padStart(2,'0')
  return cron
}

onMounted(async () => {
  try {
    const gRes = await getGroupList()
    groups.value = gRes.data?.data || []
  } catch (_) {}
  load()
})

async function load() {
  try {
    const p = { start: 0, length: 200, jobGroup: -1, triggerStatus: -1, jobDesc: filter.jobDesc || '', executorHandler: '', author: '' }
    const res = await getJobList(p)
    tableData.value = res.data?.data || []
    total.value = tableData.value.length
  } catch (_) {}
}

function search() { load() }

function buildJobForm(source = {}) {
  return {
    id: source.id,
    jobGroup: source.jobGroup || groups.value[0]?.id || 1,
    jobDesc: source.jobDesc || '',
    author: source.author || 'admin',
    scheduleType: source.scheduleType || 'CRON',
    scheduleConf: source.scheduleConf || '0 */1 * * * ?',
    executorRouteStrategy: source.executorRouteStrategy || 'FIRST',
    executorBlockStrategy: source.executorBlockStrategy || 'SERIAL_EXECUTION',
    executorTimeout: source.executorTimeout ?? '0',
    executorFailRetryCount: source.executorFailRetryCount ?? '0',
    executorParam: source.executorParam || '',
    alarmEmail: source.alarmEmail || '',
    childJobId: source.childJobId || '',
    glueType: source.glueType || 'BEAN',
    executorHandler: source.executorHandler || '',
    misfireStrategy: source.misfireStrategy || 'DO_NOTHING'
  }
}

function showAdd() {
  dialog.isEdit = false
  dialog.form = buildJobForm()
  applyCron('0 */1 * * * ?')
  dialog.visible = true
}

function showEdit(row) {
  dialog.isEdit = true
  dialog.form = buildJobForm(row)
  if (row.scheduleConf) {
    try { applyCron(row.scheduleConf) } catch (_) { applyCron('0 */1 * * * ?') }
  }
  dialog.visible = true
}

async function saveJob() {
  dialog.form.scheduleConf = cronExpr.value
  if (!dialog.form.executorHandler || !dialog.form.executorHandler.trim()) {
    ElMessage.error(t('job.executorHandlerRequired'))
    return
  }
  dialog.form.executorHandler = dialog.form.executorHandler.trim()
  try {
    const apiFn = dialog.isEdit ? updateJob : addJob
    await apiFn(buildJobForm(dialog.form))
    ElMessage({ message: t('job.saved'), type: 'success' })
    dialog.visible = false
    load()
  } catch (_) {}
}

async function handleStart(id) { await startJobApi(id); ElMessage.success(t('job.started')); load() }
async function handleStop(id) { await stopJobApi(id); ElMessage.success(t('common.stopped')); load() }
async function handleTrigger(id) { await triggerJob({ id }); ElMessage.success(t('job.triggered')); }

function handleRemove(id) { deleteModal.id = id; deleteModal.visible = true }

async function confirmDelete() {
  try {
    await removeJob(deleteModal.id)
    ElMessage.success(t('job.deleted'))
    deleteModal.visible = false
    load()
  } catch (_) {}
}
</script>
