import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readPostCard() {
  const url = new URL('../../components/PostCard.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('post card markup policy', () => {
  it('should expose original post jump trigger', () => {
    const content = readPostCard()

    assert.match(content, /@click="openOriginalPost"/)
  })

  it('should remove emoji-based action labels', () => {
    const content = readPostCard()

    assert.doesNotMatch(content, /👍|💬|⭐|🔁|🗑/)
  })
})
