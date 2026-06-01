import request from './request'

export function getUserList(params) {
  return request.post('/user/pageList', null, { params })
}

export function addUser(data) {
  return request.post('/user/add', null, { params: data })
}

export function updateUser(data) {
  return request.post('/user/update', null, { params: data })
}

export function removeUser(id) {
  return request.post('/user/remove', null, { params: { id } })
}

export function updatePwd(data) {
  return request.post('/user/updatePwd', null, { params: data })
}
