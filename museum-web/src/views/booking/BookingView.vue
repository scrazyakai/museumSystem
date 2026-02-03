<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElButton,
  ElDatePicker,
  ElTable,
  ElTableColumn,
  ElTag,
  ElDialog,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElEmpty,
  ElDropdown,
  ElDropdownMenu
} from 'element-plus'
import { Calendar, Refresh, Delete, Tickets, UserFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  createBooking,
  getMyBookings,
  cancelBooking,
  rescheduleBooking,
  createBatchBookings,
  type Booking,
  type BatchCreateResult,
  BookingStatus,
  BookingStatusText,
  BookingStatusColor
} from '@/api/booking'
import { getUserProfile, type UserProfile } from '@/api/user'
import { getQuotaInfo, type QuotaInfo } from '@/api/quota'
import { useAuthStore } from '@/store/auth'
import AppHeader from '@/components/common/AppHeader.vue'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = ref<UserProfile | null>(null)
const loading = ref(false)

// 创建预约相关
const showCreateDialog = ref(false)
const selectedDate = ref('')
const quotaInfo = ref<QuotaInfo | null>(null)
const quotaLoading = ref(false)
const dateOptions = {
  disabledDate(time: Date) {
    // 不能选择过去的日期
    return dayjs(time).isBefore(dayjs().startOf('day'))
  }
}

