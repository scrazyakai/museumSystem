<template>
  <AdminLayout>
    <div class="admin-booking-manage">
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">预约管理</span>
            <el-button type="primary" :icon="Check" @click="showVerifyDialog = true">
              核验入馆
            </el-button>
          </div>
        </template>

        <!-- 搜索栏 -->
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="参观日期">
            <el-date-picker
              v-model="queryParams.visitDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryParams.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <el-option label="已预约" :value="1" />
              <el-option label="已取消" :value="2" />
              <el-option label="已改签" :value="3" />
              <el-option label="已核验" :value="4" />
              <el-option label="已过期" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="票号">
            <el-input
              v-model="queryParams.ticketCode"
              placeholder="输入票号"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
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

        <!-- 预约表格 -->
        <el-table
          v-loading="loading"
          :data="bookingList"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="visitDate" label="参观日期" width="120" />
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
              {{ row.verifyTime ? formatDateTime(row.verifyTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="cancelReason" label="取消原因" min-width="150">
            <template #default="{ row }">
              {{ row.cancelReason || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  v-if="row.status === 1"
                  type="success"
                  size="small"
                  :icon="Check"
                  @click="handleQuickVerify(row)"
                >
                  核验
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  :icon="View"
                  @click="handleViewDetail(row)"
                >
                  详情
                </el-button>
              </div>
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
      </el-card>

      <!-- 核验对话框 -->
      <el-dialog
        v-model="showVerifyDialog"
        title="核验入馆"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form label-width="80px">
          <el-form-item label="票号">
            <el-input
              v-model="verifyForm.ticketCode"
              placeholder="请输入或扫描票号"
              clearable
              maxlength="32"
            />
          </el-form-item>
          <el-alert
            title="操作说明"
            type="info"
            :closable="false"
            show-icon
          >
            请输入参观人的票号进行核验，核验成功后允许入馆
          </el-alert>
        </el-form>
        <template #footer>
          <el-button @click="showVerifyDialog = false">取消</el-button>
          <el-button type="primary" @click="handleVerify">确定核验</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Check, View, Download } from '@element-plus/icons-vue'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import dayjs from 'dayjs'
import {
  getAllBookings,
  verifyBooking,
  type Booking
} from '@/api/admin-booking'
import { BookingStatusText, BookingStatusColor } from '@/api/booking'

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = ref({
  visitDate: '',
  status: undefined as number | undefined,
  ticketCode: ''
})

// 预约列表
const bookingList = ref<Booking[]>([])

// 分页信息
const pagination = ref({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

// 核验对话框
const showVerifyDialog = ref(false)
const verifyForm = ref({
  ticketCode: ''
})

onMounted(() => {
  loadBookingList()
})

// 加载预约列表
const loadBookingList = async () => {
  loading.value = true
  try {
    // 构建请求参数，过滤掉空值
    const requestData: any = {
      page: pagination.value.current,
      size: pagination.value.size
    }

    // 只添加有值的筛选参数
    if (queryParams.value.visitDate) {
      requestData.visitDate = queryParams.value.visitDate
    }
    if (queryParams.value.status !== undefined && queryParams.value.status !== null) {
      requestData.status = queryParams.value.status
    }
    if (queryParams.value.ticketCode) {
      requestData.ticketCode = queryParams.value.ticketCode
    }

    const result = await getAllBookings(requestData)
    bookingList.value = result.records
    pagination.value.total = result.total
    pagination.value.pages = result.pages
  } catch (error) {
    console.error('加载预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadBookingList()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    visitDate: '',
    status: undefined,
    ticketCode: ''
  }
  handleSearch()
}

// 分页改变
const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadBookingList()
}

// 快速核验
const handleQuickVerify = async (booking: Booking) => {
  try {
    await ElMessageBox.confirm(
      `确定要核验票号 ${booking.ticketCode} 吗？`,
      '核验确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await verifyBooking({ ticketCode: booking.ticketCode })
    ElMessage.success('核验成功')
    loadBookingList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('核验失败:', error)
    }
  }
}

// 核验
const handleVerify = async () => {
  if (!verifyForm.value.ticketCode) {
    ElMessage.warning('请输入票号')
    return
  }

  try {
    await verifyBooking({ ticketCode: verifyForm.value.ticketCode })
    ElMessage.success('核验成功')
    showVerifyDialog.value = false
    verifyForm.value.ticketCode = ''
    loadBookingList()
  } catch (error) {
    console.error('核验失败:', error)
  }
}

// 查看详情
const handleViewDetail = (booking: Booking) => {
  ElMessageBox.alert(`
    <div style="line-height: 2;">
      <p><strong>预约ID：</strong>${booking.id}</p>
      <p><strong>参观日期：</strong>${booking.visitDate}</p>
      <p><strong>票号：</strong>${booking.ticketCode}</p>
      <p><strong>状态：</strong>${BookingStatusText[booking.status]}</p>
      <p><strong>核验时间：</strong>${booking.verifyTime ? formatDateTime(booking.verifyTime) : '未核验'}</p>
      <p><strong>取消原因：</strong>${booking.cancelReason || '无'}</p>
      <p><strong>创建时间：</strong>${formatDateTime(booking.createdAt)}</p>
      <p><strong>更新时间：</strong>${formatDateTime(booking.updatedAt)}</p>
    </div>
  `, '预约详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.admin-booking-manage {
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

.search-form {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
