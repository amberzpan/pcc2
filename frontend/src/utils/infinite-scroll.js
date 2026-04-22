export function shouldTriggerInfiniteLoad({ isIntersecting, loading, hasMore }) {
  if (!isIntersecting) {
    return false
  }
  if (loading) {
    return false
  }
  return !!hasMore
}
