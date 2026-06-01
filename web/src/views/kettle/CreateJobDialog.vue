<template>
  <el-dialog v-model="visible" title="一键创建 Kettle 调度任务" width="560px" destroy-on-close>
    <el-form :model="form" label-width="120px">
      <el-alert
        :title="`将基于文件 «${props.kettleFile?.fileName || ''}» 创建 Shell 任务`"
        type="info" :closable="false" show-icon style="margin-bottom:16px"
      />
      <el-form-item label="任务描述">
        <el-input v-model="form.jobDesc"/>
      </el-form-item>
      <el-form-item label="执行器">
        <el-select v-model="form.jobGroup" style="width:100%">
          <el-option v-for="g in groups" :key="g.id" :label="g.title" :value="g.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="负责人">
        <el-input v-model="form.author"/>
      </el-form-item>
      <el-form-item label="Cron 表达式" required>
        <el-input v-model="form.scheduleConf" placeholder="0 0 2 * * ?"/>
        <div class="cron-hint">每天凌晨2点: 0 0 2 * * ? | 每小时: 0 0 * * * ?</div>
      </el-form-item>
      <el-form-item label="运行参数">
        <el-input v-model="form.executorParam" placeholder='如: -param:DB_HOST=192.168.1.1 -param:DB_PORT=3306'/>
        <div class="cron-hint">可选，将传给 Kettle 的 -param 参数</div>
      </el-form-item>
      <el-form-item label="报警邮件">
        <el-input v-model="form.alarmEmail" placeholder="admin@example.com"/>
      </el-form-item>
      <el-form-item label="失败重试">
        <el-input-number v-model="form.executorFailRetryCount" :min="0" :max="10"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleCreate">创建任务</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createKettleJob } from '@/api/kettle'
import { getGroupList } from '@/api/group'

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
      ElMessage.success('任务创建成功')
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
