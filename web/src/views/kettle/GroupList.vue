<template>
  <div>
    <div class="page-header">
      <h1><em>{{ t('kettle.title') }}</em> {{ t('kettle.group') }}</h1>
      <button class="btn-new" @click="showAdd">{{ t('kettle.newGroup') }}</button>
    </div>

    <section class="glass-panel wide">
      <table class="gls">
        <thead>
          <tr><th style="width:60px">ID</th><th>{{ t('kettle.groupName') }}</th><th>{{ t('kettle.groupDesc') }}</th><th style="width:160px">{{ t('kettle.createTime') }}</th><th style="width:240px">{{ t('common.operate') }}</th></tr>
        </thead>
        <tbody>
          <tr v-if="!tableData.length">
            <td colspan="5">
              <div class="empty-state">
                <div class="empty-icon">📂</div>
                <h3>{{ t('kettle.noGroup') }}</h3>
                <p>{{ t('kettle.noGroupTip') }}</p>
              </div>
            </td>
          </tr>
          <tr v-for="row in tableData" :key="row.id">
            <td><span class="job-id">#{{ row.id }}</span></td>
            <td><span class="job-name">{{ row.groupName }}</span></td>
            <td><span style="font-size:11px;color:var(--muted)">{{ row.groupDesc || '—' }}</span></td>
            <td class="dur">{{ fmt(row.addTime) }}</td>
            <td>
              <div class="actions">
                <button class="act-btn primary" @click="enterFiles(row)" v-tip="t('kettle.manageFile')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
                  <span class="tip">{{ t('kettle.manageFile') }}</span>
                </button>
                <button class="act-btn" @click="showEdit(row)" v-tip="t('common.edit')">
                  <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
                  <span class="tip">{{ t('common.edit') }}</span>
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

    <!-- 新增/编辑弹窗 -->
    <div class="modal-overlay" :class="{ open: dialog.visible }" @click.self="dialog.visible = false">
      <div class="modal" style="width:460px">
        <h3>{{ dialog.isEdit ? t('kettle.editGroup') : t('kettle.newGroup') }}</h3>
        <div class="modal-sub">{{ t('kettle.groupDesc') }}</div>
        <div class="form-group" style="margin-bottom:16px">
          <label>{{ t('kettle.groupName') }} *</label>
          <input v-model="dialog.form.groupName" :placeholder="t('kettle.placeholder.groupName')" maxlength="64">
        </div>
        <div class="form-group" style="margin-bottom:20px">
          <label>{{ t('kettle.groupDesc') }}</label>
          <textarea v-model="dialog.form.groupDesc" rows="2" :placeholder="t('kettle.placeholder.groupDesc')"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="dialog.visible = false">{{ t('common.cancel') }}</button>
          <button class="btn-confirm" @click="saveGroup">{{ t('common.save') }}</button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div class="modal-overlay" :class="{ open: deleteModal.visible }" @click.self="deleteModal.visible = false">
      <div class="confirm-modal">
        <div class="warn-icon">!</div>
        <h3>{{ t('common.confirmDelete') }}</h3>
        <p style="font-size:12px;color:var(--muted);margin-bottom:4px">{{ t('kettle.deleteGroupWarn') }}</p>
        <p style="font-size:13px;color:var(--danger);margin-bottom:20px;font-weight:500">{{ t('common.deleteConfirm') }}</p>
        <div class="modal-actions" style="justify-content:center">
          <button class="btn-cancel" @click="deleteModal.visible = false">{{ t('common.cancel') }}</button>
          <button class="btn-danger" @click="confirmDelete">{{ t('common.confirmDelete') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getGroupList, addGroup, updateGroup, deleteGroup } from '@/api/kettle'
import { formatDateTime } from '@/utils/datetime'

const { t, locale } = useI18n()
const router = useRouter()
const tableData = ref([])
const dialog = reactive({ visible: false, isEdit: false, form: {} })
const deleteModal = reactive({ visible: false, id: null })

onMounted(() => load())

async function load() {
  try { const res = await getGroupList(); tableData.value = res.data?.content || [] } catch (_) {}
}

function showAdd() { dialog.isEdit = false; dialog.form = { groupName: '', groupDesc: '' }; dialog.visible = true }
function showEdit(row) { dialog.isEdit = true; dialog.form = { ...row }; dialog.visible = true }

async function saveGroup() {
  const apiFn = dialog.isEdit ? updateGroup : addGroup
  try { await apiFn(dialog.form); ElMessage.success(t('job.saved')); dialog.visible = false; load() } catch (_) {}
}

function handleRemove(id) { deleteModal.id = id; deleteModal.visible = true }
async function confirmDelete() {
  try { await deleteGroup(deleteModal.id); ElMessage.success(t('job.deleted')); deleteModal.visible = false; load() } catch (_) {}
}

function enterFiles(row) { router.push(`/kettle/file/${row.id}`) }
function fmt(t) { return formatDateTime(t, locale.value) }
</script>
