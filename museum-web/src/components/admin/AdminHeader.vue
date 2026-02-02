<template>
  <div class="admin-header">
    <div class="header-left">
      <el-button
        link
        :icon="collapsed ? Expand : Fold"
        @click="handleToggle"
        class="toggle-btn"
      />
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentBreadcrumb">
          {{ currentBreadcrumb }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" :size="32" />
          <el-avatar v-else :icon="UserFilled" :size="32" />
          <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
          <el-tag type="danger" size="small">管理员</el-tag>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Fold, Expand, UserFilled, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { logout } from '@/api/auth'

const props = defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const userInfo = computed(() => authStore.userInfo)

const currentBreadcrumb = computed(() => {
  const path = route.path
  if (path === '/admin/items') return '展品管理'
  if (path === '/admin/users') return '用户管理'
  if (path === '/admin') return '数据统计'
  return ''
})

const handleToggle = () => {
  emit('toggle')
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      try {
        await logout()
      } catch (error) {
        console.error('退出登录接口调用失败:', error)
      }

      authStore.clearUser()
      ElMessage.success('已退出登录')
      router.push('/login/admin')
    } catch {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.admin-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  font-size: 20px;
  color: #303133;
}

.toggle-btn:hover {
  background: #f5f5f5;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #303133;
}

.el-dropdown :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
