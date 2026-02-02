import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getLoginType, isLoginTypeMatched } from '@/utils/auth'
import { useAuthStore } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 用户登录页
    {
      path: '/login/user',
      name: 'UserLogin',
      component: () => import('@/views/login/UserLogin.vue'),
      meta: { requiresAuth: false }
    },
    
    // 管理员登录页
    {
      path: '/login/admin',
      name: 'AdminLogin',
      component: () => import('@/views/login/AdminLogin.vue'),
      meta: { requiresAuth: false }
    },
    
    // 用户注册页
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/login/Register.vue'),
      meta: { requiresAuth: false }
    },
    
    // 博物馆首页
    {
      path: '/home',
      name: 'HomeView',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true, authType: 'user' }
    },

    // 展品列表（用户端）
    {
      path: '/items',
      name: 'ItemList',
      component: () => import('@/views/items/ItemList.vue'),
      meta: { requiresAuth: true, authType: 'user' }
    },

    // 展品详情（用户端）
    {
      path: '/items/:id',
      name: 'ItemDetail',
      component: () => import('@/views/items/ItemDetail.vue'),
      meta: { requiresAuth: true, authType: 'user' }
    },

    // 预约管理（用户端）
    {
      path: '/booking',
      name: 'BookingView',
      component: () => import('@/views/booking/BookingView.vue'),
      meta: { requiresAuth: true, authType: 'user' }
    },

    // 个人中心（用户端）
    {
      path: '/profile',
      name: 'ProfileView',
      component: () => import('@/views/user/ProfileView.vue'),
      meta: { requiresAuth: true, authType: 'user' }
    },

    // 管理员首页及其子路由
    {
      path: '/admin',
      name: 'AdminHome',
      component: () => import('@/views/admin/AdminHome.vue'),
      meta: { requiresAuth: true, authType: 'admin' }
    },

    // 展品管理（管理端）
    {
      path: '/admin/items',
      name: 'AdminItemManage',
      component: () => import('@/views/admin/AdminItemManage.vue'),
      meta: { requiresAuth: true, authType: 'admin' }
    },

    // 用户管理（管理端）
    {
      path: '/admin/users',
      name: 'AdminUserManage',
      component: () => import('@/views/admin/AdminUserManage.vue'),
      meta: { requiresAuth: true, authType: 'admin' }
    },

    // 评论管理（管理端）
    {
      path: '/admin/comments',
      name: 'AdminCommentManage',
      component: () => import('@/views/admin/AdminCommentManage.vue'),
      meta: { requiresAuth: true, authType: 'admin' }
    },

    // 预约管理（管理端）
    {
      path: '/admin/bookings',
      name: 'AdminBookingManage',
      component: () => import('@/views/admin/AdminBookingManage.vue'),
      meta: { requiresAuth: true, authType: 'admin' }
    },

    // 根路径重定向到博物馆首页
    {
      path: '/',
      redirect: '/home'
    }
  ]
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  useAuthStore();
  const token = getToken()
  const loginType = getLoginType()
  
  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    // 检查是否存在 token
    if (!token) {
      // 没有登录，跳转到对应登录页
      if (to.meta.authType === 'admin') {
        next('/login/admin')
      } else {
        next('/login/user')
      }
      return
    }
    
    // 检查登录类型是否匹配
    const requiredType = to.meta.authType as 'user' | 'admin'
    if (!isLoginTypeMatched(requiredType)) {
      // 登录类型不匹配，跳转到对应登录页
      if (requiredType === 'admin') {
        next('/login/admin')
      } else {
        next('/login/user')
      }
      return
    }
  }
  
  // 如果已登录访问登录页，重定向到对应首页
  if (token && (to.path === '/login/user' || to.path === '/register')) {
    next('/home')
    return
  }
  
  // 如果管理员访问管理员登录页，重定向到管理员首页
  if (token && to.path === '/login/admin' && loginType === 'admin') {
    next('/admin')
    return
  }
  
  next()
})

export default router
