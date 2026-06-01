import request from './request'

// 任务列表
export function getJobList(params) {
  return request.post('/jobinfo/pageList', null, { params })
}

// 新增任务
export function addJob(data) {
  return request.post('/jobinfo/add', null, { params: data })
}

// 更新任务
export function updateJob(data) {
  return request.post('/jobinfo/update', null, { params: data })
}

// 删除任务
export function removeJob(id) {
  return request.post('/jobinfo/remove', null, { params: { id } })
}

// 启动/停止任务
export function startJob(id) {
  return request.post('/jobinfo/start', null, { params: { id } })
}

export function stopJob(id) {
  return request.post('/jobinfo/stop', null, { params: { id } })
}

// 手动触发
export function triggerJob(params) {
  return request.post('/jobinfo/trigger', null, { params })
}

// 下次触发时间
export function nextTriggerTime(params) {
  return request.post('/jobinfo/nextTriggerTime', null, { params })
}
