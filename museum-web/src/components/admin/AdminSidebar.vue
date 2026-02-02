<template>
  <div class="admin-sidebar" :class="{ collapsed }">
    <div class="sidebar-logo">
      <div v-if="!collapsed" class="logo-text">
        <el-icon><Platform /></el-icon>
        <span>博物馆管理</span>
      </div>
      <el-icon v-else class="logo-icon"><Platform /></el-icon>
    </div>

    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      class="sidebar-menu"
      @select="handleSelect"
    >
      <el-menu-item index="dashboard">
        <el-icon><DataAnalysis /></el-icon>
        <template #title>数据统计</template>
      </el-menu-item>

      <el-menu-item index="items">
        <el-icon><Collection /></el-icon>
        <template #title>展品管理</template>
      </el-menu-item>

      <el-menu-item index="users">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>

      <el-menu-item index="comments">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>评论管理</template>
      </el-menu-item>

      <el-menu-item index="bookings">
        <el-icon><Tickets /></el-icon>
        <template #title>预约管理</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Platform, DataAnalysis, Collection, User, ChatDotRound, Tickets } from '@element-plus/icons-vue'

const props = defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/admin/items') return 'items'
  if (path === '/admin/users') return 'users'
  if (path === '/admin/comments') return 'comments'
  if (path === '/admin/bookings') return 'bookings'
  if (path === '/admin') return 'dashboard'
  return 'dashboard'
})

const handleSelect = (index: string) => {
  switch (index) {
    case 'dashboard':
      router.push('/admin')
      break
    case 'items':
      router.push('/admin/items')
      break
    case 'users':
      router.push('/admin/users')
      break
    case 'comments':
      router.push('/admin/comments')
      break
    case 'bookings':
      router.push('/admin/bookings')
      break
  }
}
</script>

<style scoped>
.admin-sidebar {
  width: 200px;
  height: 100vh;
  background: #001529;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.admin-sidebar.collapsed {
  width: 54px;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #002140;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  transition: all 0.3s;
}

.logo-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 24px;
}

.sidebar-menu {
  border: none;
  background: #001529;
  flex: 1;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.sidebar-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: #1890ff !important;
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: inherit;
}
</style>
