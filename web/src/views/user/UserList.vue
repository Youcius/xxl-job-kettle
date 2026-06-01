<template>
  <div>
    <div class="page-header">
      <h1><em>{{ t('user.title') }}</em></h1>
      <button class="btn-new" @click="showAdd">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        {{ t('user.newUserTitle') }}
      </button>
    </div>

    <div class="glass-panel wide">
      <div v-if="loading" class="loading-state">{{ t('joblog.loading') }}</div>
      <table v-else-if="tableData.length" class="gls">
        <thead>
          <tr>
            <th>ID</th>
            <th>{{ t('user.username') }}</th>
            <th>{{ t('user.role') }}</th>
            <th>{{ t('user.permission') }}</th>
            <th>{{ t('common.operate') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in tableData" :key="row.id">
            <td>{{ row.id }}</td>
            <td><span class="job-name">{{ row.username }}</span></td>
            <td>
              <span class="status-pill" :class="row.role === 1 ? 'failed' : 'running'">
                <span class="dot"></span>{{ row.role === 1 ? t('user.admin') : t('user.normal') }}
              </span>
            </td>
            <td>{{ row.permission || t('user.all') }}</td>
            <td>
              <div class="actions">
                <button class="act-btn" @click="showEdit(row)">
                  <svg viewBox="0 0 24 24" fill="none"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  <span class="tip">{{ t('common.edit') }}</span>
                </button>
                <button class="act-btn danger" @click="handleRemove(row.id)">
                  <svg viewBox="0 0 24 24" fill="none"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                  <span class="tip">{{ t('common.delete') }}</span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <EmptyState v-else :title="t('user.noUser')" :desc="t('user.noUserTip')" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <div class="modal-overlay" :class="{ open: dialog.visible }" @click.self="dialog.visible = false">
      <div class="modal" @click.stop>
        <h3>{{ dialog.isEdit ? t('user.editUser') : t('user.newUserTitle') }}</h3>
        <p class="modal-sub">{{ dialog.isEdit ? t('user.editUserDesc') : t('user.newUserDesc') }}</p>
        <div class="form-grid">
          <div class="form-group">
            <label>{{ t('user.username') }} <span class="req">*</span></label>
            <input v-model="dialog.form.username" :placeholder="t('user.username')" />
          </div>
          <div class="form-group">
            <label>{{ t('user.password') }} <span class="req">*</span></label>
            <input v-model="dialog.form.password" type="password" :placeholder="dialog.isEdit ? t('user.leaveBlank') : t('user.passwordPlaceholder')" />
          </div>
          <div class="form-group full">
            <label>{{ t('user.role') }} <span class="req">*</span></label>
            <div class="radio-group">
              <label class="radio-row">
                <input type="radio" v-model.number="dialog.form.role" :value="1" />
                <span>{{ t('user.admin') }}</span>
              </label>
              <label class="radio-row">
                <input type="radio" v-model.number="dialog.form.role" :value="0" />
                <span>{{ t('user.normal') }}</span>
              </label>
            </div>
          </div>
          <div class="form-group full">
            <label>{{ t('user.permission') }}</label>
            <input v-model="dialog.form.permission" :placeholder="t('user.permission')" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="dialog.visible = false">{{ t('common.cancel') }}</button>
          <button class="btn-confirm" @click="saveUser">{{ t('common.save') }}</button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal-overlay" :class="{ open: confirm.visible }" @click.self="confirm.visible = false">
      <div class="confirm-modal" @click.stop>
        <div class="warn-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--danger)" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        </div>
        <h3>{{ t('common.confirmDelete') }}</h3>
        <p class="modal-sub">{{ t('common.deleteConfirm') }}</p>
        <div class="modal-actions" style="justify-content:center">
          <button class="btn-cancel" @click="confirm.visible = false">{{ t('common.cancel') }}</button>
          <button class="btn-danger" @click="doRemove">{{ t('common.confirmDelete') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getUserList, addUser, updateUser, removeUser } from '@/api/user'
import EmptyState from '@/components/EmptyState.vue'

const { t, locale } = useI18n()
const tableData = ref([])
const loading = ref(false)

const dialog = reactive({ visible: false, isEdit: false, form: {} })
const confirm = reactive({ visible: false, id: null })

onMounted(() => load())

async function load() {
  loading.value = true
  const res = await getUserList({ start: 0, length: 200, username: '', role: -1 })
  tableData.value = res.data?.data || []
  loading.value = false
}

function showAdd() {
  dialog.isEdit = false
  dialog.form = { username: '', password: '', role: 0, permission: '' }
  dialog.visible = true
}

function showEdit(row) {
  dialog.isEdit = true
  dialog.form = { ...row, password: '' }
  dialog.visible = true
}

async function saveUser() {
  const api = dialog.isEdit ? updateUser : addUser
  await api(dialog.form)
  ElMessage.success(t('job.saved'))
  dialog.visible = false
  load()
}

function handleRemove(id) {
  confirm.id = id
  confirm.visible = true
}

async function doRemove() {
  await removeUser(confirm.id)
  ElMessage.success(t('job.deleted'))
  confirm.visible = false
  load()
}
</script>

<style scoped>
.loading-state {
  text-align: center;
  padding: 60px;
  color: var(--muted);
  font-size: 13px;
}
.req {
  color: var(--danger);
}
.radio-group {
  display: flex;
  gap: 24px;
}
.radio-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--fg);
  cursor: pointer;
  user-select: none;
}
.radio-row input[type="radio"] {
  accent-color: var(--accent);
  width: 16px;
  height: 16px;
  cursor: pointer;
}
</style>
