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
            <el-descriptions-item label="头像">
              <el-avatar v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" :size="60" />
              <el-avatar v-else icon="UserFilled" :size="60" />
            </el-descriptions-item>
            <el-descriptions-item label="Token">
              <el-text size="small" truncated>{{ userInfo?.token }}</el-text>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        
        <!-- 统计数据 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic title="总用户数" :value="statistics.totalUsers" />
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic title="正常用户" :value="statistics.activeUsers">
                <template #suffix>
                  <el-icon style="vertical-align: -0.125em"><UserFilled /></el-icon>
                </template>
              </el-statistic>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic title="禁用用户" :value="statistics.bannedUsers">
                <template #suffix>
                  <el-icon style="vertical-align: -0.125em"><CircleClose /></el-icon>
                </template>
              </el-statistic>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic title="系统状态" value="正常">
                <template #suffix>
                  <el-tag type="success" size="small">运行中</el-tag>
                </template>
              </el-statistic>
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
  CircleClose
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { logout } from '@/api/auth'
import {
  getUserList,
  type UserInfo,
  type UserQueryParams
} from '@/api/admin'
import AdminLayout from '@/components/admin/AdminLayout.vue'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = computed(() => authStore.userInfo)

// 统计数据
const statistics = ref({
  totalUsers: 0,
  activeUsers: 0,
  bannedUsers: 0
})

// 图表引用
const statusChartRef = ref<HTMLElement | null>(null)
const roleChartRef = ref<HTMLElement | null>(null)

// 图表实例
let statusChart: echarts.ECharts | null = null
let roleChart: echarts.ECharts | null = null

// 初始化图表
const initCharts = () => {
  // 用户状态分布饼图
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    updateStatusChart()
  }
  
  // 用户角色分布饼图
  if (roleChartRef.value) {
    roleChart = echarts.init(roleChartRef.value)
    updateRoleChart()
  }
}

// 更新状态图表
const updateStatusChart = () => {
  if (!statusChart) return
  
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

// 更新角色图表
const updateRoleChart = () => {
  if (!roleChart) return
  
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

// 获取统计数据
const loadStatistics = async () => {
  try {
    const result = await getUserList({
      page: 1,
      size: 1000 // 获取所有用户用于统计
    })

    // 更新统计数据
    statistics.value.totalUsers = result.total
    statistics.value.activeUsers = result.records.filter((u: UserInfo) => u.status === 0).length
    statistics.value.bannedUsers = result.records.filter((u: UserInfo) => u.status === 1).length

    // 更新图表
    nextTick(() => {
      updateStatusChart()
      updateRoleChart()
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
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
  loadStatistics()
  nextTick(() => {
    initCharts()
  })
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    statusChart?.resize()
    roleChart?.resize()
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

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

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
</style>
