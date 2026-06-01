import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '运行报表', icon: 'Odometer' }
      },
      {
        path: 'job',
        name: 'JobList',
        component: () => import('@/views/job/JobInfoList.vue'),
        meta: { title: '任务管理', icon: 'Operation' }
      },
      {
        path: 'joblog',
        name: 'JobLog',
        component: () => import('@/views/joblog/JobLogList.vue'),
        meta: { title: '调度日志', icon: 'Document' }
      },
      {
        path: 'joblog/:id',
        name: 'JobLogDetail',
        component: () => import('@/views/joblog/JobLogDetail.vue'),
        meta: { title: '日志详情', hidden: true }
      },
      {
        path: 'group',
        name: 'JobGroup',
        component: () => import('@/views/group/JobGroupList.vue'),
        meta: { title: '执行器管理', icon: 'Monitor' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', requireAdmin: true }
      },
      {
        path: 'kettle/group',
        name: 'KettleGroup',
        component: () => import('@/views/kettle/GroupList.vue'),
        meta: { title: 'Kettle分组', icon: 'FolderOpened' }
      },
      {
        path: 'kettle/file/:groupId',
        name: 'KettleFile',
        component: () => import('@/views/kettle/FileList.vue'),
        meta: { title: 'Kettle文件', hidden: true }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - XXL-JOB` : 'XXL-JOB'

  if (to.meta.requireAdmin) {
    const role = Number(localStorage.getItem('xxl_role')) || 0
    if (role !== 1) {
      return next('/dashboard')
    }
  }
  next()
})

export default router
