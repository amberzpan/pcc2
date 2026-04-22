import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { validateChangePasswordForm } from '../../utils/password-policy.js'

describe('change password policy', () => {
  it('should reject missing fields', () => {
    assert.deepEqual(validateChangePasswordForm({
      oldPassword: '',
      newPassword: 'abc12345',
      confirmPassword: 'abc12345'
    }), {
      valid: false,
      message: '请完整填写密码项'
    })
  })

  it('should reject mismatched confirm password', () => {
    assert.deepEqual(validateChangePasswordForm({
      oldPassword: 'old12345',
      newPassword: 'abc12345',
      confirmPassword: 'abc12346'
    }), {
      valid: false,
      message: '两次新密码不一致'
    })
  })

  it('should enforce strength rules for new password', () => {
    assert.deepEqual(validateChangePasswordForm({
      oldPassword: 'old12345',
      newPassword: 'abcdefgh',
      confirmPassword: 'abcdefgh'
    }), {
      valid: false,
      message: '密码需同时包含字母和数字'
    })
  })

  it('should reject too long new password', () => {
    assert.deepEqual(validateChangePasswordForm({
      oldPassword: 'old12345',
      newPassword: 'ab12345678901234567890123X',
      confirmPassword: 'ab12345678901234567890123X'
    }), {
      valid: false,
      message: '密码长度需在 8 到 24 位之间'
    })
  })

  it('should pass with valid change password payload', () => {
    assert.deepEqual(validateChangePasswordForm({
      oldPassword: 'old12345',
      newPassword: 'abc12345',
      confirmPassword: 'abc12345'
    }), {
      valid: true,
      message: ''
    })
  })
})
