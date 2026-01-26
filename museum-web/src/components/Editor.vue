<template>
  <div class="editor-container">
    <Toolbar
      class="editor-toolbar"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      mode="default"
    />
    <Editor
      class="editor-content"
      :style="{ height }"
      :modelValue="modelValue"
      :defaultConfig="editorConfig"
      mode="default"
      @onCreated="handleCreated"
      @onChange="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { shallowRef, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor/dist/src/index'
import '@wangeditor/editor/dist/css/style.css'

interface Props {
  modelValue: string
  height?: string
  placeholder?: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  height: '400px',
  placeholder: '请输入内容...'
})

const emit = defineEmits<Emits>()

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef<IDomEditor>()

// 工具栏配置
const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: ['group-video'] // 排除视频按钮
}

// 编辑器配置
const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder,
  MENU_CONF: {
    // 配置上传图片
    uploadImage: {
      async customUpload(file: File, insertFn: (url: string) => void) {
        // 这里实现图片上传到服务器的逻辑
        // 暂时使用本地预览
        const url = URL.createObjectURL(file)
        insertFn(url)
      }
    }
  }
}

// 记录 editor 实例，重要！
const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor
}

// 处理内容变化
const handleChange = (editor: IDomEditor) => {
  emit('update:modelValue', editor.getHtml())
}

// 组件销毁时，也销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
</script>

<style scoped>
.editor-container {
  border: 1px solid #ccc;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  border-bottom: 1px solid #ccc;
}

.editor-content {
  overflow-y: auto;
}
</style>
