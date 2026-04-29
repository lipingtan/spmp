/* eslint-env node */
// postcss 配置 - 移动端 px 转 vw 适配方案
module.exports = {
  plugins: {
    'postcss-px-to-viewport-8-plugin': {
      viewportWidth: 375, // 设计稿基准宽度
      unitPrecision: 5, // 转换精度
      viewportUnit: 'vw', // 转换单位
      selectorBlackList: ['.ignore'], // 忽略的选择器
      minPixelValue: 1, // 最小转换像素
      mediaQuery: false, // 不转换媒体查询中的 px
      exclude: [/node_modules\/vant/] // 排除 Vant 组件（Vant 已内置适配）
    }
  }
}
