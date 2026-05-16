const viewModules = import.meta.glob('../views/**/*.vue')
const fallbackComponent = viewModules['../views/exception/404.vue']

function normalizeComponentPath(componentPath) {
    if (!componentPath) {
        return ''
    }
    if (componentPath.startsWith('../')) {
        return componentPath
    }
    if (componentPath.startsWith('views/')) {
        return `../${componentPath}`
    }
    return `../views/${componentPath}`
}

function resolveComponent(componentPath) {
    const normalizedPath = normalizeComponentPath(componentPath)
    return viewModules[normalizedPath] || fallbackComponent
}

function flattenRouteMenus(menuTree, collector = []) {
    menuTree.forEach((item) => {
        if (item.menuType === 'C' && item.routePath) {
            collector.push(item)
        }
        flattenRouteMenus(item.children || [], collector)
    })
    return collector
}

function toRouteName(menu) {
    return `Menu${menu.id}`
}

function toChildPath(routePath) {
    return routePath.replace(/^\//, '')
}

export function addDynamicRoutes(router, menuTree) {
    const routeNames = []
    const routeMenus = flattenRouteMenus(menuTree)

    routeMenus.forEach((menu) => {
        const routeName = toRouteName(menu)
        if (router.hasRoute(routeName)) {
            return
        }

        router.addRoute('Root', {
            path: toChildPath(menu.routePath),
            name: routeName,
            component: resolveComponent(menu.componentPath),
            meta: {
                requiresAuth: true,
                title: menu.menuName,
                permissionCode: menu.permissionCode,
                menuId: menu.id
            }
        })
        routeNames.push(routeName)
    })

    return routeNames
}
