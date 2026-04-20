import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readHomeTemplate() {
  const url = new URL('../HomeView.vue', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('home guest experience markup', () => {
  it('should include guest hero block with auth entry points', () => {
    const content = readHomeTemplate()

    assert.match(content, /class="panel guest-hero"\s+v-if="!isLoggedIn"/)
    assert.match(content, /class="hero-copy"/)
    assert.match(content, /class="hero-highlights"/)
    assert.match(content, /<router-link[^>]*to="\/register"[^>]*>/)
    assert.match(content, /<router-link[^>]*to="\/login"[^>]*>/)
  })
})
