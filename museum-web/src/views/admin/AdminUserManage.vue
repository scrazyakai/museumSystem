<template>
  <AdminLayout>
    <div class="admin-user-manage">
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">用户管理</span>
            <el-button type="primary" :icon="Download" @click="handleExport">
              导出用户
            </el-button>
          </div>
        </template>

        <!-- 搜索栏 -->
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="queryParams.keyword"
              placeholder="输入用户名或昵称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryParams.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <el-option label="正常" :value="0" />
              <el-option label="禁用" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button :icon="Refresh" @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 用户表格 -->
        <el-table
          v-loading="loading"
          :data="userList"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="nickname" label="昵称" width="150">
            <template #default="{ row }">
              {{ row.nickname || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.role === 'ADMIN'" type="danger">管理员</el-tag>
              <el-tag v-else type="success">普通用户</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="130">
            <template #default="{ row }">
              {{ row.phone || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="success">正常</el-tag>
              <el-tag v-else type="danger">禁用</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                :icon="View"
                @click="handleViewDetail(row)"
              >
                详情
              </el-button>
              <el-button
                v-if="row.status === 0"
                type="danger"
                size="small"
                :icon="Lock"
                @click="handleBanUser(row)"
              >
                禁用
              </el-button>
              <el-button
                v-else
                type="success"
                size="small"
                :icon="Unlock"
                @click="handleUnbanUser(row)"
              >
                解禁
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </el-card>
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="600px"
    >
      <el-descriptions v-if="currentUser" :column="1" border>
        <el-descriptions-item label="用户ID">
          {{ currentUser.id }}
        </el-descriptions-item>
        <el-descriptions-item label="用户名">
          {{ currentUser.username }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-if="currentUser.role === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else type="success">普通用户</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ currentUser.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ currentUser.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="身份证号">
          {{ currentUser.idCard || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="QQ OpenID">
          {{ currentUser.qqOpenid || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="微信 OpenID">
          {{ currentUser.wechatOpenid || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="允许推送">
          <el-tag :type="currentUser.allowPush === 1 ? 'success' : 'info'">
            {{ currentUser.allowPush === 1 ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="允许足迹">
          <el-tag :type="currentUser.allowFootprint === 1 ? 'success' : 'info'">
            {{ currentUser.allowFootprint === 1 ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentUser.status === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(currentUser.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDateTime(currentUser.updatedAt) }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, View, Lock, Unlock } from '@element-plus/icons-vue'
import {
  getUserList,
  getUserDetail,
  banUser,
  unbanUser,
  exportUsers,
  type UserInfo,
  type UserQueryParams
} from '@/api/admin'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import dayjs from 'dayjs'

// 查询参数
const queryParams = ref<UserQueryParams>({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined
})

// 用户列表
const userList = ref<UserInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentUser = ref<UserInfo | null>(null)

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 查询用户列表
const handleSearch = async () => {
  loading.value = true
  try {
    const result = await getUserList(queryParams.value)
    userList.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('查询用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const handleReset = () => {
  queryParams.value = {
    page: 1,
    size: 10,
    keyword: '',
    status: undefined
  }
  handleSearch()
}

// 查看用户详情
const handleViewDetail = async (row: UserInfo) => {
  try {
    const detail = await getUserDetail(row.id)
    currentUser.value = detail
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

// 禁用用户
const handleBanUser = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用用户 "${row.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await banUser(row.id)
    ElMessage.success('禁用成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('禁用用户失败:', error)
    }
  }
}

// 解禁用户
const handleUnbanUser = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要解禁用户 "${row.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await unbanUser(row.id)
    ElMessage.success('解禁成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('解禁用户失败:', error)
    }
  }
}

// 导出用户
const handleExport = async () => {
  try {
    const params = {
      keyword: queryParams.value.keyword,
      status: queryParams.value.status
    }

    const blob = await exportUsers(params)

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `用户列表_${new Date().getTime()}.xlsx`
    link.click()

    // 释放 URL 对象
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出用户失败:', error)
  }
}

// 初始化
handleSearch()
</script>

<style scoped>
.admin-user-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.search-form {
  margin-bottom: 20px;
}
</style>
