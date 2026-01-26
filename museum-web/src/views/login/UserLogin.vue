<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2 class="login-title">用户登录</h2>
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
            placeholder="请输入用户名"
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
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <el-link type="primary" @click="$router.push('/register')">
          注册账号
        </el-link>
      </div>
      
      <el-divider>第三方登录</el-divider>
      
      <div class="social-login">
        <el-button
          type="info"
          size="default"
          class="social-button"
          @click="handleSocialLogin('QQ')"
        >
          QQ登录
        </el-button>
        <el-button
          type="success"
          size="default"
          class="social-button"
          @click="handleSocialLogin('微信')"
        >
          微信登录
        </el-button>
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
    { required: true, message: '请输入用户名', trigger: 'blur' }
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
        
        console.log('登录响应:', res)
        
        // 保存用户信息
        authStore.setUser(res, res.role === 'ADMIN' ? 'admin' : 'user')
        
        console.log('用户信息已保存')
        
        ElMessage.success('登录成功')
        
        console.log('准备跳转到 /home')
        await router.push('/home')
        console.log('跳转完成')
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败: ' + (error as Error).message)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleSocialLogin = (type: string) => {
  ElMessage.info(`${type}登录暂未开放`)
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  border-radius: 8px;
}

.login-title {
  text-align: center;
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.login-footer {
  text-align: center;
  margin-bottom: 20px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-button {
  flex: 1;
}
</style>
