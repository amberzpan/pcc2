import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

function readTemplate(relativePath) {
  const url = new URL(relativePath, import.meta.url)
  return fs.readFileSync(fileURLToPath(url), 'utf8')
}

describe('auth form sizing', () => {
  it('login and register cards should use larger width', () => {
    const login = readTemplate('../LoginView.vue')
    const register = readTemplate('../RegisterView.vue')

    assert.match(login, /width:\s*min\(920px, 100%\)/)
    assert.match(register, /width:\s*min\(920px, 100%\)/)
  })
})
