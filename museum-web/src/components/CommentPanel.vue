<template>
  <div class="comment-panel">
    <UComment
      :config="config"
      relative-time
      @submit="handleSubmit"
      @like="handleLike"
      @show-info="handleShowInfo"
      @before-data="handleBeforeData"
    >
      <!-- 未登录提示 -->
      <template #operate>
        <div v-if="!authStore.isAuthed" class="login-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>登录后才能发表评论</span>
          <el-button type="primary" text @click="handleLogin">去登录</el-button>
        </div>
      </template>

      <!-- 自定义工具栏 - 删除按钮 -->
      <template #tools="{ comment }">
        <el-button
          v-if="canDelete(comment)"
          type="danger"
          text
          size="small"
          @click="handleDelete(comment)"
        >
          删除
        </el-button>
      </template>
    </UComment>

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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getComments, addComment, deleteComment, type Comment } from '@/api/comments'
import dayjs from 'dayjs'
import { UComment } from 'undraw-ui'
import type { ConfigApi, CommentApi, SubmitParamApi } from 'undraw-ui'

const props = defineProps<{
  itemId: number
}>()

const router = useRouter()
const authStore = useAuthStore()

const comments = ref<Comment[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 转换后端评论数据为 undraw-ui 格式
const convertToCommentApi = (comment: Comment): CommentApi => {
  return {
    id: String(comment.id),
    parentId: null, // 暂不支持回复
    uid: String(comment.userId),
    content: comment.content,
    createTime: comment.createdAt,
    user: {
      username: comment.username,
      avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      homeLink: '#',
      level: undefined
    }
  }
}

// 配置对象
const config = computed<ConfigApi>(() => {
  const user = authStore.userInfo

  // ConfigApi.user 是必需字段，即使未登录也需要提供（但通过 show.form 控制表单显示）
  const configUser: any = user ? {
    id: String(user.id),
    username: user.nickname || user.username,
    avatar: user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    homeLink: '#',
    level: null,
    likeIds: []
  } : {
    id: '',
    username: '',
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }

  return {
    user: configUser,
    comments: comments.value.map(convertToCommentApi),
    show: {
      // 未登录时隐藏评论表单，通过 operate 插槽显示登录提示
      form: authStore.isAuthed,
      content: true,
      level: false,
      likes: false, // 后端暂不支持点赞
      address: false,
      homeLink: false,
      reply: false // 暂不支持回复功能
    }
  }
})

// 获取评论列表
const loadComments = async () => {
  try {
    const result = await getComments(props.itemId, {
      current: currentPage.value,
      size: pageSize.value
    })
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
const handleSubmit = (submitParam: SubmitParamApi) => {
  if (!authStore.isAuthed) {
    ElMessage.warning('请先登录')
    return
  }

  const { content, finish } = submitParam

  addComment(props.itemId, { content: content.trim() })
    .then((id) => {
      ElMessage.success('评论成功')
      // 创建新评论并添加到列表
      const newComment: Comment = {
        id,
        itemId: props.itemId,
        userId: authStore.userInfo!.id,
        username: authStore.userInfo!.nickname || authStore.userInfo!.username,
        content: content.trim(),
        createdAt: new Date().toISOString()
      }
      comments.value.unshift(newComment)
      total.value++
      // 完成提交
      finish(convertToCommentApi(newComment))
    })
    .catch((error) => {
      console.error('发表评论失败:', error)
      ElMessage.error('发表评论失败')
    })
}

// 点赞（暂不支持）
const handleLike = (id: string, finish: () => void) => {
  ElMessage.info('点赞功能暂未开放')
  finish()
}

// 显示用户信息
const handleShowInfo = (id: string, show: Function) => {
  // 这里可以调用后端 API 获取用户详细信息
  // 暂时使用默认显示
  show({
    homeLink: '#',
    level: 1,
    likeIds: []
  })
}

// 加载前数据处理（过滤掉需要删除的评论）
const handleBeforeData = (comment: CommentApi) => {
  // 如果当前用户是评论作者，返回 true 表示显示该评论
  // 如果不是，检查是否应该显示（管理员可以看到所有评论）
  const originalComment = comments.value.find(c => String(c.id) === comment.id)
  if (!originalComment) return true

  // 只有评论作者和管理员可以看到删除按钮
  const canDeleteItem = authStore.userInfo?.id === originalComment.userId

  // 如果可以删除，添加一个自定义属性
  if (canDeleteItem) {
    ;(comment as any).canDelete = true
  }

  return true
}

// 判断是否可以删除评论
const canDelete = (comment: CommentApi) => {
  return authStore.userInfo?.id === Number(comment.uid)
}

// 删除评论
const handleDelete = async (comment: CommentApi) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteComment(Number(comment.id))
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

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* undraw-ui 评论组件样式调整 */
:deep(.u-comment) {
  padding: 0;
}

:deep(.u-comment .u-comment-box) {
  border-radius: 8px;
}

:deep(.u-comment .u-comment-item) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 0;
}

:deep(.u-comment .u-comment-item:last-child) {
  border-bottom: none;
}
</style>
