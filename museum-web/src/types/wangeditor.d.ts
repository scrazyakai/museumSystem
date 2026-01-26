declare module '@wangeditor/editor-for-vue' {
  import { DefineComponent } from 'vue'
  const Editor: DefineComponent
  const Toolbar: DefineComponent
  export default Editor
  export { Toolbar }
}

declare module '@wangeditor/editor/dist/src/index' {
  export * from '@wangeditor/editor'
}