// 预约列表
const bookingList = ref<Booking[]>([])
const pagination = ref({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

// 改签相关
const showRescheduleDialog = ref(false)
const rescheduleBookingId = ref<number | null>(null)
const rescheduleDate = ref('')

// 取消相关
const showCancelDialog = ref(false)
const cancelBookingId = ref<number | null>(null)
const cancelReason = ref('')

// 团体预约相关
const showBatchDialog = ref(false)
const batchDate = ref('')
const batchQuotaInfo = ref<QuotaInfo | null>(null)
const selectedUsers = ref<number[]>([])
const batchLoading = ref(false)
const batchResult = ref<BatchCreateResult | null>(null)

// 团体预约人员列表
interface PersonInfo {
  id?: number
  realName: string
  idNo: string
  phone: string
}
const personList = ref<PersonInfo[]>([])

onMounted(async () => {
  await loadUserProfile()
  loadBookingList()
})

// 加载用户信息
const loadUserProfile = async () => {
  try {
    const data = await getUserProfile()
    userInfo.value = data
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载预约列表
const loadBookingList = async () => {
  loading.value = true
  try {
    const result = await getMyBookings({
      page: pagination.value.current,
      size: pagination.value.size
    })
    bookingList.value = result.records
    pagination.value.total = result.total
    pagination.value.pages = result.pages
  } catch (error) {
    console.error('加载预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载库存信息
const loadQuotaInfo = async (date: string) => {
  if (!date) {
    quotaInfo.value = null
    return
  }
  quotaLoading.value = true
  try {
    const data = await getQuotaInfo(date)
    quotaInfo.value = data
  } catch (error) {
    console.error('加载库存信息失败:', error)
    quotaInfo.value = null
  } finally {
    quotaLoading.value = false
  }
}

// 分页改变
const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadBookingList()
}

// 打开创建预约对话框
const openCreateDialog = () => {
  if (!userInfo.value?.realName) {
    ElMessage.warning('请先完成实名认证')
    router.push('/profile')
    return
  }
  selectedDate.value = ''
  quotaInfo.value = null
  showCreateDialog.value = true
}

// 监听日期选择变化，加载库存信息
const handleDateChange = (date: string) => {
  if (date) {
    loadQuotaInfo(date)
  } else {
    quotaInfo.value = null
  }
}

// 创建预约
const handleCreateBooking = async () => {
  if (!selectedDate.value) {
    ElMessage.warning('请选择参观日期')
    return
  }

  if (!quotaInfo.value || quotaInfo.value.remainingCount <= 0) {
    ElMessage.warning('该日期已无剩余名额，请选择其他日期')
    return
  }

  try {
    await createBooking({
      visitDate: selectedDate.value
    })
    ElMessage.success('预约成功')
    showCreateDialog.value = false
    loadBookingList()
  } catch (error) {
    console.error('创建预约失败:', error)
  }
}

// 打开改签对话框
const openRescheduleDialog = (booking: Booking) => {
  if (booking.status !== BookingStatus.BOOKED) {
    ElMessage.warning('只有已预约状态才能改签')
    return
  }
  rescheduleBookingId.value = booking.id
  rescheduleDate.value = ''
  showRescheduleDialog.value = true
}

// 改签预约
const handleReschedule = async () => {
  if (!rescheduleDate.value) {
    ElMessage.warning('请选择新的参观日期')
    return
  }

  try {
    await rescheduleBooking({
      bookingId: rescheduleBookingId.value!,
      newVisitDate: rescheduleDate.value
    })
    ElMessage.success('改签成功')
    showRescheduleDialog.value = false
    loadBookingList()
  } catch (error) {
    console.error('改签失败:', error)
  }
}

// 打开取消预约对话框
const openCancelDialog = (booking: Booking) => {
  if (booking.status !== BookingStatus.BOOKED && booking.status !== BookingStatus.RESCHEDULED) {
    ElMessage.warning('该预约状态不允许取消')
    return
  }
  cancelBookingId.value = booking.id
  cancelReason.value = ''
  showCancelDialog.value = true
}

// 取消预约
const handleCancel = async () => {
  try {
    await cancelBooking({
      bookingId: cancelBookingId.value!,
      cancelReason: cancelReason.value || '个人原因'
    })
    ElMessage.success('取消成功')
    showCancelDialog.value = false
    loadBookingList()
  } catch (error) {
    console.error('取消预约失败:', error)
  }
}

// 打开团体预约对话框
const openBatchDialog = () => {
  if (!userInfo.value?.realName) {
    ElMessage.warning('请先完成实名认证')
    router.push('/profile')
    return
  }
  batchDate.value = ''
  batchQuotaInfo.value = null
  personList.value = [{ realName: '', idNo: '', phone: '' }]
  batchResult.value = null
  showBatchDialog.value = true
}

// 添加预约人
const addPerson = () => {
  personList.value.push({ realName: '', idNo: '', phone: '' })
}

// 删除预约人
const removePerson = (index: number) => {
  if (personList.value.length > 1) {
    personList.value.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个预约人')
  }
}

// 团体预约
const handleBatchCreate = async () => {
  if (!batchDate.value) {
    ElMessage.warning('请选择参观日期')
    return
  }

  if (!batchQuotaInfo.value || batchQuotaInfo.value.remainingCount <= 0) {
    ElMessage.warning('该日期已无剩余名额')
    return
  }

  // 验证表单
  const validPersons = personList.value.filter(p => p.realName && p.idNo && p.phone)
  if (validPersons.length === 0) {
    ElMessage.warning('请至少填写一个完整的预约人信息')
    return
  }

  if (validPersons.length > batchQuotaInfo.value.remainingCount) {
    ElMessage.warning(`剩余名额不足，仅剩 ${batchQuotaInfo.value.remainingCount} 个`)
    return
  }

  // 验证身份证号和手机号格式
  for (const person of validPersons) {
    const idReg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    const phoneReg = /^1[3-9]\d{9}$/

    if (!idReg.test(person.idNo)) {
      ElMessage.warning(`请检查身份证号格式：${person.realName}`)
      return
    }
    if (!phoneReg.test(person.phone)) {
      ElMessage.warning(`请检查手机号格式：${person.realName}`)
      return
    }
  }

  batchLoading.value = true
  try {
    // 这里需要先通过手机号或身份证号查找用户ID
    // 暂时使用当前用户的ID（实际应该调用后端接口查找）
    const userIds = validPersons.map(() => authStore.userInfo!.id)

    const result = await createBatchBookings({
      visitDate: batchDate.value,
      userIds
    })
    batchResult.value = result

    if (result.failCount === 0) {
      ElMessage.success(`成功预约 ${result.successCount} 人`)
      showBatchDialog.value = false
      loadBookingList()
    } else {
      ElMessage.warning(`成功 ${result.successCount} 人，失败 ${result.failCount} 人`)
    }
  } catch (error) {
    console.error('团体预约失败:', error)
    ElMessage.error('团体预约失败，请确保所有用户已注册并完成实名认证')
  } finally {
    batchLoading.value = false
  }
}
</script>

<template>
  <div class="booking-container">
    <!-- Header -->
    <AppHeader />

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 创建预约卡片 -->
      <el-card class="booking-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon :size="20" color="#b03128"><Calendar /></el-icon>
            <span class="card-title">创建新预约</span>
          </div>
        </template>
        <div class="create-booking-content">
          <div class="user-info">
            <div class="info-label">参观人信息</div>
            <div class="info-item">
              <span class="label">姓名：</span>
              <span class="value">{{ userInfo?.realName || '未实名认证' }}</span>
            </div>
            <div class="info-item">
              <span class="label">身份证号：</span>
              <span class="value">{{ userInfo?.idNo || '未实名认证' }}</span>
            </div>
          </div>
          <div class="action-section">
            <div class="button-group">
              <el-button
                type="danger"
                size="large"
                :icon="Calendar"
                @click="openCreateDialog"
              >
                立即预约
              </el-button>
              <el-button
                type="primary"
                size="large"
                :icon="UserFilled"
                @click="openBatchDialog"
              >
                团体预约
              </el-button>
            </div>
            <div class="tip-text">每日限额 2000 人，建议提前预约</div>
          </div>
        </div>
      </el-card>

      <!-- 我的预约列表 -->
      <el-card class="booking-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon :size="20" color="#b03128"><Tickets /></el-icon>
            <span class="card-title">我的预约</span>
            <el-button
              type="primary"
              size="small"
              :icon="Refresh"
              @click="loadBookingList"
              :loading="loading"
            >
              刷新
            </el-button>
          </div>
        </template>

        <div v-if="bookingList.length > 0" class="table-container">
          <el-table :data="bookingList" v-loading="loading" stripe>
            <el-table-column prop="visitDate" label="参观日期" width="150" />
            <el-table-column prop="ticketCode" label="票号" min-width="200">
              <template #default="{ row }">
                <el-tag type="info" size="small">{{ row.ticketCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="BookingStatusColor[row.status]">
                  {{ BookingStatusText[row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="verifyTime" label="核验时间" width="180">
              <template #default="{ row }">
                {{ row.verifyTime ? dayjs(row.verifyTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === BookingStatus.BOOKED"
                  type="primary"
                  size="small"
                  @click="openRescheduleDialog(row)"
                >
                  改签
                </el-button>
                <el-button
                  v-if="row.status === BookingStatus.BOOKED || row.status === BookingStatus.RESCHEDULED"
                  type="danger"
                  size="small"
                  @click="openCancelDialog(row)"
                >
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :total="pagination.total"
              :page-size="pagination.size"
              :current-page="pagination.current"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <el-empty v-else description="暂无预约记录" />
      </el-card>
    </div>

    <!-- 创建预约对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建预约"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-position="top">
        <el-form-item label="选择参观日期">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="请选择参观日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="dateOptions.disabledDate"
            @change="handleDateChange"
            style="width: 100%"
          />
        </el-form-item>
        <div v-if="quotaLoading && selectedDate" class="quota-loading">
          <el-icon class="is-loading"><Refresh /></el-icon>
          <span>加载库存信息中...</span>
        </div>
        <div v-if="quotaInfo && selectedDate" class="quota-info">
          <el-alert
            :title="`剩余名额: ${quotaInfo.remainingCount} / 总名额: ${quotaInfo.capacity}`"
            :type="quotaInfo.remainingCount > 1000 ? 'success' : quotaInfo.remainingCount > 0 ? 'warning' : 'error'"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="quota-details">
                <div>已预约: {{ quotaInfo.reservedCount }} 人</div>
                <div>剩余: {{ quotaInfo.remainingCount }} 人</div>
                <el-progress
                  :percentage="Math.round((quotaInfo.reservedCount / quotaInfo.capacity) * 100)"
                  :status="quotaInfo.remainingCount > 1000 ? 'success' : quotaInfo.remainingCount > 0 ? 'warning' : 'exception'"
                />
              </div>
            </template>
          </el-alert>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateBooking">确定预约</el-button>
      </template>
    </el-dialog>

    <!-- 改签对话框 -->
    <el-dialog
      v-model="showRescheduleDialog"
      title="改签预约"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-position="top">
        <el-form-item label="选择新的参观日期">
          <el-date-picker
            v-model="rescheduleDate"
            type="date"
            placeholder="请选择新的参观日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="dateOptions.disabledDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-alert
          title="温馨提示"
          type="warning"
          :closable="false"
          show-icon
        >
          只能改签一次，请谨慎操作
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showRescheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReschedule">确定改签</el-button>
      </template>
    </el-dialog>

    <!-- 取消预约对话框 -->
    <el-dialog
      v-model="showCancelDialog"
      title="取消预约"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-position="top">
        <el-form-item label="取消原因（选填）">
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-alert
          title="温馨提示"
          type="warning"
          :closable="false"
          show-icon
        >
          取消预约后，如需参观需要重新预约
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showCancelDialog = false">取消</el-button>
        <el-button type="danger" @click="handleCancel">确定取消</el-button>
      </template>
    </el-dialog>

    <!-- 团体预约对话框 -->
    <el-dialog
      v-model="showBatchDialog"
      title="团体预约"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form label-position="top">
        <el-form-item label="选择参观日期">
          <el-date-picker
            v-model="batchDate"
            type="date"
            placeholder="请选择参观日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="dateOptions.disabledDate"
            @change="handleDateChange"
            style="width: 100%"
          />
        </el-form-item>

        <div v-if="batchLoading && batchDate" class="quota-loading">
          <el-icon class="is-loading"><Refresh /></el-icon>
          <span>加载库存信息中...</span>
        </div>

        <div v-if="batchQuotaInfo && batchDate" class="quota-info">
          <el-alert
            :title="`剩余名额: ${batchQuotaInfo.remainingCount} / 总名额: ${batchQuotaInfo.capacity}`"
            :type="batchQuotaInfo.remainingCount > 1000 ? 'success' : batchQuotaInfo.remainingCount > 0 ? 'warning' : 'error'"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="quota-details">
                <div>已预约: {{ batchQuotaInfo.reservedCount }} 人</div>
                <div>剩余: {{ batchQuotaInfo.remainingCount }} 人</div>
              </div>
            </template>
          </el-alert>
        </div>

        <el-divider content-position="left">预约人员信息</el-divider>

        <div v-for="(person, index) in personList" :key="index" class="person-form-item">
          <div class="person-header">
            <span class="person-title">预约人 {{ index + 1 }}</span>
            <el-button
              v-if="personList.length > 1"
              type="danger"
              size="small"
              :icon="Delete"
              @click="removePerson(index)"
              text
            >
              删除
            </el-button>
          </div>
          <el-form :model="person" label-position="top">
            <el-form-item label="真实姓名" required>
              <el-input
                v-model="person.realName"
                placeholder="请输入真实姓名"
                clearable
              />
            </el-form-item>
            <el-form-item label="身份证号" required>
              <el-input
                v-model="person.idNo"
                placeholder="请输入身份证号"
                clearable
                maxlength="18"
              />
            </el-form-item>
            <el-form-item label="手机号" required>
              <el-input
                v-model="person.phone"
                placeholder="请输入手机号"
                clearable
                maxlength="11"
              />
            </el-form-item>
          </el-form>
        </div>

        <el-button
          type="primary"
          :icon="Plus"
          @click="addPerson"
          style="width: 100%; margin-top: 12px"
        >
          添加预约人
        </el-button>

        <el-alert
          v-if="batchQuotaInfo && personList.filter(p => p.realName && p.idNo && p.phone).length > batchQuotaInfo.remainingCount"
          title="警告"
          type="error"
          :closable="false"
          show-icon
          style="margin-top: 16px"
        >
          填写完整信息的人数（{{ personList.filter(p => p.realName && p.idNo && p.phone).length }}）超过剩余名额（{{ batchQuotaInfo.remainingCount }}）
        </el-alert>

        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 12px"
        >
          请确保每位预约人都已注册并完成实名认证，系统将通过身份证号和手机号匹配用户
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleBatchCreate"
          :loading="batchLoading"
          :disabled="!batchDate || !batchQuotaInfo"
        >
          确定预约（{{ personList.filter(p => p.realName && p.idNo && p.phone).length }}人）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.booking-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 主内容区 */
.main-content {
  width: 100%;
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 40px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.booking-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  justify-content: space-between;
}

/* 创建预约卡片 */
.create-booking-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 40px;
  padding: 20px 0;
}

.user-info {
  flex: 1;
}

.info-label {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.info-item {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.info-item .label {
  font-weight: bold;
  margin-right: 8px;
  min-width: 80px;
}

.info-item .value {
  color: #333;
}

.action-section {
  flex: 0 0 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.button-group {
  display: flex;
  gap: 12px;
  width: 100%;
}

.button-group .el-button {
  flex: 1;
}

.tip-text {
  font-size: 12px;
  color: #999;
  text-align: center;
}

/* 表格容器 */
.table-container {
  padding: 10px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 库存信息样式 */
.quota-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
  color: #999;
  font-size: 14px;
}

.quota-info {
  padding: 12px 0;
}

.quota-details {
  margin-top: 12px;
}

.quota-details > div {
  font-size: 14px;
  margin-bottom: 8px;
  color: #666;
}

.user-count-hint {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.user-count-hint strong {
  color: #409eff;
  font-size: 16px;
}

.person-form-item {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.person-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.person-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content,
  .main-content {
    padding: 0 20px;
  }

  .create-booking-content {
    flex-direction: column;
  }

  .action-section {
    width: 100%;
  }
}
</style>
