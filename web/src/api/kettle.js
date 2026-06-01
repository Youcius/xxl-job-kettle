import request from './request'

// 分组列表
export function getGroupList() {
  return request.get('/api/kettle/group/list')
}

// 新增分组
export function addGroup(data) {
  return request.post('/api/kettle/group/add', data)
}

// 修改分组
export function updateGroup(data) {
  return request.post('/api/kettle/group/update', data)
}

// 删除分组
export function deleteGroup(id) {
  return request.delete('/api/kettle/group/delete', { params: { id } })
}

// 文件列表
export function getFileList(params) {
  return request.get('/api/kettle/file/list', { params })
}

// 文件详情
export function getFileDetail(id) {
  return request.get('/api/kettle/file/detail', { params: { id } })
}

// 上传文件（单）
export function uploadFile(formData) {
  return request.post('/api/kettle/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 上传文件（批量）
export function uploadFileBatch(formData) {
  return request.post('/api/kettle/file/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除文件
export function deleteFile(id) {
  return request.delete('/api/kettle/file/delete', { params: { id } })
}

// 批量删除文件
export function deleteFileBatch(ids) {
  return request.delete('/api/kettle/file/delete/batch', { data: ids })
}

// 下载文件
export function downloadFile(id) {
  return request.get('/api/kettle/file/download', { params: { id }, responseType: 'blob' })
}

// 一键创建任务
export function createKettleJob(data) {
  return request.post('/jobinfo/createKettleJob', { ...data, kettleFileId: data.kettleFileId || data.fileId })
}
