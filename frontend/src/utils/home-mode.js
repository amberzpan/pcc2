const MODE_META = {
  all: {
    label: '首页',
    memberDescription: '按时间顺序看最新发布的内容。',
    guestDescription: '未登录状态下可浏览公开内容；登录后可点赞、评论和收藏。'
  },
  hot: {
    label: '热门帖',
    memberDescription: '按互动热度排序，优先展示讨论度较高的帖子。',
    guestDescription: '登录后可以看热门帖。'
  },
  following: {
    label: '关注',
    memberDescription: '仅展示你已关注用户发布的内容。',
    guestDescription: '登录后可查看关注内容。'
  }
}

export function normalizeFeedMode(mode) {
  return Object.prototype.hasOwnProperty.call(MODE_META, mode) ? mode : 'all'
}

export function resolveAccessibleFeedMode(mode, loggedIn) {
  const normalized = normalizeFeedMode(mode)
  if (!loggedIn) {
    return 'all'
  }
  return normalized
}

export function getFeedModeMeta(mode, loggedIn) {
  const normalized = normalizeFeedMode(mode)
  const entry = MODE_META[normalized]
  return {
    mode: normalized,
    label: entry.label,
    description: loggedIn ? entry.memberDescription : entry.guestDescription
  }
}
