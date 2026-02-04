<template>
  <div class="comment-panel">
    <!-- 评论输入框 -->
    <div v-if="authStore.isAuthed" class="comment-input-section">
      <el-input
        v-model="commentText"
        type="textarea"
        :rows="3"
        placeholder="发表你的评论..."
        maxlength="500"
        show-word-limit
        class="comment-input"
      />
      <div class="input-actions">
        <el-button
          type="primary"
          @click="handleSubmit"
          :disabled="!commentText.trim() || submitting"
          :loading="submitting"
        >
          发表评论
        </el-button>
      </div>
    </div>

    <!-- 未登录提示 -->
    <div v-else class="login-tip">
      <el-icon><InfoFilled /></el-icon>
      <span>登录后才能发表评论</span>
      <el-button type="primary" text @click="handleLogin">去登录</el-button>
    </div>

    <!-- 评论列表 -->
    <div v-if="comments.length > 0" class="comment-list">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <!-- 用户头像和信息 -->
        <div class="comment-avatar">
          <el-avatar
            :src="comment.avatarURL"
            :size="40"
          >
            {{ comment.username.charAt(0) }}
          </el-avatar>
        </div>

        <!-- 评论内容区 -->
        <div class="comment-content-wrapper">
          <!-- 用户名 -->
          <div class="comment-username">{{ comment.username }}</div>

          <!-- 评论内容 -->
          <div class="comment-text">{{ comment.content }}</div>

          <!-- 删除按钮（右上角） -->
          <el-button
            v-if="authStore.userInfo?.id === comment.userId"
            type="danger"
            text
            size="small"
            @click="handleDelete(comment)"
            class="delete-button"
          >
            删除
          </el-button>

          <!-- 点赞和时间（右下角） -->
          <div class="comment-actions">
            <!-- 点赞按钮 -->
            <div class="comment-like-button" @click="handleLike(comment.id)">
              <img
                :src="comment.liked === 1 ? '/icons/like-liked.png' : '/icons/like-unliked.png'"
                :alt="comment.liked === 1 ? '已点赞' : '未点赞'"
                class="like-icon"
              />
              <span :class="{ 'liked': comment.liked === 1 }" class="like-count">
                {{ comment.likeCount ?? comment.likecount ?? 0 }}
              </span>
            </div>

            <!-- 发布时间 -->
            <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无评论，快来抢沙发吧！" />

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="handlePageChange"
      @current-change="handlePageChange"
      class="pagination"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getComments, addComment, deleteComment, likeComment, unlikeComment, type Comment } from '@/api/comments'
import dayjs from 'dayjs'

const props = defineProps<{
  itemId: number
}>()

const router = useRouter()
const authStore = useAuthStore()

const comments = ref<Comment[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const commentText = ref('')
const submitting = ref(false)

// 格式化时间显示
const formatTime = (dateTime: string): string => {
  const now = new Date()
  const commentTime = new Date(dateTime)
  const diffMs = now.getTime() - commentTime.getTime()
  const diffHours = diffMs / (1000 * 60 * 60)
  const diffDays = diffHours / 24

  // 超过三天：显示 MM-DD
  if (diffDays > 3) {
    const month = String(commentTime.getMonth() + 1).padStart(2, '0')
    const day = String(commentTime.getDate()).padStart(2, '0')
    return `${month}-${day}`
  }

  // 24小时以上，三天以内：显示 x天前
  if (diffDays >= 1) {
    return `${Math.floor(diffDays)}天前`
  }

  // 24小时以内：显示 x小时前
  if (diffHours >= 1) {
    return `${Math.floor(diffHours)}小时前`
  }

  // 小于1小时：显示刚刚
  return '刚刚'
}

// 获取评论列表
const loadComments = async () => {
  try {
    const result = await getComments(props.itemId, {
      current: currentPage.value,
      size: pageSize.value
    })

    // 打印第一条评论，检查字段名
    if (result.records.length > 0) {
      console.log('评论数据示例:', result.records[0])
    }

    comments.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  }
}

// 分页改变
const handlePageChange = () => {
  loadComments()
}

// 提交评论
const handleSubmit = async () => {
  if (!authStore.isAuthed) {
    ElMessage.warning('请先登录')
    return
  }

  const content = commentText.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const id = await addComment(props.itemId, { content })

    // 创建新评论并添加到列表
    const newComment: Comment = {
      id,
      itemId: props.itemId,
      userId: authStore.userInfo!.id,
      username: authStore.userInfo!.nickname || authStore.userInfo!.username,
      content,
      createdAt: new Date().toISOString(),
      avatarURL: authStore.userInfo!.avatarUrl,
      liked: 0,
      likecount: 0
    }

    comments.value.unshift(newComment)
    total.value++
    commentText.value = ''
    ElMessage.success('评论成功')
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败')
  } finally {
    submitting.value = false
  }
}

