export function normalizePost(post) {
  return {
    ...post,
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
