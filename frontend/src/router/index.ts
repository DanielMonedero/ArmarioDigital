import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, layout: 'blank' }
  },
  {
    path: '/',
    redirect: { name: 'wardrobe' }
  },
  {
    path: '/wardrobe',
    name: 'wardrobe',
    component: () => import('@/views/WardrobeView.vue'),
    meta: { title: 'Mi armario' }
  },
  {
    path: '/for-sale',
    name: 'for-sale',
    component: () => import('@/views/ForSaleView.vue'),
    meta: { title: 'En venta' }
  },
  {
    path: '/sold',
    name: 'sold',
    component: () => import('@/views/SoldView.vue'),
    meta: { title: 'Vendidas' }
  },
  {
    path: '/locations',
    name: 'locations',
    component: () => import('@/views/LocationsView.vue'),
    meta: { title: 'Ubicaciones' }
  },
  {
    path: '/outfits',
    name: 'outfits',
    component: () => import('@/views/OutfitsView.vue'),
    meta: { title: 'Conjuntos' }
  },
  {
    path: '/outfits/:id(\\d+)',
    name: 'outfit-detail',
    component: () => import('@/views/OutfitDetailView.vue'),
    meta: { title: 'Conjunto' },
    props: true
  },
  {
    path: '/garments/new',
    name: 'garment-new',
    component: () => import('@/views/GarmentNewView.vue'),
    meta: { title: 'Nueva prenda' }
  },
  {
    path: '/garments/:id(\\d+)',
    name: 'garment-detail',
    component: () => import('@/views/GarmentDetailView.vue'),
    meta: { title: 'Detalle' },
    props: true
  },
  {
    path: '/garments/:id(\\d+)/edit',
    name: 'garment-edit',
    component: () => import('@/views/GarmentEditView.vue'),
    meta: { title: 'Editar prenda' },
    props: true
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true, layout: 'blank' }
  }
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // Bootstrap only once on the first navigation.
  if (auth.status === 'idle') {
    await auth.bootstrap()
  }

  const isPublic = to.meta.public === true

  if (!isPublic && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'wardrobe' }
  }

  return true
})

router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) ?? 'Armario'
  document.title = `${title} · Armario`
})
