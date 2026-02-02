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
import { Calendar, ArrowLeft, Refresh, Delete,Tickets, UserFilled, SwitchButton } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  createBooking,
  getMyBookings,
  cancelBooking,
  rescheduleBooking,
  type Booking,
  BookingStatus,
  BookingStatusText,
  BookingStatusColor
} from '@/api/booking'
import { getUserProfile, type UserProfile } from '@/api/user'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = ref<UserProfile | null>(null)
const loading = ref(false)

// 创建预约相关
const showCreateDialog = ref(false)
const selectedDate = ref('')
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
  showCreateDialog.value = true
}

// 创建预约
const handleCreateBooking = async () => {
  if (!selectedDate.value) {
    ElMessage.warning('请选择参观日期')
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

// 返回首页
const goBack = () => {
  router.push('/home')
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    handleLogout()
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 清除本地状态
    authStore.clearUser()
    ElMessage.success('已退出登录')
    router.push('/login/user')
  } catch {
    // 用户取消
  }
}
</script>

<template>
  <div class="booking-container">
    <!-- Header -->
    <header class="header">
      <div class="header-content">
        <div class="logo-section">
          <el-button link @click="goBack" class="back-button">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回首页</span>
          </el-button>
        </div>
        <div class="logo-section">
          <el-icon :size="28" color="#b03128"><Tickets /></el-icon>
          <span class="logo-text">参观预约</span>
        </div>
        <div class="header-actions">
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-dropdown-info">
              <el-avatar v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" :size="32" />
              <el-avatar v-else :icon="UserFilled" :size="32" />
              <span class="username">{{ userInfo?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

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
            <el-button
              type="danger"
              size="large"
              :icon="Calendar"
              @click="openCreateDialog"
            >
              立即预约
            </el-button>
            <div class="tip-text">每日限额 5000 人，建议提前预约</div>
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
            style="width: 100%"
          />
        </el-form-item>
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
  </div>
</template>

<style scoped>
.booking-container {
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

.back-button {
  color: rgba(255, 255, 255, 0.9);
  padding: 8px 12px;
}

.back-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.logo-text {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.header-actions .el-button {
  color: rgba(255, 255, 255, 0.9);
  border: none;
}

.header-actions .el-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.user-dropdown {
  margin-left: 0;
}

.user-dropdown-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
  color: rgba(255, 255, 255, 0.9);
}

.user-dropdown-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-info {
  flex: 1;
}

.username {
  font-size: 14px;
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
