export function resolveLayoutFlags(pathname, loggedIn) {
  const authPaths = ['/login', '/register']
  const isAuthPage = authPaths.includes(pathname)
  const showSidebar = loggedIn && !isAuthPage
  const isGuestPage = !loggedIn && !isAuthPage
  const expandGuestHomeLayout = isGuestPage && pathname === '/'

  return {
    isAuthPage,
    showSidebar,
    isGuestPage,
    expandGuestHomeLayout,
    compactTopbar: false
  }
}
