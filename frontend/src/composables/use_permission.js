import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export function usePermission() {
    const authStore = useAuthStore()
    const permissionSet = computed(() => new Set(authStore.permissions || []))

    function hasPermission(permissionCode) {
        if (!permissionCode) {
            return true
        }
        return permissionSet.value.has(permissionCode)
    }

    function hasAnyPermission(permissionCodes = []) {
        return permissionCodes.some((permissionCode) => hasPermission(permissionCode))
    }

    return {
        permissions: computed(() => authStore.permissions || []),
        hasPermission,
        hasAnyPermission
    }
}
