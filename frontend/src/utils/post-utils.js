export function normalizeMediaUrls(value) {
  if (Array.isArray(value)) {
    return value.filter((url) => typeof url === 'string' && url.trim()).map((url) => url.trim())
  }

  if (typeof value !== 'string') {
    return []
  }

  const trimmed = value.trim()
  if (!trimmed) {
    return []
  }

  if (trimmed.startsWith('[')) {
    try {
      const parsed = JSON.parse(trimmed)
      return normalizeMediaUrls(parsed)
    } catch {
      return [trimmed]
    }
  }

  return [trimmed]
}

export function normalizePost(post) {
  const mediaUrls = normalizeMediaUrls(post.mediaUrls?.length ? post.mediaUrls : post.mediaUrl)

  return {
    ...post,
    mediaUrls,
    liked: !!post.liked,
    favorited: !!post.favorited,
    reposted: !!post.reposted,
    followed: !!post.followed,
    showComments: !!post.showComments,
    comments: Array.isArray(post.comments) ? post.comments : [],
    commentsLoading: !!post.commentsLoading,
    commentSort: post.commentSort || 'time_desc',
    newComment: post.newComment || ''
  }
}

export function normalizePostList(items) {
  return Array.isArray(items) ? items.map(normalizePost) : []
}

export function normalizeComments(items) {
  return Array.isArray(items)
    ? items.map((comment) => ({
      ...comment,
      liked: !!comment.liked,
      likeCount: comment.likeCount || 0
    }))
    : []
}
