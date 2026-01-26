<template>
  <div class="comment-panel">
    <!-- 发表评论 -->
    <div v-if="authStore.isAuthed" class="comment-input-section">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        placeholder="发表你的评论..."
        maxlength="500"
        show-word-limit
      />
      <div class="input-actions">
        <el-button
          type="primary"
          :disabled="!commentContent.trim()"
          :loading="submitting"
          @click="handleSubmit"
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
    <div v-loading="loading" class="comment-list">
      <div v-if="comments.length === 0 && !loading" class="no-comment">
        暂无评论，快来抢沙发吧~
      </div>

      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <span class="comment-username">{{ comment.username }}</span>
          <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div v-if="canDelete(comment)" class="comment-actions">
          <el-button
            type="danger"
            text
            size="small"
            @click="handleDelete(comment)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getComments, addComment, deleteComment, type Comment } from '@/api/comments'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  itemId: number
}>()

const router = useRouter()
const authStore = useAuthStore()

const comments = ref<Comment[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const submitting = ref(false)
const commentContent = ref('')

// 获取评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const result = await getComments(props.itemId, {
      current: currentPage.value,
      size: pageSize.value
    })
    comments.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页改变
const handlePageChange = () => {
  loadComments()
}

// 发表评论
const handleSubmit = async () => {
  if (!commentContent.value.trim()) {
    return
  }

  submitting.value = true
  try {
    await addComment(props.itemId, {
      content: commentContent.value.trim()
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    currentPage.value = 1
    loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    submitting.value = false
  }
}

// 判断是否可以删除评论
const canDelete = (comment: Comment) => {
  return authStore.userInfo?.id === comment.userId
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
    loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
    }
  }
}

// 跳转登录
const handleLogin = () => {
  router.push('/login/user')
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-panel {
  padding: 20px;
}

.comment-input-section {
  margin-bottom: 24px;
}

.input-actions {
  margin-top: 12px;
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
  border-radius: 4px;
  color: #409eff;
  margin-bottom: 24px;
}

.comment-list {
  min-height: 200px;
}

.no-comment {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #eee;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-username {
  font-weight: bold;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  margin-top: 8px;
  text-align: right;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
