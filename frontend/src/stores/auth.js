import { defineStore } from 'pinia'
import { getCurrentUser, getMenus } from '@/api/auth'
import { clearToken, getToken, setToken } from '@/utils/auth'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: getToken(),
        user: null,
        menus: [],
        permissions: [],
        menusLoaded: false
    }),
    getters: {
        hasPermission: (state) => (permissionCode) => {
            if (!permissionCode) {
                return true
            }
            return state.permissions.includes(permissionCode)
        }
    },
    actions: {
        saveToken(token) {
            this.token = token
            setToken(token)
            this.user = null
            this.menus = []
            this.permissions = []
            this.menusLoaded = false
        },
        saveUser(user) {
            this.user = user
            this.permissions = Array.isArray(user?.permissions) ? user.permissions : []
        },
        saveMenus(menus) {
            this.menus = Array.isArray(menus) ? menus : []
            this.menusLoaded = true
        },
        async fetchCurrentUser() {
            const response = await getCurrentUser()
            this.saveUser(response.data)
            return response.data
        },
        async fetchMenus() {
            const response = await getMenus()
            this.saveMenus(response.data)
            return response.data
        },
        clearAuthorization() {
            this.user = null
            this.menus = []
            this.permissions = []
            this.menusLoaded = false
        },
        logout() {
            this.token = ''
            this.clearAuthorization()
            clearToken()
        }
    }
})
