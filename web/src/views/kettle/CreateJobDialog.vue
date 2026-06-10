<template>
  <el-dialog v-model="visible" :title="t('kettle.createJobDialogTitle')" width="560px" destroy-on-close>
    <el-form :model="form" label-width="120px">
      <el-alert
        :title="t('kettle.createJobAlert', { fileName: props.kettleFile?.fileName || '' })"
        type="info" :closable="false" show-icon style="margin-bottom:16px"
      />
      <el-form-item :label="t('job.taskName')">
        <el-input v-model="form.jobDesc"/>
      </el-form-item>
      <el-form-item :label="t('job.executor')">
        <el-select v-model="form.jobGroup" style="width:100%">
          <el-option v-for="g in groups" :key="g.id" :label="g.title" :value="g.id"/>
        </el-select>
      </el-form-item>
      <el-form-item :label="t('job.author')">
        <el-input v-model="form.author"/>
      </el-form-item>
      <el-form-item :label="t('job.cronExpr')" required>
        <el-input v-model="form.scheduleConf" placeholder="0 0 2 * * ?"/>
        <div class="cron-hint">{{ t('kettle.cronHint') }}</div>
      </el-form-item>
      <el-form-item :label="t('job.param')">
        <el-input v-model="form.executorParam" :placeholder="t('kettle.paramPlaceholder')"/>
        <div class="cron-hint">{{ t('kettle.paramHint') }}</div>
      </el-form-item>
      <el-form-item :label="t('job.alarmEmail')">
        <el-input v-model="form.alarmEmail" placeholder="admin@example.com"/>
      </el-form-item>
      <el-form-item :label="t('kettle.retryCount')">
        <el-input-number v-model="form.executorFailRetryCount" :min="0" :max="10"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="handleCreate">{{ t('kettle.createJob') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { createKettleJob } from '@/api/kettle'
import { getGroupList } from '@/api/group'

const { t } = useI18n()

const props = defineProps({
  kettleFile: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['success'])

const visible = ref(false)
const submitting = ref(false)
const groups = ref([])

const form = reactive({
  jobGroup: 1,
  jobDesc: '',
  author: 'admin',
  scheduleConf: '0 0 2 * * ?',
  executorParam: '',
  alarmEmail: '',
  executorFailRetryCount: 0,
  executorTimeout: 0,
  childJobId: ''
})

watch(() => props.kettleFile, (file) => {
  if (file) {
    form.jobDesc = file.fileName || ''
  }
})

onMounted(async () => {
  const res = await getGroupList()
  groups.value = res.data?.data || []
})

function open() {
  visible.value = true
}

async function handleCreate() {
  submitting.value = true
  try {
    const res = await createKettleJob({
      kettleFileId: props.kettleFile?.id,
      ...form
    })
    if (res.data?.code === 200) {
      ElMessage.success(t('kettle.jobCreated'))
      visible.value = false
      emit('success')
    }
  } catch (_) {
    /* error handled by interceptor */
  }
  submitting.value = false
}

defineExpose({ open })
</script>

<style scoped>
.cron-hint {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 4px;
}
</style>
