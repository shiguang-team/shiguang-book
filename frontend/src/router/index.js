import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
  { path: '/book/:id', name: 'bookDetail', component: () => import('../views/BookDetail.vue') },
  { path: '/publish', name: 'publish', component: () => import('../views/Publish.vue'), meta: { auth: true } },
  { path: '/favorites', name: 'favorites', component: () => import('../views/Favorites.vue'), meta: { auth: true } },
  { path: '/profile', name: 'profile', component: () => import('../views/Profile.vue'), meta: { auth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router