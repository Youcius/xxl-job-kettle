import request from './request'

export function getLogList(params) {
  return request.post('/joblog/pageList', null, { params })
}

export function getLogDetail(params) {
  return request.post('/joblog/logDetailCat', null, { params })
}

export function killJob(executorAddress, logId) {
  return request.post('/joblog/logKill', null, { params: { executorAddress, id: logId } })
}
