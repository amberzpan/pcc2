import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readIndexHtml() {
  const url = new URL('../../../index.html', import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('branding', () => {
  it('should expose SimpleSocial title and favicon', () => {
    const content = readIndexHtml()

    assert.match(content, /<title>SimpleSocial<\/title>/)
    assert.match(content, /rel="icon"/)
  })
})
