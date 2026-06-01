import request from './request'

// 登录
export function login({ userName, password, ifRemember }) {
  const params = new URLSearchParams()
  params.append('userName', userName)
  params.append('password', password)
  if (ifRemember) params.append('ifRemember', 'on')
  return request.post('/login', params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}

// 登出
export function logout() {
  return request.post('/logout')
}
