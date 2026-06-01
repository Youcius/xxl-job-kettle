<template>
  <!-- 静态背景 -->
  <div class="bg-layer"></div>

  <!-- 侧栏 -->
  <aside class="sidebar">
    <div class="shead">
      <div style="display:flex;align-items:center;justify-content:space-between">
        <div class="slogo">XXL<span>-JOB</span></div>
        <button @click="toggleLang" class="lang-btn" :title="locale === 'zh-CN' ? 'Switch to English' : '切换到中文'">
          {{ locale === 'zh-CN' ? 'EN' : '中' }}
        </button>
      </div>
      <div class="senv"><span class="edot"></span>{{ t('header.prod') }}</div>
    </div>
    <nav class="snav">
      <div class="sec-title">{{ t('menu.monitor') }}</div>
      <router-link to="/dashboard" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
        {{ t('menu.dashboard') }}
      </router-link>
      <router-link to="/job" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="M9 14l2 2 4-4"/></svg>
        {{ t('menu.job') }}
      </router-link>
      <router-link to="/joblog" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        {{ t('menu.joblog') }}
      </router-link>
      <div class="sec-title">{{ t('menu.system') }}</div>
      <router-link to="/group" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
        {{ t('menu.group') }}
      </router-link>
      <router-link v-if="userStore.role === 1" to="/user" active-class="active">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        {{ t('menu.user') }}
      </router-link>
      <div class="sec-title">{{ t('menu.data') }}</div>
      <router-link to="/kettle/group" :class="$route.path.startsWith('/kettle/') ? 'active' : ''">
        <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
        {{ t('menu.kettle') }}
      </router-link>
    </nav>
    <div class="sfoot">
      <div>
        <div style="font-size:12px;font-weight:500">{{ userStore.username }}</div>
        <div style="font-size:10px;color:var(--muted)">{{ userStore.role === 1 ? t('user.admin') : t('user.normal') }}</div>
      </div>
      <button @click="pwdDialog.visible = true" :title="t('user.changePassword')" style="margin-left:auto;background:none;border:1px solid var(--glass-border);color:var(--muted);padding:4px 8px;border-radius:8px;cursor:pointer;font-size:11px;font-family:var(--font-body);margin-right:4px;white-space:nowrap;min-width:fit-content">{{ t('user.changePassword') }}</button>
      <button @click="userStore.logout()" style="background:none;border:1px solid var(--glass-border);color:var(--muted);padding:4px 10px;border-radius:8px;cursor:pointer;font-size:11px;font-family:var(--font-body)">{{ t('header.logout') }}</button>
    </div>
  </aside>

  <!-- 修改密码弹窗 -->
  <div class="modal-overlay" :class="{ open: pwdDialog.visible }" @click.self="pwdDialog.visible = false">
    <div class="confirm-modal" @click.stop style="width:400px;text-align:left">
      <h3 style="margin-bottom:4px">{{ t('user.changePassword') }}</h3>
      <p class="modal-sub">{{ t('user.currentAccount') }}: {{ userStore.username }}</p>
      <div class="form-group">
        <label>{{ t('user.oldPassword') }} <span style="color:var(--danger)">*</span></label>
        <input v-model="pwdDialog.oldPassword" type="password" :placeholder="t('user.passwordPlaceholder')" @keyup.enter="doUpdatePwd" />
      </div>
      <div class="form-group" style="margin-top:12px">
        <label>{{ t('user.newPassword') }} <span style="color:var(--danger)">*</span></label>
        <input v-model="pwdDialog.newPassword" type="password" :placeholder="t('user.newPasswordPlaceholder')" @keyup.enter="doUpdatePwd" />
      </div>
      <p v-if="pwdDialog.error" style="color:var(--danger);font-size:12px;margin-top:8px">{{ pwdDialog.error }}</p>
      <div class="modal-actions" style="margin-top:20px">
        <button class="btn-cancel" @click="pwdDialog.visible = false">{{ t('common.cancel') }}</button>
        <button class="btn-confirm" @click="doUpdatePwd">{{ t('common.confirm') }}</button>
      </div>
    </div>
  </div>

  <!-- 主内容 -->
  <div class="main-wrap">
    <router-view />
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { updatePwd } from '@/api/user'
import { ElMessage } from 'element-plus'

const { locale, t } = useI18n()
const userStore = useUserStore()

function toggleLang() {
  locale.value = locale.value === 'zh-CN' ? 'en' : 'zh-CN'
  localStorage.setItem('xxl_lang', locale.value)
}

const pwdDialog = reactive({
  visible: false,
  oldPassword: '',
  newPassword: '',
  error: ''
})

async function doUpdatePwd() {
  pwdDialog.error = ''
  if (!pwdDialog.oldPassword) { pwdDialog.error = t('user.oldPasswordRequired'); return }
  if (!pwdDialog.newPassword) { pwdDialog.error = t('user.passwordRequired'); return }
  if (pwdDialog.newPassword.length < 4 || pwdDialog.newPassword.length > 20) { pwdDialog.error = t('user.passwordLenError'); return }
  try {
    await updatePwd({ password: pwdDialog.newPassword, oldPassword: pwdDialog.oldPassword })
    ElMessage.success(t('user.passwordChanged'))
    pwdDialog.visible = false
    pwdDialog.oldPassword = ''
    pwdDialog.newPassword = ''
  } catch (e) {
    pwdDialog.error = t('user.passwordError')
  }
}
</script>

<style scoped>
.lang-btn {
  background: rgba(124,131,255,0.1);
  border: 1px solid rgba(124,131,255,0.2);
  color: var(--accent);
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
  cursor: pointer;
  font-family: var(--font-mono);
  letter-spacing: 0.04em;
  transition: all .2s;
}
.lang-btn:hover {
  background: rgba(124,131,255,0.2);
  border-color: rgba(124,131,255,0.35);
}
</style>
