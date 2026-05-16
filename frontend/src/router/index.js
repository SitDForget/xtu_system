import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import { getToken } from '@/utils/auth'
import pinia from '@/stores'
import { useAuthStore } from '@/stores/auth'
import { addDynamicRoutes } from './dynamic'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: {
            public: true
        }
    },
    {
        path: '/',
        name: 'Root',
        component: Layout,
        redirect: '/dashboard',
        meta: {
            requiresAuth: true
        },
        children: [
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/profile/index.vue'),
                meta: {
                    requiresAuth: true,
                    title: '个人中心'
                }
            }
        ]
    },
    {
        path: '/403',
        name: 'Forbidden',
        component: () => import('@/views/exception/403.vue'),
        meta: {
            public: true
        }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/exception/404.vue'),
        meta: {
            public: true
        }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach(async (to) => {
    const token = getToken()
    const authStore = useAuthStore(pinia)
    const isNotFoundRoute = to.name === 'NotFound'

    if (to.path === '/login') {
        if (token) {
            return '/dashboard'
        }
        return true
    }

    if (token) {
        try {
            if (!authStore.user) {
                await authStore.fetchCurrentUser()
            }

            if (!authStore.menusLoaded) {
                const menuTree = await authStore.fetchMenus()
                addDynamicRoutes(router, menuTree)
                return {
                    path: to.fullPath,
                    replace: true
                }
            }
        } catch (error) {
            authStore.logout()
            return {
                path: '/login',
                query: {
                    redirect: to.fullPath
                }
            }
        }

        if (to.meta.permissionCode && !authStore.hasPermission(to.meta.permissionCode)) {
            return '/403'
        }

        return true
    }

    if (isNotFoundRoute) {
        return {
            path: '/login',
            query: {
                redirect: to.fullPath
            }
        }
    }

    if (to.meta.public) {
        return true
    }

    return {
        path: '/login',
        query: {
            redirect: to.fullPath
        }
    }
})

export default router
