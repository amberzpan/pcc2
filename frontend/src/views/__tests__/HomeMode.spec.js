import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { getFeedModeMeta, normalizeFeedMode, resolveAccessibleFeedMode } from '../../utils/home-mode.js'

describe('home feed mode helpers', () => {
  it('should normalize invalid and removed mode to all', () => {
    assert.equal(normalizeFeedMode('unknown-mode'), 'all')
    assert.equal(normalizeFeedMode('discover'), 'all')
  })

  it('should lock guests to all mode', () => {
    assert.equal(resolveAccessibleFeedMode('hot', false), 'all')
    assert.equal(resolveAccessibleFeedMode('following', false), 'all')
    assert.equal(resolveAccessibleFeedMode('all', false), 'all')
    assert.equal(resolveAccessibleFeedMode('following', true), 'following')
  })

  it('should provide different copy for guests and members', () => {
    const guestMeta = getFeedModeMeta('hot', false)
    const memberMeta = getFeedModeMeta('hot', true)

    assert.equal(guestMeta.label, '热门帖')
    assert.match(guestMeta.description, /请先登录|登录后/)
    assert.equal(memberMeta.label, '热门帖')
    assert.match(memberMeta.description, /热度|讨论/)
  })
})
