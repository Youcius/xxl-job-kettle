import { createRouter, createWebHashHistory } from 'vue-router'
import i18n from '@/i18n'

export function updateDocumentTitle(route) {
  const pageTitle = route.meta.titleKey ? i18n.global.t(route.meta.titleKey) : route.meta.title
  document.title = pageTitle ? `${pageTitle} - XXL-JOB` : 'XXL-JOB'
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { requiresAuth: false, titleKey: 'route.login' }
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
        meta: { titleKey: 'route.dashboard', icon: 'Odometer' }
      },
      {
        path: 'job',
        name: 'JobList',
        component: () => import('@/views/job/JobInfoList.vue'),
        meta: { titleKey: 'route.job', icon: 'Operation' }
      },
      {
        path: 'joblog',
        name: 'JobLog',
        component: () => import('@/views/joblog/JobLogList.vue'),
        meta: { titleKey: 'route.joblog', icon: 'Document' }
      },
      {
        path: 'joblog/:id',
        name: 'JobLogDetail',
        component: () => import('@/views/joblog/JobLogDetail.vue'),
        meta: { titleKey: 'route.joblogDetail', hidden: true }
      },
      {
        path: 'group',
        name: 'JobGroup',
        component: () => import('@/views/group/JobGroupList.vue'),
        meta: { titleKey: 'route.group', icon: 'Monitor' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserList.vue'),
        meta: { titleKey: 'route.user', icon: 'User', requireAdmin: true }
      },
      {
        path: 'kettle/group',
        name: 'KettleGroup',
        component: () => import('@/views/kettle/GroupList.vue'),
        meta: { titleKey: 'route.kettleGroup', icon: 'FolderOpened' }
      },
      {
        path: 'kettle/file/:groupId',
        name: 'KettleFile',
        component: () => import('@/views/kettle/FileList.vue'),
        meta: { titleKey: 'route.kettleFile', hidden: true }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  // 用 hash 模式避免 Spring Boot 单 jar 部署时 SPA fallback 404
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  updateDocumentTitle(to)

  if (to.meta.requireAdmin) {
    const role = Number(localStorage.getItem('xxl_role')) || 0
    if (role !== 1) {
      return next('/dashboard')
    }
  }
  next()
})

export default router
