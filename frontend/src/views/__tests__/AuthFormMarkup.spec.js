import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readTemplate(relativePath) {
  const url = new URL(relativePath, import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('auth form security markup', () => {
  it('login view should use form submit and autocomplete fields', () => {
    const content = readTemplate('../LoginView.vue')

    assert.match(content, /<form[^>]*@submit\.prevent="submit"/)
    assert.match(content, /autocomplete="username"/)
    assert.match(content, /autocomplete="current-password"/)
  })

  it('register view should use form submit and strong password autocomplete', () => {
    const content = readTemplate('../RegisterView.vue')

    assert.match(content, /<form[^>]*@submit\.prevent="submit"/)
    assert.match(content, /autocomplete="username"/)
    assert.match(content, /autocomplete="new-password"/)
  })
})
