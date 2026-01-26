<template>
  <AdminLayout>
    <div class="admin-comment-manage">
      <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="展品ID">
          <el-input
            v-model="queryParams.itemId"
            placeholder="输入展品ID"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="展品名称">
          <el-input
            v-model="queryParams.itemTitle"
            placeholder="输入展品名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="已隐藏" :value="0" />
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

      <!-- 评论表格 -->
      <el-table
        v-loading="loading"
        :data="commentList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="评论ID" width="80" />
        <el-table-column prop="itemId" label="展品ID" width="100" />
        <el-table-column prop="itemTitle" label="展品名称" width="200" show-overflow-tooltip />
        <el-table-column prop="username" label="评论用户" width="120" />
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">已隐藏</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评论时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <!-- 显示按钮：当 status=0（已隐藏）时显示 -->
            <el-button
              v-if="row.status === 0"
              type="info"
              size="small"
              @click="handleShow(row)"
            >
              显示
            </el-button>
            <!-- 隐藏按钮：当 status=1（正常）时显示 -->
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="handleHide(row)"
            >
              隐藏
            </el-button>
            <!-- 删除按钮：所有状态下都显示 -->
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.current"
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
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import {
  getAdminComments,
  deleteAdminComment,
  hideComment,
  showComment,
  type AdminComment,
  type AdminCommentQueryParams
} from '@/api/admin-comments'

// 查询参数
const queryParams = ref<AdminCommentQueryParams>({
  current: 1,
  size: 10,
  itemId: undefined,
  itemTitle: '',
  status: undefined
})

// 评论列表
const commentList = ref<AdminComment[]>([])
const total = ref(0)
const loading = ref(false)

// 查询评论列表
const handleSearch = async () => {
  loading.value = true
  try {
    const result = await getAdminComments(queryParams.value)
    commentList.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('查询评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    itemId: undefined,
    itemTitle: '',
    status: undefined
  }
  handleSearch()
}

// 隐藏评论
const handleHide = async (row: AdminComment) => {
  try {
    await ElMessageBox.confirm(`确定要隐藏用户 "${row.username}" 的这条评论吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await hideComment(row.id)
    ElMessage.success('隐藏成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('隐藏评论失败:', error)
    }
  }
}

// 显示评论
const handleShow = async (row: AdminComment) => {
  try {
    await ElMessageBox.confirm(`确定要显示这条评论吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await showComment(row.id)
    ElMessage.success('显示成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('显示评论失败:', error)
    }
  }
}

// 删除评论
const handleDelete = async (row: AdminComment) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 的这条评论吗？此操作不可恢复！`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteAdminComment(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
    }
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.admin-comment-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.search-form {
  margin-bottom: 20px;
}
</style>
