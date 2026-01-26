<template>
  <div class="admin-layout">
    <AdminSidebar :collapsed="collapsed" @toggle="handleToggle" />

    <div class="admin-container" :class="{ 'sidebar-collapsed': collapsed }">
      <AdminHeader :collapsed="collapsed" @toggle="handleToggle" />

      <div class="admin-main">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import AdminHeader from './AdminHeader.vue'

const collapsed = ref(false)

const handleToggle = () => {
  collapsed.value = !collapsed.value
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.admin-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left 0.3s;
}

.sidebar-collapsed {
  margin-left: 54px;
}

.admin-main {
  flex: 1;
  overflow-y: auto;
  background: #f0f2f5;
  padding: 20px;
}
</style>
