<template>
  <div class="login-wrap">
    <div class="bg-layer">
      <div class="aurora-blob a1"></div>
      <div class="aurora-blob a2"></div>
    </div>

    <div class="login-card">
      <div class="login-hd">
        <h2>XXL-JOB <em>Admin</em></h2>
        <p class="login-sub">{{ t('login.subtitle') }}</p>
      </div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>{{ t('login.username') }}</label>
          <input
            v-model="form.userName"
            :placeholder="t('login.usernamePlaceholder')"
            autocomplete="username"
            :class="{ 'input-err': errors.userName }"
          />
          <span v-if="errors.userName" class="field-err">{{ errors.userName }}</span>
        </div>
        <div class="form-group">
          <label>{{ t('login.password') }}</label>
          <input
            v-model="form.password"
            type="password"
            :placeholder="t('login.passwordPlaceholder')"
            autocomplete="current-password"
            :class="{ 'input-err': errors.password }"
          />
          <span v-if="errors.password" class="field-err">{{ errors.password }}</span>
        </div>
        <label class="remember-row">
          <input type="checkbox" v-model="form.ifRemember" />
          <span>{{ t('login.rememberMe') }}</span>
        </label>
        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="loading" class="spin-dot"></span>
          <span v-else>{{ t('login.login') }}</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()

const userStore = useUserStore()
const loading = ref(false)
const errors = reactive({ userName: '', password: '' })

const form = reactive({
  userName: localStorage.getItem('xxl_user') || '',
  password: '',
  ifRemember: false
})

function validate() {
  errors.userName = ''
  errors.password = ''
  let ok = true
  if (!form.userName.trim()) {
    errors.userName = '请输入账号'
    ok = false
  }
  if (!form.password) {
    errors.password = '请输入密码'
    ok = false
  }
  return ok
}

async function handleLogin() {
  if (!validate()) return
  loading.value = true
  try {
    await userStore.login(form)
  } catch (_) {
    /* login service handles error message */
  }
  loading.value = false
}
</script>

<style scoped>
.login-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: var(--bg);
  position: relative;
  overflow: hidden;
}
.login-card {
  width: 400px;
  max-width: 92vw;
  padding: 44px 40px;
  background: rgba(18,18,45,0.70);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  position: relative;
  z-index: 2;
}
.login-hd {
  text-align: center;
  margin-bottom: 36px;
}
.login-hd h2 {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 500;
  letter-spacing: -0.02em;
  color: var(--fg);
  margin: 0;
}
.login-hd h2 em {
  font-style: normal;
  background: linear-gradient(135deg, var(--accent), var(--accent2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.login-sub {
  font-size: 13px;
  color: var(--muted);
  margin: 10px 0 0;
}
.login-card .form-group {
  margin-bottom: 18px;
}
.remember-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  margin-bottom: 26px;
  user-select: none;
}
.remember-row input[type="checkbox"] {
  accent-color: var(--accent);
  width: 15px;
  height: 15px;
  cursor: pointer;
}
.login-btn {
  width: 100%;
  padding: 12px;
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.12em;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--accent), rgba(124,131,255,0.7));
  color: #fff;
  cursor: pointer;
  transition: all .25s var(--ease-out);
  box-shadow: 0 4px 20px rgba(124,131,255,0.25);
}
.login-btn:hover {
  box-shadow: 0 8px 28px rgba(124,131,255,0.4);
  transform: translateY(-1px);
}
.login-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
.input-err {
  border-color: var(--danger) !important;
}
.field-err {
  display: block;
  font-size: 11px;
  color: var(--danger);
  margin-top: 5px;
}
.spin-dot {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.22);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
</style>
