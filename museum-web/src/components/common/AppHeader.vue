<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ElButton,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElAvatar,
  ElMessage,
  ElMessageBox
} from 'element-plus'
import { HomeFilled, Collection, Tickets, UserFilled, SwitchButton, Bell, VideoCamera } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { locale, t } = useI18n()

// 切换语言
const changeLanguage = (lang: string) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
  location.reload()
}

// 用户信息
const userInfo = computed(() => authStore.userInfo)

// 判断当前激活的导航
const isActive = (path: string) => {
  return route.path === path
}

// 跳转到首页
const goToHome = () => {
  if (route.path === '/home') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    router.push('/home')
  }
}

// 跳转到展品页面
const goToItems = () => {
  router.push('/items')
}

// 跳转到预约页面
const goToBooking = () => {
  router.push('/booking')
}

// 跳转到直播页面
const goToLive = () => {
  router.push('/live')
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'notices') {
    router.push('/notices')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm(t('common.logoutConfirm'), '提示', {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })

      await logout()
      authStore.clearUser()
      ElMessage.success(t('common.logoutSuccess'))
      router.push('/login/user')
    } catch {
      // 用户取消
    }
  }
}
</script>

<template>
  <header class="app-header">
    <div class="header-content">
      <!-- Logo -->
      <div class="logo" @click="goToHome">
        <img
          width="48"
          height="48"
          src="https://img.icons8.com/fluency/48/museum.png"
          alt="museum"
        />
      </div>

      <!-- 导航菜单 -->
      <div class="header-nav">
        <el-button
          link
          :class="{ active: isActive('/home') }"
          @click="goToHome"
        >
          <el-icon><HomeFilled /></el-icon>
          <span>{{ t('common.home') }}</span>
        </el-button>
        <el-button
          link
          :class="{ active: isActive('/items') || isActive('/items/') }"
          @click="goToItems"
        >
          <el-icon><Collection /></el-icon>
          <span>{{ t('common.exhibitions') }}</span>
        </el-button>
        <el-button
          link
          :class="{ active: isActive('/booking') }"
          @click="goToBooking"
        >
          <el-icon><Tickets /></el-icon>
          <span>{{ t('common.booking') }}</span>
        </el-button>
        <el-button
          link
          :class="{ active: isActive('/live') }"
          @click="goToLive"
        >
          <el-icon><VideoCamera /></el-icon>
          <span>{{ t('common.live') }}</span>
        </el-button>
      </div>

      <!-- 语言切换 -->
      <el-dropdown @command="changeLanguage" class="language-dropdown">
        <div class="language-selector">
          <span>{{ locale === 'zh' ? '中文' : 'EN' }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="zh">中文</el-dropdown-item>
            <el-dropdown-item command="en">English</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 用户信息下拉菜单 -->
      <el-dropdown @command="handleCommand" class="user-dropdown">
        <div class="user-info">
          <el-avatar v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" :size="32" />
          <el-avatar v-else :icon="UserFilled" :size="32" />
          <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><UserFilled /></el-icon>
              {{ t('common.profile') }}
            </el-dropdown-item>
            <el-dropdown-item command="notices">
              <el-icon><Bell /></el-icon>
              {{ t('common.notices') }}
            </el-dropdown-item>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              {{ t('common.logout') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.app-header {
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
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  cursor: pointer;
  margin: 0;
  white-space: nowrap;
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.1);
}

.logo img {
  display: block;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 40px;
  flex: 1;
}

.header-nav .el-button {
  color: rgba(255, 255, 255, 0.9);
  border: none;
  padding: 8px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.header-nav .el-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.header-nav .el-button.active {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.header-nav .el-button .el-icon {
  margin-right: 4px;
}

.user-dropdown {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
  color: rgba(255, 255, 255, 0.9);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  font-size: 14px;
}

/* 语言选择器 */
.language-dropdown {
  margin-left: 16px;
}

.language-selector {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background 0.3s;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.language-selector:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
  }

  .header-nav {
    margin-left: 20px;
  }

  .header-nav .el-button span {
    display: none;
  }

  .username {
    display: none;
  }
}
</style>