// 点赞/取消点赞
const handleLike = async (commentId: number) => {
  if (!authStore.isAuthed) {
    ElMessage.warning('请先登录')
    return
  }

  const comment = comments.value.find(c => c.id === commentId)
  if (!comment) return

  // 获取当前点赞数（支持两种字段名）
  const currentLikeCount = comment.likeCount ?? comment.likecount ?? 0

  // 判断是点赞还是取消点赞（liked: 1 = 已点赞, 0 = 未点赞）
  const isLiked = comment.liked === 1
  const previousLiked = comment.liked
  const previousLikeCount = currentLikeCount

  try {
    // 乐观更新 UI
    if (isLiked) {
      // 取消点赞
      comment.liked = 0
      const newCount = Math.max(0, currentLikeCount - 1)
      comment.likeCount = newCount
      comment.likecount = newCount
    } else {
      // 点赞
      comment.liked = 1
      const newCount = currentLikeCount + 1
      comment.likeCount = newCount
      comment.likecount = newCount
    }

    // 调用 API（返回boolean）
    const success = isLiked
      ? await unlikeComment(commentId)
      : await likeComment(commentId)

    if (!success) {
      throw new Error('操作失败')
    }
  } catch (error) {
    // 发生错误，回滚到之前的状态
    console.error('点赞操作失败:', error)
    comment.liked = previousLiked
    comment.likeCount = previousLikeCount
    comment.likecount = previousLikeCount
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 删除评论
const handleDelete = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteComment(comment.id)
    ElMessage.success('删除成功')
    // 从列表中移除该评论
    comments.value = comments.value.filter(c => c.id !== comment.id)
    total.value--
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 跳转登录
const handleLogin = () => {
  router.push('/login/user')
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-panel {
  padding: 20px;
  max-width: 80%;
  margin: 0 auto;
}

/* 评论输入区 */
.comment-input-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.comment-input {
  margin-bottom: 12px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.login-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  color: #409eff;
  margin-bottom: 24px;
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.comment-item {
  display: flex;
  gap: 12px;
  position: relative;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content-wrapper {
  flex: 1;
  position: relative;
  min-height: 80px;
  padding-top: 8px;
}

.comment-username {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  padding-right: 80px;
}

.comment-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  padding-right: 80px;
  margin-bottom: 24px;
}

/* 删除按钮 - 右上角 */
.delete-button {
  position: absolute;
  top: 8px;
  right: 16px;
  padding: 4px 8px;
}

/* 点赞和时间容器 - 右下角 */
.comment-actions {
  position: absolute;
  bottom: 8px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 点赞按钮 */
.comment-like-button {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
  user-select: none;
}

.comment-like-button:hover {
  background: #f5f7fa;
}

.like-icon {
  width: 16px;
  height: 16px;
  display: block;
}

.like-count {
  font-size: 14px;
  color: #909399;
}

.like-count.liked {
  color: #B03128;
}

/* 时间 */
.comment-time {
  font-size: 12px;
  color: #999;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
