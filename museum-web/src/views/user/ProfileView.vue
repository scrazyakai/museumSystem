<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElAvatar,
  ElMessage,
  ElMessageBox,
  ElUpload,
  ElDialog,
  ElTag,
  ElDescriptions,
  ElDescriptionsItem,
  ElTabPane,
  ElTabs
} from 'element-plus'
import { User, ArrowLeft, Lock, Camera, Checked, Warning } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getToken } from '@/utils/auth'
import {
  getUserProfile,
  updateProfile,
  realNameVerify,
  changePassword,
  type UserProfile
} from '@/api/user'
import type { UploadProps } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

// 当前用户信息
const userProfile = ref<UserProfile | null>(null)
const loading = ref(false)
const activeTab = ref('basic')

// 是否编辑模式
const isEditMode = ref(false)

// 编辑表单
const editForm = ref({
  phone: ''
})

// 实名认证对话框
const showVerifyDialog = ref(false)
const verifyForm = ref({
  realName: '',
  idCard: ''
})

// 修改密码对话框
const showPasswordDialog = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 头像上传URL
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/sys-user/avatar`
})

onMounted(() => {
  loadUserProfile()
})

// 加载用户信息
const loadUserProfile = async () => {
  loading.value = true
  try {
    const data = await getUserProfile()
    userProfile.value = data
    // 初始化编辑表单
    editForm.value = {
      phone: data.phone || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 进入编辑模式
const enterEditMode = () => {
  isEditMode.value = true
}

// 取消编辑
const cancelEdit = () => {
  isEditMode.value = false
  // 恢复原始数据
  if (userProfile.value) {
    editForm.value = {
      phone: userProfile.value.phone || ''
    }
  }
}

// 保存基本信息
const handleSaveProfile = async () => {
  try {
    await updateProfile(editForm.value)
    await loadUserProfile()
    isEditMode.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
  }
}

// 打开实名认证对话框
const openVerifyDialog = () => {
  verifyForm.value = {
    realName: userProfile.value?.realName || '',
    idCard: userProfile.value?.idNo || ''
  }
  showVerifyDialog.value = true
}

// 提交实名认证
const handleVerify = async () => {
  if (!verifyForm.value.realName || !verifyForm.value.idCard) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 身份证号格式验证
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!idCardRegex.test(verifyForm.value.idCard)) {
    ElMessage.warning('请输入正确的身份证号码')
    return
  }

  try {
    await realNameVerify(verifyForm.value)
    await loadUserProfile()
    showVerifyDialog.value = false
    ElMessage.success('实名认证成功')
  } catch (error) {
    console.error('实名认证失败:', error)
  }
}

// 打开修改密码对话框
const openPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showPasswordDialog.value = true
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }

  try {
    await changePassword(passwordForm.value)
    showPasswordDialog.value = false
    ElMessage.success('密码修改成功，请重新登录')
    // 延迟退出登录
    setTimeout(() => {
      authStore.clearUser()
      router.push('/login/user')
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
  }
}

// 头像上传成功
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 0) {
    ElMessage.success('头像上传成功')
    loadUserProfile()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 头像上传前验证
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 返回首页
const goBack = () => {
  router.push('/home')
}
</script>

<template>
  <div class="profile-container">
    <!-- Header -->
    <header class="header">
      <div class="header-content">
        <div class="logo-section">
          <el-icon :size="28" color="#b03128"><User /></el-icon>
          <span class="logo-text">个人中心</span>
        </div>
        <div class="header-actions">
          <el-button link @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回首页</span>
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="main-content" v-loading="loading">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息标签页 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-card class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">基本信息</span>
                <el-button
                  v-if="!isEditMode"
                  type="primary"
                  size="small"
                  @click="enterEditMode"
                >
                  编辑资料
                </el-button>
                <template v-else>
                  <el-button size="small" @click="cancelEdit">取消</el-button>
                  <el-button type="primary" size="small" @click="handleSaveProfile">保存</el-button>
                </template>
              </div>
            </template>

            <!-- 头像区域 -->
            <div class="avatar-section">
              <el-avatar
                :size="120"
                :src="userProfile?.avatarUrl"
                :icon="User"
                class="avatar"
              />
              <el-upload
                :action="uploadUrl"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                name="file"
                :headers="{ satoken: getToken() }"
              >
                <el-button
                  type="primary"
                  size="small"
                  :icon="Camera"
                  class="upload-btn"
                >
                  更换头像
                </el-button>
              </el-upload>
            </div>

            <!-- 基本信息表单 -->
            <el-form label-width="100px" class="profile-form">
              <el-form-item label="用户名">
                <el-input :model-value="userProfile?.username" disabled />
              </el-form-item>

              <el-form-item label="手机号">
                <el-input
                  v-model="editForm.phone"
                  :disabled="!isEditMode"
                  placeholder="请输入手机号"
                />
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <!-- 实名认证标签页 -->
        <el-tab-pane label="实名认证" name="verify">
          <el-card class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">实名认证</span>
                <el-tag
                  v-if="userProfile?.realName"
                  type="success"
                  :icon="Checked"
                >
                  已认证
                </el-tag>
                <el-tag
                  v-else
                  type="warning"
                  :icon="Warning"
                >
                  未认证
                </el-tag>
              </div>
            </template>

            <div v-if="userProfile?.realName" class="verified-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="真实姓名">
                  {{ userProfile.realName }}
                </el-descriptions-item>
                <el-descriptions-item label="身份证号">
                  {{ userProfile.idNo?.replace(/^(.{6})(.*)(.{4})$/, '$1****$3') }}
                </el-descriptions-item>
              </el-descriptions>
              <el-alert
                title="温馨提示"
                type="info"
                :closable="false"
                show-icon
                class="verify-tip"
              >
                实名认证后可进行场馆预约，信息仅用于验证身份，我们将严格保护您的隐私
              </el-alert>
            </div>

            <div v-else class="unverified-info">
              <el-empty description="您还未完成实名认证">
                <el-button type="primary" @click="openVerifyDialog">
                  立即认证
                </el-button>
              </el-empty>
              <el-alert
                title="为什么要实名认证？"
                type="warning"
                :closable="false"
                show-icon
                class="verify-tip"
              >
                根据相关规定，参观博物馆需要进行实名认证。认证后可享受便捷的预约服务
              </el-alert>
            </div>
          </el-card>
        </el-tab-pane>

        <!-- 安全设置标签页 -->
        <el-tab-pane label="安全设置" name="security">
          <el-card class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">安全设置</span>
              </div>
            </template>

            <div class="security-options">
              <div class="security-item">
                <div class="security-info">
                  <div class="security-title">登录密码</div>
                  <div class="security-desc">
                    定期修改密码，保护账号安全
                  </div>
                </div>
                <el-button
                  type="primary"
                  :icon="Lock"
                  @click="openPasswordDialog"
                >
                  修改密码
                </el-button>
              </div>

              <el-alert
                title="安全建议"
                type="info"
                :closable="false"
                show-icon
              >
                <ul class="security-tips">
                  <li>不要使用过于简单的密码，建议使用字母、数字和符号的组合</li>
                  <li>不要在其他网站使用相同密码</li>
                  <li>定期更换密码，保护账号安全</li>
                  <li>不要将密码告知他人</li>
                </ul>
              </el-alert>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 实名认证对话框 -->
    <el-dialog
      v-model="showVerifyDialog"
      title="实名认证"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="真实姓名">
          <el-input
            v-model="verifyForm.realName"
            placeholder="请输入真实姓名"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input
            v-model="verifyForm.idCard"
            placeholder="请输入身份证号码"
            maxlength="18"
          />
        </el-form-item>
        <el-alert
          title="重要提示"
          type="warning"
          :closable="false"
          show-icon
        >
          实名认证信息提交后不可修改，请确保信息准确无误
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showVerifyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleVerify">确定认证</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="当前密码">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            maxlength="20"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* Header 样式 */
.header {
  background-color: #b03128;
  height: 60px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.header-actions .el-button {
  color: rgba(255, 255, 255, 0.9);
}

.header-actions .el-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

/* 主内容区 */
.main-content {
  width: 100%;
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 40px;
  flex: 1;
}

.profile-tabs {
  margin-bottom: 24px;
}

.profile-card {
  border-radius: 8px;
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 30px;
}

.avatar {
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.upload-btn {
  margin-top: 10px;
}

/* 表单样式 */
.profile-form {
  max-width: 600px;
  margin: 0 auto;
}

/* 实名认证区域 */
.verified-info,
.unverified-info {
  padding: 20px;
}

.verify-tip {
  margin-top: 20px;
}

/* 安全设置 */
.security-options {
  padding: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.security-info {
  flex: 1;
}

.security-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.security-desc {
  font-size: 14px;
  color: #666;
}

.security-tips {
  margin: 10px 0;
  padding-left: 20px;
}

.security-tips li {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content,
  .main-content {
    padding: 0 20px;
  }

  .profile-form {
    max-width: 100%;
  }

  .security-item {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
