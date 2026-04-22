function validatePasswordStrength(password) {
  const value = typeof password === 'string' ? password : ''
  if (value.length < 8 || value.length > 24) {
    return {
      valid: false,
      message: '密码长度需在 8 到 24 位之间'
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

export function validateRegisterPassword(password) {
  return validatePasswordStrength(password)
}

export function validateChangePasswordForm(form) {
  const payload = form || {}
  const oldPassword = typeof payload.oldPassword === 'string' ? payload.oldPassword : ''
  const newPassword = typeof payload.newPassword === 'string' ? payload.newPassword : ''
  const confirmPassword = typeof payload.confirmPassword === 'string' ? payload.confirmPassword : ''

  if (!oldPassword || !newPassword || !confirmPassword) {
    return {
      valid: false,
      message: '请完整填写密码项'
    }
  }

  if (newPassword !== confirmPassword) {
    return {
      valid: false,
      message: '两次新密码不一致'
    }
  }

  return validatePasswordStrength(newPassword)
}
