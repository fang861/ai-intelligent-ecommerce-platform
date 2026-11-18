import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/user/home'
  },
  {
    path: '/login',
    name: 'UserLogin',
    component: () => import('@/views/UserLogin.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: () => import('@/views/UserRegister.vue'),
    meta: { public: true }
  },
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: () => import('@/views/MerchantLogin.vue'),
    meta: { public: true, merchant: true }
  },
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { public: true },
    redirect: '/user/home',
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue'),
        meta: { public: true, title: '首页' }
      },
      {
        path: 'category',
        name: 'UserCategory',
        component: () => import('@/views/user/Category.vue'),
        meta: { public: true, title: '分类' }
      },
      {
        path: 'product/:id',
        name: 'UserProductDetail',
        component: () => import('@/views/user/ProductDetail.vue'),
        meta: { public: true, title: '商品详情' }
      },
      {
        path: 'shops',
        name: 'UserShops',
        component: () => import('@/views/user/Shops.vue'),
        meta: { public: true, title: '店铺街' }
      },
      {
        path: 'shop/:id',
        name: 'UserShopDetail',
        component: () => import('@/views/user/ShopDetail.vue'),
        meta: { public: true, title: '店铺详情' }
      },
      {
        path: 'cart',
        name: 'UserCart',
        component: () => import('@/views/user/Cart.vue'),
        meta: { requiresAuth: true, requiresUser: true, title: '购物车' }
      },
      {
        path: 'ai-guide',
        name: 'UserAiGuide',
        component: () => import('@/views/user/AiGuide.vue'),
        meta: { requiresAuth: true, requiresUser: true, title: 'AI导购', hideFooter: true, fillHeight: true }
      },
      {
        path: '',
        component: () => import('@/layouts/UserCenterLayout.vue'),
        children: [
          {
            path: 'orders',
            name: 'UserOrders',
            component: () => import('@/views/user/Orders.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '我的订单' }
          },
          {
            path: 'favorites',
            name: 'UserFavorites',
            component: () => import('@/views/user/Favorites.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '商品收藏' }
          },
          {
            path: 'history',
            name: 'UserHistory',
            component: () => import('@/views/user/History.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '我的足迹' }
          },
          {
            path: 'addresses',
            name: 'UserAddresses',
            component: () => import('@/views/user/Addresses.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '收货地址' }
          },
          {
            path: 'wallet',
            name: 'UserWallet',
            component: () => import('@/views/user/Wallet.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '余额充值' }
          },
          {
            path: 'follows',
            name: 'UserFollows',
            component: () => import('@/views/user/Follows.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '店铺关注' }
          },
          {
            path: 'account',
            name: 'UserAccount',
            component: () => import('@/views/user/Account.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '个人信息' }
          },
          {
            path: 'profile',
            name: 'UserProfile',
            component: () => import('@/views/user/Profile.vue'),
            meta: { requiresAuth: true, requiresUser: true, title: '个人中心' }
          }
        ]
      },
      {
        path: 'announcements',
        name: 'UserAnnouncements',
        component: () => import('@/views/user/Announcements.vue'),
        meta: { public: true, title: '公告' }
      },
      {
        path: 'announcements/:id',
        name: 'UserAnnouncementDetail',
        component: () => import('@/views/user/AnnouncementDetail.vue'),
        meta: { public: true, title: '公告详情' }
      },
    ]
  },
  {
    path: '/merchant',
    component: () => import('@/layouts/MerchantLayout.vue'),
    redirect: '/merchant/home',
    meta: { requiresAuth: true, requiresMerchant: true },
    children: [
      {
        path: 'home',
        name: 'MerchantHome',
        component: () => import('@/views/merchant/Home.vue'),
        meta: { title: '商家首页', requiresMerchant: true }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/Products.vue'),
        meta: { title: '商品管理', requiresMerchant: true }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/Orders.vue'),
        meta: { title: '订单管理', requiresMerchant: true }
      },
      {
        path: 'reviews',
        name: 'MerchantReviews',
        component: () => import('@/views/merchant/Reviews.vue'),
        meta: { title: '评价管理', requiresMerchant: true }
      },
      {
        path: 'qa',
        name: 'MerchantQa',
        component: () => import('@/views/merchant/Qa.vue'),
        meta: { title: '问答管理', requiresMerchant: true }
      },
      {
        path: 'profile',
        name: 'MerchantProfile',
        component: () => import('@/views/merchant/Profile.vue'),
        meta: { title: '店铺资料', requiresMerchant: true }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, admin: true }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/admin/home',
    meta: { admin: true, requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', admin: true }
      },
      {
        path: 'admins',
        name: 'AdminAdmins',
        component: () => import('@/views/Admins.vue'),
        meta: { title: '管理员管理', admin: true }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', admin: true }
      },
      {
        path: 'menus',
        name: 'AdminMenus',
        component: () => import('@/views/Menus.vue'),
        meta: { title: '菜单管理', admin: true }
      },
      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('@/views/system/Banners.vue'),
        meta: { title: '轮播图管理', admin: true }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue'),
        meta: { title: '分类管理', admin: true }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/Products.vue'),
        meta: { title: '商品管理', admin: true }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/Orders.vue'),
        meta: { title: '订单管理', admin: true }
      },
      {
        path: 'merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/Merchants.vue'),
        meta: { title: '商家审核', admin: true }
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('@/views/admin/Reviews.vue'),
        meta: { title: '评价管理', admin: true }
      },
      {
        path: 'qa',
        name: 'AdminQa',
        component: () => import('@/views/admin/Qa.vue'),
        meta: { title: '问答管理', admin: true }
      },
      {
        path: 'favorites',
        name: 'AdminFavorites',
        component: () => import('@/views/admin/Favorites.vue'),
        meta: { title: '收藏管理', admin: true }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/Announcements.vue'),
        meta: { title: '公告管理', admin: true }
      },
      {
        path: 'rag',
        name: 'AdminRag',
        component: () => import('@/views/admin/RagKnowledge.vue'),
        meta: { title: 'RAG知识库', admin: true }
      },
      {
        path: 'agent',
        name: 'AdminAgent',
        component: () => import('@/views/admin/AgentCenter.vue'),
        meta: { title: 'AI Agent', admin: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', admin: true }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', admin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (
      (to.path.startsWith('/admin') && from.path.startsWith('/admin')) ||
      (to.path.startsWith('/merchant') && from.path.startsWith('/merchant'))
    ) {
      const main = document.querySelector('.layout .main')
      if (main) main.scrollTop = 0
      return false
    }
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

function isAdminAccount(user) {
  return user?.accountType === 'ADMIN' || user?.role === 'ADMIN'
}

function isMerchantAccount(user) {
  return user?.accountType === 'MERCHANT' || user?.role === 'MERCHANT'
}

function homeFor(user) {
  if (isAdminAccount(user)) return '/admin/home'
  if (isMerchantAccount(user)) return '/merchant/home'
  return '/user/home'
}

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  const isAdminLogin = to.path === '/admin/login'
  const isUserLogin = to.path === '/login'
  const isMerchantLogin = to.path === '/merchant/login'
  const isUserRegister = to.path === '/register'
  const isAdminRoute = to.path.startsWith('/admin') && !isAdminLogin
  const isMerchantRoute = to.path.startsWith('/merchant') && !isMerchantLogin

  if (to.meta.public && !to.meta.requiresAuth) {
    if (token && (isAdminLogin || isUserLogin || isMerchantLogin || isUserRegister)) {
      if (!userStore.user) {
        try {
          await userStore.fetchUser()
        } catch {
          userStore.logout()
          return true
        }
      }
      if (isAdminLogin && isAdminAccount(userStore.user)) {
        return '/admin/home'
      }
      if (isMerchantLogin && isMerchantAccount(userStore.user)) {
        return '/merchant/home'
      }
      if ((isUserLogin || isUserRegister) && !isAdminAccount(userStore.user) && !isMerchantAccount(userStore.user)) {
        return '/user/home'
      }
      if (isUserLogin && isMerchantAccount(userStore.user)) {
        return '/merchant/home'
      }
    }
    return true
  }

  if (to.meta.requiresAuth || isAdminRoute || isMerchantRoute) {
    if (!token) {
      let loginPath = '/login'
      if (to.meta.requiresAdmin || isAdminRoute) loginPath = '/admin/login'
      else if (to.meta.requiresMerchant || isMerchantRoute) loginPath = '/merchant/login'
      return { path: loginPath, query: { redirect: to.fullPath } }
    }

    if (!userStore.user) {
      try {
        await userStore.fetchUser()
      } catch {
        userStore.logout()
        let loginPath = '/login'
        if (to.meta.requiresAdmin || isAdminRoute) loginPath = '/admin/login'
        else if (to.meta.requiresMerchant || isMerchantRoute) loginPath = '/merchant/login'
        return { path: loginPath, query: { redirect: to.fullPath } }
      }
    }

    if (to.meta.requiresAdmin || isAdminRoute) {
      if (!isAdminAccount(userStore.user)) {
        userStore.logout()
        return { path: '/admin/login' }
      }
    }

    if (to.meta.requiresMerchant || isMerchantRoute) {
      if (!isMerchantAccount(userStore.user)) {
        return homeFor(userStore.user)
      }
    }

    if (to.meta.requiresUser) {
      if (isAdminAccount(userStore.user)) return '/admin/home'
      if (isMerchantAccount(userStore.user)) return '/merchant/home'
    }
  }

  return true
})

export default router
