import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { validateRegisterPassword } from '../../utils/password-policy.js'

describe('register password policy', () => {
  it('should reject short passwords', () => {
    assert.deepEqual(validateRegisterPassword('abc123'), {
      valid: false,
      message: '密码长度需在 8 到 24 位之间'
    })
  })

  it('should reject overlong passwords', () => {
    assert.deepEqual(validateRegisterPassword('ab12345678901234567890123X'), {
      valid: false,
      message: '密码长度需在 8 到 24 位之间'
    })
  })

  it('should reject passwords without letters', () => {
    assert.deepEqual(validateRegisterPassword('12345678'), {
      valid: false,
      message: '密码需同时包含字母和数字'
    })
  })

  it('should reject passwords without digits', () => {
    assert.deepEqual(validateRegisterPassword('abcdefgh'), {
      valid: false,
      message: '密码需同时包含字母和数字'
    })
  })

  it('should accept strong password', () => {
    assert.deepEqual(validateRegisterPassword('abc12345'), {
      valid: true,
      message: ''
    })
  })
})
