import request from './request'

export function getGroupList() {
  return request.post('/jobgroup/pageList', null, { params: { start: 0, length: 200, appname: '', title: '' } })
}

export function saveGroup(data) {
  return request.post('/jobgroup/save', null, { params: data })
}

export function updateGroup(data) {
  return request.post('/jobgroup/update', null, { params: data })
}

export function removeGroup(id) {
  return request.post('/jobgroup/remove', null, { params: { id } })
}
