<template>
  <div class="item-card" @click="handleClick">
    <!-- 图片展品 -->
    <el-image
      v-if="item.mediaKind === 'IMAGE'"
      :src="item.mediaUrl"
      fit="cover"
      class="item-media"
      lazy
    >
      <template #error>
        <div class="image-slot">
          <el-icon><icon-picture /></el-icon>
        </div>
      </template>
    </el-image>

    <!-- 视频展品 -->
    <div v-else class="video-wrapper">
      <video
        :src="item.mediaUrl"
        :poster="item.coverUrl || ''"
        class="item-media"
        preload="metadata"
      />
      <div class="video-overlay">
        <el-icon class="play-icon"><VideoPlay /></el-icon>
      </div>
    </div>

    <!-- 展品信息 -->
    <div class="item-info">
      <h3 class="item-title">{{ item.title }}</h3>
      <p v-if="item.description" class="item-description">
        {{ stripHtml(item.description) }}
      </p>
      <div class="item-meta">
        <el-tag :type="item.mediaKind === 'IMAGE' ? 'success' : 'warning'" size="small">
          {{ item.mediaKind === 'IMAGE' ? '图片' : '视频' }}
        </el-tag>
        <span class="item-time">{{ formatTime(item.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Picture as IconPicture, VideoPlay } from '@element-plus/icons-vue'
import type { ExhibitItem } from '@/api/items'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  item: ExhibitItem
}>()

const emit = defineEmits<{
  click: [item: ExhibitItem]
}>()

const handleClick = () => {
  emit('click', props.item)
}

// 去除HTML标签，提取纯文本
const stripHtml = (html: string) => {
  const div = document.createElement('div')
  div.innerHTML = html
  return div.textContent || div.innerText || ''
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}
</script>

<style scoped>
.item-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-block;
  width: 100%;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.item-media {
  width: 100%;
  min-height: 200px;
  display: block;
}

.video-wrapper {
  position: relative;
  width: 100%;
}

.video-wrapper video {
  width: 100%;
  display: block;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.item-card:hover .video-overlay {
  opacity: 1;
}

.play-icon {
  font-size: 48px;
  color: #fff;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  color: #909399;
}

.image-slot .el-icon {
  font-size: 48px;
}

.item-info {
  padding: 12px;
}

.item-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-description {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.item-time {
  color: #909399;
}
</style>
