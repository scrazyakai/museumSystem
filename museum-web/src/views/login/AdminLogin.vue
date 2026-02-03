<template>
  <div class="login-container admin-login">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <el-card class="login-card admin-card">
      <template #header>
        <div class="login-header">
          <img
            width="48"
            height="48"
            src="https://img.icons8.com/fluency/48/museum.png"
            alt="museum"
            class="login-logo"
          />
          <h2 class="login-title admin-title">管理员登录</h2>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button admin-button"
            @click="handleLogin"
          >
            管理员登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-link type="primary" @click="$router.push('/login/user')">
          返回用户登录
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { login } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login({
          username: loginForm.username,
          password: loginForm.password
        })

        // 验证是否为管理员
        if (res.role !== 'ADMIN') {
          ElMessage.error('该账号不是管理员账号')
          return
        }

        // 保存用户信息
        authStore.setUser(res, 'admin')
        ElMessage.success('登录成功')

        // 跳转到管理后台
        await router.push('/admin')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd 0%, #90caf9 50%, #64b5f6 100%);
  position: relative;
  overflow: hidden;
}

.admin-login {
  background: linear-gradient(135deg, #bbdefb 0%, #64b5f6 50%, #42a5f5 100%);
}

/* 背景装饰圆圈 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.6;
  }
}

.login-card {
  width: 420px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(33, 150, 243, 0.2);
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  z-index: 1;
  position: relative;
}

.admin-card {
  box-shadow: 0 8px 32px rgba(25, 118, 210, 0.3);
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.login-logo {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.login-title {
  text-align: center;
  margin: 0;
  font-size: 24px;
  color: #1976d2;
  font-weight: 600;
}

.admin-title {
  color: #0d47a1;
}

.login-footer {
  text-align: center;
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
  border: none;
  transition: all 0.3s ease;
}

.admin-button {
  background: linear-gradient(135deg, #0d47a1 0%, #1565c0 100%);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(25, 118, 210, 0.4);
}

/* 覆盖 Element Plus 的输入框样式 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.2);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
  border-color: #1976d2;
}

:deep(.el-input__inner) {
  color: #333;
}

:deep(.el-input__prefix-inner) {
  color: #1976d2;
}
</style>
