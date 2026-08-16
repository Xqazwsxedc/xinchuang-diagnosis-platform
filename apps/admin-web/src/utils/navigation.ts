import type { AuthMenuItem } from '@/api/modules/auth'

export const FALLBACK_ROUTE = '/profile'

export function findFirstAccessibleRoute(menus: AuthMenuItem[] = []): string {
  for (const menu of menus) {
    if (menu.status !== 'ENABLED' || menu.visible !== 1 || menu.menuType !== 'MENU') {
      continue
    }
    if (menu.routePath) {
      return menu.routePath
    }
    const childRoute = findFirstAccessibleRoute(menu.children || [])
    if (childRoute) {
      return childRoute
    }
  }
  return ''
}

export function resolveHomeRoute(menus: AuthMenuItem[] = []): string {
  return findFirstAccessibleRoute(menus) || FALLBACK_ROUTE
}
