import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { getFeedModeMeta, normalizeFeedMode, resolveAccessibleFeedMode } from '../../utils/home-mode.js'

describe('home feed mode helpers', () => {
  it('should normalize invalid mode to all', () => {
    assert.equal(normalizeFeedMode('unknown-mode'), 'all')
    assert.equal(normalizeFeedMode('discover'), 'discover')
  })

  it('should fallback following mode to all for guests', () => {
    assert.equal(resolveAccessibleFeedMode('following', false), 'all')
    assert.equal(resolveAccessibleFeedMode('following', true), 'following')
  })

  it('should provide different copy for guests and members', () => {
    const guestMeta = getFeedModeMeta('discover', false)
    const memberMeta = getFeedModeMeta('discover', true)

    assert.equal(guestMeta.label, '发现动态')
    assert.match(guestMeta.description, /游客|公开/)
    assert.equal(memberMeta.label, '发现动态')
    assert.match(memberMeta.description, /正在讨论|新鲜/)
  })
})
