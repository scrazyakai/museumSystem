<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElCarousel, ElCarouselItem, ElTabs, ElTabPane, ElScrollbar, ElButton, ElMessage, ElMessageBox, ElDropdown, ElDropdownMenu } from 'element-plus'
import { HomeFilled, Collection, SwitchButton, UserFilled, Tickets } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { useAuthStore } from '@/store/auth'
import { logout } from '@/api/auth'
import AppHeader from '@/components/common/AppHeader.vue'

const router = useRouter()
const authStore = useAuthStore()
const { t, locale } = useI18n()

// 用户信息
const userInfo = computed(() => authStore.userInfo)

const currentDate = ref('')
const weekday = ref('')
const activeTab = ref('civilized')
const activeFloor = ref('1&2')

let mapChart = null
const chartRef = ref(null)

const floors = computed(() => [
  { label: t('home.floor1And2'), value: '1&2' },
  { label: t('home.floor3'), value: '3' },
  { label: t('home.floor4'), value: '4' }
])

onMounted(() => {
  const now = dayjs()
  currentDate.value = now.format('YYYY-MM-DD')

  // 根据当前语言设置星期显示
  updateWeekday(now)

  initMapChart()
})

// 更新星期显示
const updateWeekday = (now: dayjs.Dayjs) => {
  const day = now.day()
  const weekdays = locale.value === 'zh'
    ? ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    : ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
  weekday.value = weekdays[day]
}

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
            message = t('home.northAreaMultimedia')
          } else {
            message = t('home.northAreaExhibition')
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
          text: t('home.northArea'),
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
            message: t('home.southAreaExhibition'),
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
          text: t('home.southArea'),
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
            message: t('home.centralHall'),
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
          text: t('home.centralHall'),
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

// 跳转到预约页面
const goToBooking = () => {
  router.push('/booking')
}

// 滚动到指定模块
const scrollToSection = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 右侧网格数据
const gridItems = computed(() => [
  { icon: '🕐', label: t('home.openTime'), id: 'time' },
  { icon: '📅', label: t('home.visitBooking'), id: 'booking' },
  { icon: '🗺️', label: t('home.exhibitionMap'), id: 'map' },
  { icon: '📚', label: t('home.collection'), id: 'collection' },
  { icon: '🔔', label: t('home.visitRules'), id: 'rules' },
  { icon: '🎪', label: t('home.services'), id: 'service' }
])

// 精品藏品图片列表
const qualityImages = computed(() => [
  {
    image: '/quality/2023111510240326308.png',
    name: locale.value === 'zh' ? '金杖' : 'Golden Staff'
  },
  {
    image: '/quality/2023111510263323992.png',
    name: locale.value === 'zh' ? '祭山图玉璋' : 'Jade Zhang with Mountain Sacrifice Scene'
  },
  {
    image: '/quality/2023111510321052665.png',
    name: locale.value === 'zh' ? '青铜神树' : 'Bronze Sacred Tree'
  }
])
</script>

<template>
  <div class="museum-app">
    <!-- Header -->
    <AppHeader />

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
          <div class="thumbnail-label">{{ t('home.qualityItems') }}</div>
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
          <h2>{{ t('home.openingHours') }}</h2>
        </div>
        <div class="module-content">
          <p>{{ t('home.weekdayTuesdayToSunday') }}：{{ t('home.time') }}（{{ t('home.stopEntry') }}）</p>
          <p>{{ t('home.weekdayMonday') }} {{ t('home.closed') }} {{ t('home.exceptHolidays') }}</p>
          <p>{{ t('home.springNotice') }}</p>
        </div>
      </div>

      <!-- 参观预约模块 -->
      <div id="booking" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>{{ t('home.visitBooking') }}</h2>
        </div>
        <div class="booking-layout">
          <div class="booking-left">
            <div class="rule-title">{{ t('home.bookingRules') }}</div>
            <div class="rule-item">{{ t('home.bookingRule1') }}</div>
            <div class="rule-item">{{ t('home.bookingRule2') }}</div>
            <div class="rule-item">{{ t('home.bookingRule3') }}</div>
            <div class="rule-item">{{ t('home.bookingRule4') }}</div>
            <div class="rule-item">{{ t('home.bookingRule5') }}</div>
            <div class="rule-item">{{ t('home.bookingRule6') }}</div>
            <div class="rule-item">{{ t('home.bookingRule7') }}</div>
            <div class="rule-item">{{ t('home.bookingRule8') }}</div>
            <div class="rule-item">{{ t('home.bookingRule9') }}</div>
            <div class="rule-item">{{ t('home.bookingRule10') }}</div>
          </div>
          <div class="booking-right">
            <div class="booking-info">
              <div class="info-title">{{ t('home.bookingNotice') }}</div>
              <div class="info-content">{{ t('home.bookingNoticeContent') }}</div>
            </div>
            <ElButton type="danger" size="large" class="booking-button" @click="goToBooking">{{ t('home.bookingEntrance') }}</ElButton>
          </div>
        </div>
      </div>

      <!-- 参观须知模块 -->
      <div id="rules" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>{{ t('home.visitRules') }}</h2>
        </div>
        <ElTabs v-model="activeTab" class="rules-tabs">
          <ElTabPane :label="t('home.civilizedRules')" name="civilized">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>{{ t('home.civilizedRule1') }}</p>
                <p>{{ t('home.civilizedRule2') }}</p>
                <p>{{ t('home.civilizedRule3') }}</p>
                <p>{{ t('home.civilizedRule4') }}</p>
                <p>{{ t('home.civilizedRule5') }}</p>
                <p>{{ t('home.civilizedRule6') }}</p>
                <p>{{ t('home.civilizedRule7') }}</p>
                <p>{{ t('home.civilizedRule8') }}</p>
                <p>{{ t('home.civilizedRule9') }}</p>
                <p>{{ t('home.civilizedRule10') }}</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
          <ElTabPane :label="t('home.prohibitedItems')" name="prohibited">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>{{ t('home.prohibitedItem1') }}</p>
                <p>{{ t('home.prohibitedItem2') }}</p>
                <p>{{ t('home.prohibitedItem3') }}</p>
                <p>{{ t('home.prohibitedItem4') }}</p>
                <p>{{ t('home.prohibitedItem5') }}</p>
                <p>{{ t('home.prohibitedItem6') }}</p>
                <p>{{ t('home.prohibitedItem7') }}</p>
                <p>{{ t('home.prohibitedItem8') }}</p>
                <p>{{ t('home.prohibitedItem9') }}</p>
                <p>{{ t('home.prohibitedItem10') }}</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
          <ElTabPane :label="t('home.storageRules')" name="storage">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>{{ t('home.storageRule1') }}</p>
                <p>{{ t('home.storageRule2') }}</p>
                <p>{{ t('home.storageRule3') }}</p>
                <p>{{ t('home.storageRule4') }}</p>
                <p>{{ t('home.storageRule5') }}</p>
                <p>{{ t('home.storageRule6') }}</p>
                <p>{{ t('home.storageRule7') }}</p>
                <p>{{ t('home.storageRule8') }}</p>
                <p>{{ t('home.storageRule9') }}</p>
                <p>{{ t('home.storageRule10') }}</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
          <ElTabPane :label="t('home.charges')" name="charges">
            <ElScrollbar height="300px">
              <div class="tab-content">
                <p>{{ t('home.charge1') }}</p>
                <p>{{ t('home.charge2') }}</p>
                <p>{{ t('home.charge3') }}</p>
                <p>{{ t('home.charge4') }}</p>
                <p>{{ t('home.charge5') }}</p>
                <p>{{ t('home.charge6') }}</p>
                <p>{{ t('home.charge7') }}</p>
                <p>{{ t('home.charge8') }}</p>
                <p>{{ t('home.charge9') }}</p>
                <p>{{ t('home.charge10') }}</p>
              </div>
            </ElScrollbar>
          </ElTabPane>
        </ElTabs>
      </div>

      <!-- 展厅分布模块 -->
      <div id="map" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>{{ t('home.exhibitionDistribution') }}</h2>
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
          <h2>{{ t('home.qualityCollection') }}</h2>
        </div>
        <div class="module-content">
          <p>{{ t('home.qualityCollectionDesc1') }}</p>
          <p>{{ t('home.qualityCollectionDesc2') }}</p>
          <p>{{ t('home.qualityCollectionDesc3') }}</p>
          <p>{{ t('home.qualityCollectionDesc4') }}</p>
          <p>{{ t('home.qualityCollectionDesc5') }}</p>
        </div>
        <div class="module-action">
          <el-button type="primary" size="large" @click="goToItems">
            {{ t('home.viewMoreItems') }}
          </el-button>
        </div>
      </div>

      <!-- 便民服务模块 -->
      <div id="service" class="module">
        <div class="module-title">
          <div class="title-decoration"></div>
          <h2>{{ t('home.convenientServices') }}</h2>
        </div>
        <div class="module-content">
          <p>{{ t('home.serviceDesc1') }}</p>
          <p>{{ t('home.serviceDesc2') }}</p>
          <p>{{ t('home.serviceDesc3') }}</p>
          <p>{{ t('home.serviceDesc4') }}</p>
        </div>
      </div>
    </div>
    </div>

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
  cursor: pointer;
  margin: 0;
  white-space: nowrap;
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.1);
}

.logo img {
  display: block;
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
</style>
