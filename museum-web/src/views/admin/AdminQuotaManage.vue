<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import {
  ElCard,
  ElButton,
  ElDatePicker,
  ElInput,
  ElForm,
  ElFormItem,
  ElAlert,
  ElMessage,
  ElIcon,
  ElProgress,
  ElDescriptions,
  ElDescriptionsItem
} from 'element-plus'
import { Calendar, Refresh, Edit, Check, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getQuotaInfo, updateQuota, createFutureQuota, type QuotaInfo } from '@/api/quota'

// 日期选择
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const quotaInfo = ref<QuotaInfo | null>(null)
const loading = ref(false)

// 编辑相关
const showEditDialog = ref(false)
const editForm = ref({
  capacity: 0
})
const editLoading = ref(false)

// 创建库存相关
const showCreateDialog = ref(false)
const createForm = ref({
  days: 7
})
const createLoading = ref(false)

// 日期选项
const dateOptions = {
  disabledDate(time: Date) {
    // 可以选择今天及未来30天的日期
    return dayjs(time).isBefore(dayjs().startOf('day')) ||
           dayjs(time).isAfter(dayjs().add(30, 'day'))
  }
}

onMounted(() => {
  loadQuotaInfo()
})

// 加载库存信息
const loadQuotaInfo = async () => {
  if (!selectedDate.value) {
    quotaInfo.value = null
    return
  }
  loading.value = true
  try {
    const data = await getQuotaInfo(selectedDate.value)
    quotaInfo.value = data
    editForm.value.capacity = data.capacity
  } catch (error) {
    console.error('加载库存信息失败:', error)
    quotaInfo.value = null
  } finally {
    loading.value = false
  }
}

// 日期改变
const handleDateChange = () => {
  loadQuotaInfo()
}

// 打开编辑对话框
const openEditDialog = () => {
  if (!quotaInfo.value) {
    ElMessage.warning('请先选择日期')
    return
  }
  editForm.value.capacity = quotaInfo.value.capacity
  showEditDialog.value = true
}

// 确认更新
const handleUpdate = async () => {
  if (!editForm.value.capacity || editForm.value.capacity <= 0) {
    ElMessage.warning('请输入有效的库存数量')
    return
  }

  if (!quotaInfo.value) {
    return
  }

  // 检查新的库存是否小于已预约数量
  if (editForm.value.capacity < quotaInfo.value.reservedCount) {
    ElMessage.warning(`库存不能小于已预约数量（${quotaInfo.value.reservedCount}）`)
    return
  }

  editLoading.value = true
  try {
    await updateQuota({
      visitDate: selectedDate.value,
      capacity: editForm.value.capacity
    })
    ElMessage.success('更新库存成功')
    showEditDialog.value = false
    loadQuotaInfo()
  } catch (error) {
    console.error('更新库存失败:', error)
  } finally {
    editLoading.value = false
  }
}

// 格式化日期
const formatDisplayDate = (date: string) => {
  return dayjs(date).format('YYYY年MM月DD日')
}

// 打开创建库存对话框
const openCreateDialog = () => {
  createForm.value.days = 7
  showCreateDialog.value = true
}

// 确认创建未来库存
const handleCreateFuture = async () => {
  if (!createForm.value.days || createForm.value.days <= 0 || createForm.value.days > 30) {
    ElMessage.warning('天数必须在1-30之间')
    return
  }

  createLoading.value = true
  try {
    const result = await createFutureQuota(createForm.value.days)
    ElMessage.success(result)
    showCreateDialog.value = false
    // 刷新当前日期的库存信息
    loadQuotaInfo()
  } catch (error) {
    console.error('创建库存失败:', error)
  } finally {
    createLoading.value = false
  }
}
</script>

