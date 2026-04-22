import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { normalizeFeedMode, resolveAccessibleFeedMode } from '../../utils/home-mode.js'

describe('feed mode policy', () => {
  it('should strip discover mode after policy update', () => {
    assert.equal(normalizeFeedMode('discover'), 'all')
    assert.equal(normalizeFeedMode('all'), 'all')
    assert.equal(normalizeFeedMode('hot'), 'hot')
    assert.equal(normalizeFeedMode('following'), 'following')
  })

  it('should lock guest users to all mode', () => {
    assert.equal(resolveAccessibleFeedMode('all', false), 'all')
    assert.equal(resolveAccessibleFeedMode('hot', false), 'all')
    assert.equal(resolveAccessibleFeedMode('following', false), 'all')
  })

  it('should allow members to access hot and following', () => {
    assert.equal(resolveAccessibleFeedMode('hot', true), 'hot')
    assert.equal(resolveAccessibleFeedMode('following', true), 'following')
  })
})
