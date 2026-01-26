<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElCarousel, ElCarouselItem, ElTabs, ElTabPane, ElScrollbar, ElButton, ElMessage, ElMessageBox, ElDropdown, ElDropdownMenu } from 'element-plus'
import { HomeFilled, Collection, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { useAuthStore } from '@/store/auth'
import { logout } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = computed(() => authStore.userInfo)

const currentDate = ref('')
const weekday = ref('')
const activeTab = ref('civilized')
const activeFloor = ref('1&2')

let mapChart = null
const chartRef = ref(null)

const floors = [
  { label: '1&2层', value: '1&2' },
  { label: '3层', value: '3' },
  { label: '4层', value: '4' }
]

onMounted(() => {
  const now = dayjs()
  currentDate.value = now.format('YYYY-MM-DD')
  weekday.value = now.format('dddd')

  initMapChart()
})

const initMapChart = async () => {
  await nextTick()
  if (chartRef.value) {
    mapChart = echarts.init(chartRef.value)
    updateMapChart()

    window.addEventListener('resize', () => {
      mapChart?.resize()
    })
  }
}

const updateMapChart = () => {
  const floorImages = {
    '1&2': '/floor_1&2.png',
    '3': '/floor_3.png',
    '4': '/floor_4.png'
  }

  const option = {
    graphic: [
      {
        type: 'image',
        id: 'floor-map',
        left: 'center',
        top: 'center',
        z: 0,
        bounding: 'raw',
        style: {
          image: floorImages[activeFloor.value],
          width: 800,
          height: 600
        }
      },
      // 北区展厅
      {
        type: 'rect',
        left: 250,
        top: 50,
        z: 1,
        shape: {
          width: 200,
          height: 150
        },
        style: {
          fill: 'rgba(50, 50, 50, 0.8)',
          stroke: '#b03128',
          lineWidth: 2
        },
        emphasis: {
          style: {
            fill: 'rgba(176, 49, 40, 0.9)'
          }
        },
        cursor: 'pointer',
        onclick: () => {
          let message = ''
          if (activeFloor.value === '4') {
            message = '北区展厅：多媒体互动区'
          } else {
            message = '北区展厅：复兴之路'
          }
          ElMessage({
            message: message,
            type: 'success'
          })
        }
      },
      {
        type: 'text',
        left: 330,
        top: 120,
        z: 2,
        style: {
          text: '北区',
          fill: '#fff',
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      // 南区展厅
      {
        type: 'rect',
        left: 700,
        top: 50,
        z: 1,
        shape: {
          width: 200,
          height: 150
        },
        style: {
          fill: 'rgba(50, 50, 50, 0.8)',
          stroke: '#b03128',
          lineWidth: 2
        },
        emphasis: {
          style: {
            fill: 'rgba(176, 49, 40, 0.9)'
          }
        },
        cursor: 'pointer',
        onclick: () => {
          ElMessage({
            message: '南区展厅：书画艺术展',
            type: 'success'
          })
        }
      },
      {
        type: 'text',
        left: 780,
        top: 120,
        z: 2,
        style: {
          text: '南区',
          fill: '#fff',
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      // 中央大厅
      {
        type: 'rect',
        left: 530,
        top: 250,
        z: 1,
        shape: {
          width: 200,
          height: 150
        },
        style: {
          fill: 'rgba(50, 50, 50, 0.8)',
          stroke: '#b03128',
          lineWidth: 2
        },
        emphasis: {
          style: {
            fill: 'rgba(176, 49, 40, 0.9)'
          }
        },
        cursor: 'pointer',
        onclick: () => {
          ElMessage({
            message: '中央大厅',
            type: 'success'
          })
        }
      },
      {
        type: 'text',
        left: 600,
        top: 320,
        z: 2,
        style: {
          text: '中央大厅',
          fill: '#fff',
          fontSize: 16,
          fontWeight: 'bold'
        }
      }
    ],
    xAxis: {
      show: false,
      min: 0,
      max: 800
    },
    yAxis: {
      show: false,
      min: 0,
      max: 600
    },
    grid: {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0
    }
  }

  mapChart.setOption(option)
}

watch(activeFloor, () => {
  updateMapChart()
})

// 跳转到首页顶部
const goToHome = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 跳转到展品列表
const goToItems = () => {
  router.push('/items')
}

// 滚动到指定模块
const scrollToSection = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 右侧网格数据
const gridItems = [
  { icon: '🕐', label: '开放时间', id: 'time' },
  { icon: '📅', label: '参观预约', id: 'booking' },
  { icon: '🗺️', label: '展厅分布', id: 'map' },
  { icon: '📚', label: '馆藏精品', id: 'collection' },
  { icon: '🔔', label: '参观须知', id: 'rules' },
  { icon: '🎪', label: '便民服务', id: 'service' }
]

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  if (command === 'logout') {
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
    
    // 调用退出登录接口
    try {
      await logout()
    } catch (error) {
      console.error('退出登录接口调用失败:', error)
    }
    
    // 清除本地状态
    authStore.clearUser()
    ElMessage.success('已退出登录')
    router.push('/login/user')
  } catch {
    // 用户取消
  }
}

// 精品藏品图片列表
const qualityImages = [
  { 
    image: '/quality/2023111510240326308.png', name: '金杖'
  },
  { 
    image: '/quality/2023111510263323992.png', name: '祭山图玉璋'
  },
  { 
    image: '/quality/2023111510321052665.png', name: '青铜神树' 
  }
]
</script>

<template>
  <div class="museum-app">
    <!-- Header -->
    <header class="header">
      <div class="header-content">
        <div class="logo">博物馆</div>
        <div class="header-nav">
          <el-button type="text" @click="goToHome">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-button>
          <el-button type="text" @click="goToItems">
            <el-icon><Collection /></el-icon>
            <span>展品</span>
          </el-button>
        </div>
        <el-dropdown @command="handleCommand" class="user-dropdown">
          <div class="user-info">
            <el-avatar v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" :size="32" />
            <el-avatar v-else :icon="UserFilled" :size="32" />
            <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- Hero 区域 -->
    <div class="main-container">
      <div class="hero-section">
        <!-- 左侧卡片 -->
        <div class="left-card">
        <div class="date-section">
          <div class="date-text">{{ currentDate }}</div>
          <div class="weekday-text">{{ weekday }}</div>
        </div>
        <div class="collection-thumbnail">
          <ElCarousel :interval="3000" arrow="always" height="400px">
            <ElCarouselItem v-for="(item, index) in qualityImages" :key="index">
              <div class="carousel-item">
                <img :src="item.image" :alt="item.name" class="carousel-image" />
                <div class="image-title">{{ item.name }}</div>
              </div>
            </ElCarouselItem>
          </ElCarousel>
          <div class="thumbnail-label">精品藏品</div>
        </div>
      </div>

      <!-- 右侧网格 -->
      <div class="right-grid">
        <div class="grid-row">
          <div
              v-for="(item, index) in gridItems.slice(0, 3)"
              :key="index"
              class="grid-item"
              @click="scrollToSection(item.id)"
          >
            <div class="item-icon">{{ item.icon }}</div>
            <div class="item-label">{{ item.label }}</div>
          </div>
        </div>
        <div class="grid-row">
          <div
              v-for="(item, index) in gridItems.slice(3, 6)"
              :key="index + 3"
              class="grid-item"
              @click="scrollToSection(item.id)"
          >
            <div class="item-icon">{{ item.icon }}</div>
            <div class="item-label">{{ item.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容模块区域 -->
    <div class="content-section">
      <!-- 开放时间模块 -->
      <div id="time" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>开放时间</h2>
        </div>
        <div class="module-content">
          <p>周二至周日：9:00 - 17:00（16:00 停止入场）</p>
          <p>周一闭馆（法定节假日除外）</p>
          <p>春节期间开放时间另行通知</p>
        </div>
      </div>

      <!-- 参观预约模块 -->
      <div id="booking" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>参观预约</h2>
        </div>
        <div class="booking-layout">
          <div class="booking-left">
            <div class="rule-title">预约规则</div>
            <div class="rule-item">• 个人参观可通过官网或微信公众号提前预约</div>
            <div class="rule-item">• 团队参观需提前 3 个工作日预约</div>
            <div class="rule-item">• 预约成功后凭身份证或预约码入场</div>
            <div class="rule-item">• 每日限额 5000 人，建议提前预约</div>
            <div class="rule-item">• 预约时间：可预约 7 日内参观时段</div>
            <div class="rule-item">• 预约成功后请在规定时间内入场</div>
            <div class="rule-item">• 未按时入场需重新预约</div>
            <div class="rule-item">• 取消预约请提前 24 小时操作</div>
            <div class="rule-item">• 预约信息需真实有效</div>
            <div class="rule-item">• 优惠票需出示相关证件</div>
          </div>
          <div class="booking-right">
            <div class="booking-info">
              <div class="info-title">温馨提示</div>
              <div class="info-content">如遇特殊情况，博物馆保留调整开放时间及预约规则的权利</div>
            </div>
            <ElButton type="danger" size="large" class="booking-button">预约入口</ElButton>
          </div>
        </div>
      </div>

      <!-- 参观须知模块 -->
      <div id="rules" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>参观须知</h2>
        </div>
        <ElTabs v-model="activeTab" class="rules-tabs">
          <ElTabPane label="文明参观须知" name="civilized">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>• 请自觉遵守参观秩序，保持安静，不得大声喧哗</p>
                <p>• 请勿触摸展品，保持安全距离</p>
                <p>• 拍照时请关闭闪光灯，部分展区禁止拍照</p>
                <p>• 请勿在展厅内奔跑、追逐或进行其他危险行为</p>
                <p>• 请爱护公共设施，保持展厅清洁</p>
                <p>• 参观时请将手机调至静音或震动模式</p>
                <p>• 请尊重其他参观者，避免长时间占用观展位置</p>
                <p>• 禁止在展厅内吸烟、饮食</p>
                <p>• 请听从工作人员指引，遵守馆内秩序</p>
                <p>• 如遇紧急情况，请听从工作人员指挥有序撤离</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
          <ElTabPane label="禁限带物品" name="prohibited">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>• 禁止携带易燃、易爆、易腐蚀等危险品</p>
                <p>• 禁止携带管制刀具等违禁物品</p>
                <p>• 禁止携带宠物（导盲犬除外）</p>
                <p>• 禁止携带食品、饮料入馆</p>
                <p>• 禁止携带自拍杆、三脚架等摄影器材</p>
                <p>• 限制携带大件行李、背包（超过 A4 纸大小）</p>
                <p>• 限制携带液体超过 500ml</p>
                <p>• 限制携带专业录音录像设备</p>
                <p>• 禁止携带滑板、平衡车等代步工具</p>
                <p>• 未经许可不得携带无人机等航拍设备</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
          <ElTabPane label="寄存须知" name="storage">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>• 本馆提供免费寄存服务</p>
                <p>• 寄存物品需自行妥善保管物品凭证</p>
                <p>• 寄存时间不超过 24 小时</p>
                <p>• 不得寄存贵重物品（现金、珠宝等）</p>
                <p>• 不得寄存易燃易爆、有毒有害物品</p>
                <p>• 不得寄存活体动植物</p>
                <p>• 寄存柜使用前请检查是否完好</p>
                <p>• 遗失凭证需出示有效身份证明</p>
                <p>• 超时未取的物品将按规定处理</p>
                <p>• 任何寄存物品损坏或丢失本馆不承担责任</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
          <ElTabPane label="收费标准" name="charges">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>• 基本展览：免费参观</p>
                <p>• 特殊展览：根据展览内容定价</p>
                <p>• 讲解服务：免费（需提前预约）</p>
                <p>• 讲解器租赁：20 元/台/次</p>
                <p>• 讲解器押金：100 元（归还时退还）</p>
                <p>• 轮椅借用：免费（需押金 200 元）</p>
                <p>• 婴儿车借用：免费（需押金 200 元）</p>
                <p>• 导览册：免费领取</p>
                <p>• 停车费用：5 元/小时（前 2 小时免费）</p>
                <p>• 文创商品：按标价出售</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
        </ElTabs>
      </div>

      <!-- 展厅分布模块 -->
      <div id="map" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>展厅分布</h2>
        </div>
        <div class="floor-controls">
          <div
              v-for="floor in floors"
              :key="floor.value"
              :class="['floor-btn', { active: activeFloor === floor.value }]"
              @click="activeFloor = floor.value"
          >
            {{ floor.label }}
          </div>
        </div>
        <div ref="chartRef" class="map-chart"></div>
      </div>

      <!-- 馆藏精品模块 -->
      <div id="collection" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>馆藏精品</h2>
        </div>
        <div class="module-content">
          <p>• 青铜器：商代晚期青铜器精品</p>
          <p>• 瓷器珍品：宋代名窑瓷器展示</p>
          <p>• 书画作品：明清名家书画真迹</p>
          <p>• 玉器收藏：新石器时代至清代玉器</p>
          <p>• 金银器：历代宫廷金银器皿</p>
        </div>
        <div class="module-action">
          <el-button type="primary" size="large" @click="goToItems">
            查看更多展品
          </el-button>
        </div>
      </div>

      <!-- 便民服务模块 -->
      <div id="service" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>便民服务</h2>
        </div>
        <div class="module-content">
          <p>提供免费寄存服务</p>
          <p>轮椅、婴儿车可免费借用</p>
          <p>设有母婴室和无障碍通道</p>
          <p>提供讲解器租赁服务（需押金）</p>
        </div>
      </div>
    </div>
    </div>

    <!-- 底部二维码区域 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="qr-section">
          <div class="qr-title">关注我们</div>
          <div class="qr-container">
            <div class="qr-item">
              <div class="qr-placeholder">📱</div>
              <div class="qr-label">微信公众号</div>
            </div>
            <div class="qr-item">
              <div class="qr-placeholder">📷</div>
              <div class="qr-label">官方抖音</div>
            </div>
            <div class="qr-item">
              <div class="qr-placeholder">🌐</div>
              <div class="qr-label">官方网站</div>
            </div>
          </div>
        </div>
        <div class="footer-info">
          <p>© 2026 博物馆 版权所有</p>
          <p>地址：XX市XX区XX路XX号</p>
          <p>电话：010-XXXXXXXX</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.museum-app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
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
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  color: white;
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 40px;
}

.header-nav .el-button {
  color: rgba(255, 255, 255, 0.9);
  border: none;
  padding: 8px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.header-nav .el-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.header-nav .el-button .el-icon {
  margin-right: 4px;
}

.user-dropdown {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.el-dropdown :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 主内容容器 */
.main-container {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  flex: 1;
}

/* Hero 区域样式 */
.hero-section {
  display: flex;
  gap: 20px;
  padding: 24px;
  background-color: #fff;
  margin: 24px 0 0 0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  min-height: 400px;
}

/* 左侧卡片样式 */
.left-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  flex: 0 0 500px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.date-section {
  text-align: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.date-text {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.weekday-text {
  font-size: 18px;
  color: #666;
}

.collection-thumbnail {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.collection-thumbnail :deep(.el-carousel) {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: white;
  padding: 20px 15px 10px;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.thumbnail-label {
  font-size: 16px;
  color: #666;
  margin-top: 16px;
}

/* 右侧网格样式 */
.right-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.grid-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  flex: 1;
}

.grid-item {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.grid-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.grid-item:hover .item-label {
  color: #b03128;
}

.item-icon {
  font-size: 48px;
  margin-bottom: 12px;
  transition: transform 0.3s ease;
}

.grid-item:hover .item-icon {
  transform: scale(1.1);
}

.item-label {
  font-size: 14px;
  color: #333;
  transition: color 0.3s ease;
  text-align: center;
}

/* 内容模块区域样式 */
.content-section {
  padding: 24px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.module {
  background-color: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.module-title {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.title-decoration {
  width: 4px;
  height: 24px;
  background-color: #b03128;
  margin-right: 12px;
  border-radius: 2px;
}

.module-title h2 {
  font-size: 20px;
  color: #333;
  font-weight: bold;
  margin: 0;
}

.module-content {
  color: #666;
  line-height: 1.8;
  font-size: 14px;
}

.module-content p {
  margin: 8px 0;
}

.module-action {
  margin-top: 16px;
  text-align: center;
}

/* 参观预约模块样式 */
.booking-layout {
  display: flex;
  gap: 24px;
}

.booking-left {
  flex: 1;
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
}

.rule-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #b03128;
}

.rule-item {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 12px;
}

.booking-right {
  flex: 0 0 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.booking-info {
  background-color: #fff8f0;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #b03128;
}

.info-title {
  font-size: 16px;
  font-weight: bold;
  color: #b03128;
  margin-bottom: 8px;
}

.info-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.booking-button {
  width: 100%;
  height: 48px;
  font-size: 18px;
  font-weight: bold;
}

/* 参观须知模块样式 */
.rules-tabs {
  margin-top: 16px;
}

.rules-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.rules-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #eee;
}

.rules-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  color: #666;
}

.rules-tabs :deep(.el-tabs__item.is-active) {
  color: #b03128;
  font-weight: bold;
}

.rules-tabs :deep(.el-tabs__active-bar) {
  background-color: #b03128;
  height: 3px;
}

.rules-tabs :deep(.el-tabs__item:hover) {
  color: #b03128;
}

.tab-content {
  padding: 8px 0;
}

.tab-content p {
  font-size: 14px;
  color: #666;
  line-height: 2;
  margin: 12px 0;
  padding-left: 8px;
}

.tab-content p:hover {
  background-color: #f5f5f5;
  padding-left: 16px;
  transition: all 0.3s ease;
}

.rules-tabs :deep(.el-scrollbar__wrap) {
  padding-right: 8px;
}

.rules-tabs :deep(.el-scrollbar__bar) {
  right: 2px;
}

.rules-tabs :deep(.el-scrollbar__thumb) {
  background-color: #ddd;
}

.rules-tabs :deep(.el-scrollbar__thumb:hover) {
  background-color: #b03128;
}

/* 展厅分布模块样式 */
.floor-controls {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.floor-btn {
  padding: 8px 24px;
  border: 2px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s ease;
}

.floor-btn:hover {
  border-color: #b03128;
  color: #b03128;
}

.floor-btn.active {
  background-color: #b03128;
  color: white;
  border-color: #b03128;
}

.map-chart {
  width: 100%;
  height: 600px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

/* 底部二维码区域样式 */
.footer {
  background-color: #b03128;
  color: white;
  padding: 40px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1440px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
}

.qr-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.qr-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.qr-container {
  display: flex;
  gap: 40px;
}

.qr-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.qr-placeholder {
  width: 120px;
  height: 120px;
  background-color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}

.qr-label {
  font-size: 14px;
  color: #999;
}

.footer-info {
  text-align: right;
}

.footer-info p {
  margin: 8px 0;
  color: #999;
  font-size: 14px;
}
</style>
