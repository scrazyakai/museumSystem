<template>
  <AdminLayout>
    <div class="admin-home">
        <!-- 管理员信息卡片 -->
        <el-card class="admin-info-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">管理员信息</span>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="管理员ID">
              {{ userInfo?.id }}
            </el-descriptions-item>
            <el-descriptions-item label="管理员账号">
              {{ userInfo?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="昵称">
              {{ userInfo?.nickname || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag type="danger" size="large">
                {{ userInfo?.role }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="头像" :span="2">
              <el-avatar v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" :size="60" />
              <el-avatar v-else icon="UserFilled" :size="60" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 新的统计数据卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card today-booking">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon :size="40"><Tickets /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">今日预约</div>
                  <div class="stat-value">{{ dashboardStats.todayBookedCount }}</div>
                  <div class="stat-extra">
                    总配额: {{ dashboardStats.todayTotalQuota }}
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card monthly-booking">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon :size="40"><Calendar /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">本月预约</div>
                  <div class="stat-value">{{ dashboardStats.monthlyBookingCount }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card users">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon :size="40"><UserFilled /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">总用户数</div>
                  <div class="stat-value">{{ dashboardStats.totalUserCount }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card comments">
              <div class="stat-content">
                <div class="stat-icon">
                  <el-icon :size="40"><ChatLineRound /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">评论统计</div>
                  <div class="stat-value">{{ dashboardStats.totalCommentCount }}</div>
                  <div class="stat-extra" v-if="dashboardStats.pendingCommentCount > 0">
                    待审核: {{ dashboardStats.pendingCommentCount }}
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 数据可视化图表 -->
        <el-row :gutter="20" class="charts-row">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <span class="card-title">用户状态分布</span>
              </template>
              <div ref="statusChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <span class="card-title">用户角色分布</span>
              </template>
              <div ref="roleChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 新增的详细统计图表 -->
        <el-row :gutter="20" class="detail-row">
          <el-col :span="8">
            <el-card class="detail-card">
              <template #header>
                <div class="card-header">
                  <el-icon><DataLine /></el-icon>
                  <span class="card-title">今日预约详情</span>
                </div>
              </template>
              <div ref="todayBookingChartRef" class="chart-container"></div>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card class="detail-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Checked /></el-icon>
                  <span class="card-title">实名认证统计</span>
                </div>
              </template>
              <div ref="verifiedChartRef" class="chart-container"></div>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card class="detail-card">
              <template #header>
                <div class="card-header">
                  <el-icon><TrendCharts /></el-icon>
                  <span class="card-title">预约概览</span>
                </div>
              </template>
              <div ref="overviewChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import {
  UserFilled,
  CircleClose,
  Tickets,
  Calendar,
  ChatLineRound,
  DataLine,
  Checked,
  TrendCharts
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { logout } from '@/api/auth'
import {
  getUserList,
  type UserInfo,
  type UserQueryParams
} from '@/api/admin'
import { getDashboardStats, type DashboardStats } from '@/api/admin-dashboard'
import AdminLayout from '@/components/admin/AdminLayout.vue'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = computed(() => authStore.userInfo)

// 用户统计数据
const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  bannedUsers: 0
})

// 仪表板统计数据
const dashboardStats = ref<DashboardStats>({
  todayBookedCount: 0,
  todayTotalQuota: 0,
  totalCommentCount: 0,
  pendingCommentCount: 0,
  totalUserCount: 0,
  monthlyBookingCount: 0,
  todayVerifiedCount: 0,
  todayCancelledCount: 0
})

// 用户列表
const userList = ref<UserInfo[]>([])

// 图表引用
const statusChartRef = ref<HTMLElement | null>(null)
const roleChartRef = ref<HTMLElement | null>(null)
const todayBookingChartRef = ref<HTMLElement | null>(null)
const verifiedChartRef = ref<HTMLElement | null>(null)
const overviewChartRef = ref<HTMLElement | null>(null)

// 图表实例
let statusChart: echarts.ECharts | null = null
let roleChart: echarts.ECharts | null = null
let todayBookingChart: echarts.ECharts | null = null
let verifiedChart: echarts.ECharts | null = null
let overviewChart: echarts.ECharts | null = null

// 初始化用户状态分布饼图
const initStatusChart = () => {
  if (!statusChartRef.value) return

  statusChart = echarts.init(statusChartRef.value)

  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '用户状态',
        type: 'pie',
        radius: '60%',
        data: [
          { value: statistics.value.activeUsers, name: '正常用户', itemStyle: { color: '#67c23a' } },
          { value: statistics.value.bannedUsers, name: '禁用用户', itemStyle: { color: '#f56c6c' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  statusChart.setOption(option)
}

// 初始化用户角色分布饼图
const initRoleChart = () => {
  if (!roleChartRef.value) return

  roleChart = echarts.init(roleChartRef.value)

  const adminCount = userList.value.filter((u: UserInfo) => u.role === 'ADMIN').length
  const userCount = userList.value.filter((u: UserInfo) => u.role === 'USER').length

  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '用户角色',
        type: 'pie',
        radius: '60%',
        data: [
          { value: adminCount, name: '管理员', itemStyle: { color: '#f56c6c' } },
          { value: userCount, name: '普通用户', itemStyle: { color: '#409eff' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  roleChart.setOption(option)
}

// 初始化今日预约图表
const initTodayBookingChart = () => {
  if (!todayBookingChartRef.value) return

  todayBookingChart = echarts.init(todayBookingChartRef.value)

  const bookedCount = dashboardStats.value.todayBookedCount
  const remainingQuota = dashboardStats.value.todayTotalQuota - bookedCount
  const cancelledCount = dashboardStats.value.todayCancelledCount

  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '今日预约',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: bookedCount, name: '已预约', itemStyle: { color: '#67c23a' } },
          { value: remainingQuota, name: '剩余配额', itemStyle: { color: '#409eff' } },
          { value: cancelledCount, name: '已取消', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }

  todayBookingChart.setOption(option)
}

// 初始化实名认证图表
const initVerifiedChart = () => {
  if (!verifiedChartRef.value) return

  verifiedChart = echarts.init(verifiedChartRef.value)

  const verifiedCount = dashboardStats.value.todayVerifiedCount
  const totalUsers = dashboardStats.value.totalUserCount

  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '实名认证',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: verifiedCount, name: '今日认证', itemStyle: { color: '#e6a23c' } },
          { value: totalUsers - verifiedCount, name: '未认证', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }

  verifiedChart.setOption(option)
}

// 初始化预约概览图表
const initOverviewChart = () => {
  if (!overviewChartRef.value) return

  overviewChart = echarts.init(overviewChartRef.value)

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['今日预约', '本月预约', '今日认证', '总评论', '待审核']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: [
          {
            value: dashboardStats.value.todayBookedCount,
            itemStyle: { color: '#67c23a' }
          },
          {
            value: dashboardStats.value.monthlyBookingCount,
            itemStyle: { color: '#409eff' }
          },
          {
            value: dashboardStats.value.todayVerifiedCount,
            itemStyle: { color: '#e6a23c' }
          },
          {
            value: dashboardStats.value.totalCommentCount,
            itemStyle: { color: '#909399' }
          },
          {
            value: dashboardStats.value.pendingCommentCount,
            itemStyle: { color: '#f56c6c' }
          }
        ],
        itemStyle: {
          borderRadius: [5, 5, 0, 0]
        },
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  }

  overviewChart.setOption(option)
}

// 获取用户统计数据
const loadUserStatistics = async () => {
  try {
    const result = await getUserList({
      page: 1,
      size: 1000 // 获取所有用户用于统计
    })

    userList.value = result.records

    // 更新统计数据
    statistics.value.totalUsers = result.total
    statistics.value.activeUsers = result.records.filter((u: UserInfo) => u.status === 0).length
    statistics.value.bannedUsers = result.records.filter((u: UserInfo) => u.status === 1).length

    // 更新图表
    nextTick(() => {
      initStatusChart()
      initRoleChart()
    })
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
  }
}

// 加载仪表板统计数据
const loadDashboardStats = async () => {
  try {
    const data = await getDashboardStats()
    dashboardStats.value = data

    // 更新图表
    nextTick(() => {
      initTodayBookingChart()
      initVerifiedChart()
      initOverviewChart()
    })
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
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

// 初始化
onMounted(() => {
  loadUserStatistics()
  loadDashboardStats()

  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    statusChart?.resize()
    roleChart?.resize()
    todayBookingChart?.resize()
    verifiedChart?.resize()
    overviewChart?.resize()
  })
})
</script>

<style scoped>
.admin-home {
  max-width: 1400px;
  margin: 0 auto;
}

.admin-info-card {
  margin-bottom: 24px;
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

/* 统计卡片行 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 12px;
}

.today-booking .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.monthly-booking .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.users .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.comments .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-extra {
  font-size: 12px;
  color: #67c23a;
}

/* 用户图表行 */
.charts-row {
  margin-bottom: 24px;
}

.chart-card {
  min-height: 400px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 详细统计行 */
.detail-row {
  margin-top: 20px;
}

.detail-card {
  border-radius: 8px;
  min-height: 400px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stat-card {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .admin-home {
    padding: 10px;
  }

  .stat-content {
    flex-direction: column;
    text-align: center;
  }

  .stat-value {
    font-size: 24px;
  }
}
</style>