<template>
  <AdminLayout>
    <div class="admin-quota-manage">
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">库存管理</span>
            <div class="header-actions">
              <el-button type="success" :icon="Plus" @click="openCreateDialog">
                创建未来库存
              </el-button>
              <el-button type="primary" :icon="Edit" @click="openEditDialog" :disabled="!quotaInfo">
                修改库存
              </el-button>
            </div>
          </div>
        </template>

        <!-- 日期选择和刷新 -->
        <div class="date-selector">
          <el-form :inline="true">
            <el-form-item label="选择日期">
              <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="请选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="dateOptions.disabledDate"
                @change="handleDateChange"
                style="width: 150px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Refresh" @click="loadQuotaInfo" :loading="loading">
                刷新
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 库存信息展示 -->
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" :size="32"><Refresh /></el-icon>
          <span>加载中...</span>
        </div>

        <div v-else-if="quotaInfo" class="quota-display">
          <el-alert
            :title="`${formatDisplayDate(selectedDate)} 库存信息`"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="quota-content">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="总名额">
                    <span class="quota-number">{{ quotaInfo.capacity }}</span> 人
                  </el-descriptions-item>
                  <el-descriptions-item label="已预约">
                    <span class="quota-number booked">{{ quotaInfo.reservedCount }}</span> 人
                  </el-descriptions-item>
                  <el-descriptions-item label="剩余名额">
                    <span
                      class="quota-number"
                      :class="{
                        'success': quotaInfo.remainingCount > 1000,
                        'warning': quotaInfo.remainingCount > 0 && quotaInfo.remainingCount <= 1000,
                        'error': quotaInfo.remainingCount === 0
                      }"
                    >
                      {{ quotaInfo.remainingCount }}
                    </span> 人
                  </el-descriptions-item>
                  <el-descriptions-item label="预约率">
                    <span class="quota-percentage">
                      {{ Math.round((quotaInfo.reservedCount / quotaInfo.capacity) * 100) }}%
                    </span>
                  </el-descriptions-item>
                </el-descriptions>

                <div class="progress-section">
                  <div class="progress-label">预约进度</div>
                  <el-progress
                    :percentage="Math.round((quotaInfo.reservedCount / quotaInfo.capacity) * 100)"
                    :status="quotaInfo.remainingCount > 1000 ? 'success' : quotaInfo.remainingCount > 0 ? 'warning' : 'exception'"
                    :stroke-width="20"
                  />
                  <div class="progress-text">
                    已预约 {{ quotaInfo.reservedCount }} / {{ quotaInfo.capacity }}
                  </div>
                </div>

                <div class="tips-section">
                  <el-alert
                    v-if="quotaInfo.remainingCount === 0"
                    title="提示"
                    type="error"
                    :closable="false"
                    show-icon
                  >
                    该日期已无可预约名额，请及时增加库存或关闭预约
                  </el-alert>
                  <el-alert
                    v-else-if="quotaInfo.remainingCount <= 1000"
                    title="提示"
                    type="warning"
                    :closable="false"
                    show-icon
                  >
                    剩余名额不足 1000 人，建议关注预约情况
                  </el-alert>
                  <el-alert
                    v-else
                    title="提示"
                    type="success"
                    :closable="false"
                    show-icon
                  >
                    库存充足，可以正常预约
                  </el-alert>
                </div>
              </div>
            </template>
          </el-alert>
        </div>

        <div v-else class="empty-container">
          <el-empty description="请选择日期查看库存信息" />
        </div>
      </el-card>

      <!-- 编辑库存对话框 -->
      <el-dialog
        v-model="showEditDialog"
        title="修改库存"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form :model="editForm" label-width="100px">
          <el-form-item label="选择日期">
            <el-input :value="formatDisplayDate(selectedDate)" disabled />
          </el-form-item>
          <el-form-item label="当前库存">
            <el-input :value="quotaInfo?.capacity" disabled>
              <template #append>人</template>
            </el-input>
          </el-form-item>
          <el-form-item label="已预约">
            <el-input :value="quotaInfo?.reservedCount" disabled>
              <template #append>人</template>
            </el-input>
          </el-form-item>
          <el-form-item label="新库存" required>
            <el-input
              v-model.number="editForm.capacity"
              type="number"
              :min="quotaInfo?.reservedCount || 0"
              placeholder="请输入新的库存数量"
            >
              <template #append>人</template>
            </el-input>
          </el-form-item>
          <el-alert
            v-if="editForm.capacity < (quotaInfo?.reservedCount || 0)"
            title="错误"
            type="error"
            :closable="false"
            show-icon
          >
            库存数量不能小于已预约数量
          </el-alert>
          <el-alert
            title="温馨提示"
            type="warning"
            :closable="false"
            show-icon
          >
            修改库存会影响用户预约，请谨慎操作
          </el-alert>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleUpdate"
            :loading="editLoading"
            :disabled="editForm.capacity < (quotaInfo?.reservedCount || 0)"
          >
            确定修改
          </el-button>
        </template>
      </el-dialog>

      <!-- 创建未来库存对话框 -->
      <el-dialog
        v-model="showCreateDialog"
        title="创建未来库存"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form :model="createForm" label-width="100px">
          <el-form-item label="创建天数" required>
            <el-input
              v-model.number="createForm.days"
              type="number"
              :min="1"
              :max="30"
              placeholder="请输入要创建的天数"
            >
              <template #append>天</template>
            </el-input>
          </el-form-item>
          <el-alert
            title="说明"
            type="info"
            :closable="false"
            show-icon
          >
            将从明天开始创建指定天数的库存，每天库存容量为 2000 人
          </el-alert>
          <el-alert
            title="温馨提示"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 12px"
          >
            天数必须在 1-30 之间，且不会覆盖已存在的库存
          </el-alert>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleCreateFuture"
            :loading="createLoading"
          >
            确定创建
          </el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<style scoped>
.admin-quota-manage {
  padding: 20px;
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

.header-actions {
  display: flex;
  gap: 12px;
}

.date-selector {
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #999;
  gap: 16px;
}

.quota-display {
  padding: 20px 0;
}

.quota-content {
  margin-top: 16px;
}

.quota-number {
  font-size: 20px;
  font-weight: bold;
  margin-right: 4px;
}

.quota-number.success {
  color: #67c23a;
}

.quota-number.warning {
  color: #e6a23c;
}

.quota-number.error {
  color: #f56c6c;
}

.quota-number.booked {
  color: #409eff;
}

.quota-percentage {
  font-size: 16px;
  font-weight: bold;
}

.progress-section {
  margin-top: 24px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.progress-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.progress-text {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
  text-align: center;
}

.tips-section {
  margin-top: 20px;
}

.empty-container {
  padding: 60px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-quota-manage {
    padding: 10px;
  }
}
</style>
