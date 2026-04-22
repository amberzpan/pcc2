import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { shouldTriggerInfiniteLoad } from '../../utils/infinite-scroll.js'

describe('infinite scroll trigger', () => {
  it('should trigger when intersecting and can load', () => {
    assert.equal(shouldTriggerInfiniteLoad({
      isIntersecting: true,
      loading: false,
      hasMore: true
    }), true)
  })

  it('should not trigger while loading or no more data', () => {
    assert.equal(shouldTriggerInfiniteLoad({
      isIntersecting: true,
      loading: true,
      hasMore: true
    }), false)

    assert.equal(shouldTriggerInfiniteLoad({
      isIntersecting: true,
      loading: false,
      hasMore: false
    }), false)

    assert.equal(shouldTriggerInfiniteLoad({
      isIntersecting: false,
      loading: false,
      hasMore: true
    }), false)
  })
})
