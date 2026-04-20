export function validateRegisterPassword(password) {
  const value = typeof password === 'string' ? password : ''
  if (value.length < 8 || value.length > 64) {
    return {
      valid: false,
      message: '密码长度需在 8 到 64 位之间'
    }
  }

  const hasLetter = /[A-Za-z]/.test(value)
  const hasDigit = /\d/.test(value)
  if (!hasLetter || !hasDigit) {
    return {
      valid: false,
      message: '密码需同时包含字母和数字'
    }
  }

  return {
    valid: true,
    message: ''
  }
}
