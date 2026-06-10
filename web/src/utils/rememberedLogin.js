export const REMEMBERED_USERNAME_KEY = 'xxl_remembered_username'
export const REMEMBERED_USERNAME_ENABLED_KEY = 'xxl_remember_username_enabled'

export function getRememberedLogin() {
  const ifRemember = localStorage.getItem(REMEMBERED_USERNAME_ENABLED_KEY) === '1'

  return {
    userName: ifRemember ? localStorage.getItem(REMEMBERED_USERNAME_KEY) || '' : '',
    ifRemember,
  }
}

export function setRememberedLogin(userName, ifRemember) {
  if (ifRemember) {
    localStorage.setItem(REMEMBERED_USERNAME_KEY, userName)
    localStorage.setItem(REMEMBERED_USERNAME_ENABLED_KEY, '1')
    return
  }

  localStorage.removeItem(REMEMBERED_USERNAME_KEY)
  localStorage.removeItem(REMEMBERED_USERNAME_ENABLED_KEY)
}