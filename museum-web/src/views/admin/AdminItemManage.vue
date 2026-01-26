<template>
  <AdminLayout>
    <div class="admin-item-manage">
      <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="输入展品标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="媒体类型">
          <el-select
            v-model="queryParams.mediaKind"
            placeholder="请选择"
            clearable
            style="width: 120px"
          >
            <el-option label="图片" value="IMAGE" />
            <el-option label="视频" value="VIDEO" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd">
            新增展品
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 展品表格 -->
      <el-table
        v-loading="loading"
        :data="itemList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column label="媒体类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.mediaKind === 'IMAGE' ? 'success' : 'warning'">
              {{ row.mediaKind === 'IMAGE' ? '图片' : '视频' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预览" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.mediaKind === 'IMAGE'"
              :src="row.mediaUrl"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
              :preview-src-list="[row.mediaUrl]"
            />
            <div v-else class="video-preview">
              <el-icon :size="32"><VideoPlay /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="展示时间" width="200">
          <template #default="{ row }">
            {{ formatTimeRange(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handlePublish(row)"
            >
              上架
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleUnpublish(row)"
            >
              下架
            </el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="展品标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入展品标题" />
        </el-form-item>

        <el-form-item label="媒体类型" prop="mediaKind">
          <el-radio-group v-model="formData.mediaKind" :disabled="isEdit">
            <el-radio label="IMAGE">图片</el-radio>
            <el-radio label="VIDEO">视频</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="上传文件" prop="mediaUrl">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
            accept="image/*,video/*"
          >
            <el-button type="primary" :loading="uploading">
              <el-icon><Upload /></el-icon>
              {{ uploading ? '上传中...' : '选择文件' }}
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持图片（jpg/png等）和视频（mp4等），文件大小不超过100MB
              </div>
            </template>
          </el-upload>

          <!-- 已上传的文件预览 -->
          <div v-if="formData.mediaUrl" class="file-preview">
            <el-image
              v-if="formData.mediaKind === 'IMAGE'"
              :src="formData.mediaUrl"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 4px"
            />
            <div v-else class="video-url">
              <el-icon><VideoPlay /></el-icon>
              <span>{{ formData.mediaUrl }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="formData.mediaKind === 'VIDEO'" label="视频封面">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleCoverUploadSuccess"
            :show-file-list="false"
            accept="image/*"
          >
            <el-button type="primary" :loading="uploading">
              上传封面
            </el-button>
          </el-upload>

          <div v-if="formData.coverUrl" class="file-preview">
            <el-image
              :src="formData.coverUrl"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 4px"
            />
          </div>
        </el-form-item>

        <el-form-item label="展品描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入展品描述"
          />
        </el-form-item>

        <el-form-item label="开始时间">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="结束时间">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, VideoPlay, Upload } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getToken } from '@/utils/auth'
import {
  getAdminItems,
  addItem,
  updateItem,
  publishItem,
  unpublishItem,
  restoreItem,
  deleteItem,
  type ExhibitItem,
  type AdminItemQueryParams,
  type AddItemRequest,
  type UpdateItemRequest,
  type MediaKind
} from '@/api/admin-items'
import dayjs from 'dayjs'
import AdminLayout from '@/components/admin/AdminLayout.vue'

const authStore = useAuthStore()

// 查询参数
const queryParams = ref<AdminItemQueryParams>({
  current: 1,
  size: 10,
  keyword: '',
  mediaKind: undefined
})

// 展品列表
const itemList = ref<ExhibitItem[]>([])
const total = ref(0)
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑展品' : '新增展品')
const isEdit = ref(false)
const formRef = ref()
const uploading = ref(false)
const submitting = ref(false)

// 表单数据
const formData = ref<AddItemRequest & { id?: number }>({
  title: '',
  description: '',
  mediaKind: 'IMAGE',
  mediaUrl: '',
  coverUrl: '',
  startTime: '',
  endTime: ''
})

// 表单验证规则
const formRules = {
  title: [{ required: true, message: '请输入展品标题', trigger: 'blur' }],
  mediaKind: [{ required: true, message: '请选择媒体类型', trigger: 'change' }],
  mediaUrl: [{ required: true, message: '请上传媒体文件', trigger: 'change' }]
}

// 上传配置
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL}/api/admin/oss/upload`
})

const uploadHeaders = computed(() => {
  const token = getToken()
  return {
    satoken: token || ''
  }
})

// 查询展品列表
const handleSearch = async () => {
  loading.value = true
  try {
    const result = await getAdminItems(queryParams.value)
    itemList.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('查询展品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    keyword: '',
    mediaKind: undefined
  }
  handleSearch()
}

// 新增展品
const handleAdd = () => {
  isEdit.value = false
  formData.value = {
    title: '',
    description: '',
    mediaKind: 'IMAGE',
    mediaUrl: '',
    coverUrl: '',
    startTime: '',
    endTime: ''
  }
  dialogVisible.value = true
}

// 编辑展品
const handleEdit = (row: ExhibitItem) => {
  isEdit.value = true
  formData.value = {
    id: row.id,
    title: row.title,
    description: row.description || '',
    mediaKind: row.mediaKind,
    mediaUrl: row.mediaUrl,
    coverUrl: row.coverUrl || '',
    startTime: row.startTime || '',
    endTime: row.endTime || ''
  }
  dialogVisible.value = true
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isVideo = file.type.startsWith('video/')

  if (!isImage && !isVideo) {
    ElMessage.error('只能上传图片或视频文件')
    return false
  }

  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }

  uploading.value = true
  return true
}

// 上传成功
const handleUploadSuccess = (response: any) => {
  uploading.value = false
  if (response.code === 0) {
    formData.value.mediaUrl = response.data.url
    formData.value.mediaKind = response.data.mediaKind
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = (error: any) => {
  uploading.value = false
  ElMessage.error('上传失败')
  console.error('上传失败:', error)
}

// 封面上传成功
const handleCoverUploadSuccess = (response: any) => {
  if (response.code === 0) {
    formData.value.coverUrl = response.data.url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value?.validate()

  submitting.value = true
  try {
    if (isEdit.value) {
      // 编辑
      const updateData: UpdateItemRequest = {
        id: formData.value.id!,
        title: formData.value.title,
        description: formData.value.description,
        mediaUrl: formData.value.mediaUrl,
        coverUrl: formData.value.coverUrl,
        startTime: formData.value.startTime || undefined,
        endTime: formData.value.endTime || undefined
      }
      await updateItem(updateData)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await addItem(formData.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    handleSearch()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.value = {
    title: '',
    description: '',
    mediaKind: 'IMAGE',
    mediaUrl: '',
    coverUrl: '',
    startTime: '',
    endTime: ''
  }
}

// 上架
const handlePublish = async (row: ExhibitItem) => {
  try {
    await ElMessageBox.confirm(`确定要上架展品 "${row.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await publishItem(row.id)
    ElMessage.success('上架成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('上架失败:', error)
    }
  }
}

// 下架
const handleUnpublish = async (row: ExhibitItem) => {
  try {
    await ElMessageBox.confirm(`确定要下架展品 "${row.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await unpublishItem(row.id)
    ElMessage.success('下架成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
}

// 删除
const handleDelete = async (row: ExhibitItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除展品 "${row.title}" 吗？此操作不可恢复！`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteItem(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化时间范围
const formatTimeRange = (item: ExhibitItem) => {
  const { startTime, endTime } = item
  if (!startTime && !endTime) return '长期展出'

  const start = startTime ? dayjs(startTime).format('YYYY-MM-DD') : '不限'
  const end = endTime ? dayjs(endTime).format('YYYY-MM-DD') : '不限'

  return `${start} ~ ${end}`
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.admin-item-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.search-form {
  margin-bottom: 20px;
}

.video-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.upload-demo {
  width: 100%;
}

.file-preview {
  margin-top: 12px;
}

.video-url {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 12px;
}
</style>
