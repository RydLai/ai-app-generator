<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

/**
 * 全局设计令牌：与 src/styles/theme.css 中的 CSS 变量保持一致。
 * 主色为靛蓝紫，强调渐变用于关键操作，整体保持克制、通透的观感。
 */
const theme = {
  token: {
    colorPrimary: '#6366f1',
    colorInfo: '#6366f1',
    colorSuccess: '#10b981',
    colorWarning: '#f59e0b',
    colorError: '#ef4444',
    colorTextBase: '#0f172a',
    colorBorder: '#dfe5ef',
    colorBorderSecondary: '#e8ecf4',
    colorBgLayout: 'transparent',
    colorBgContainer: '#ffffff',
    borderRadius: 10,
    borderRadiusSM: 8,
    borderRadiusLG: 18,
    controlHeight: 38,
    fontSize: 14,
    fontFamily:
      "'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif",
    wireframe: false,
    boxShadowSecondary:
      '0 18px 40px -24px rgba(30, 41, 59, 0.25), 0 4px 12px -6px rgba(30, 41, 59, 0.08)',
  },
  components: {
    Button: {
      primaryShadow: 'none',
      fontWeight: 500,
    },
    Table: {
      headerBg: '#f8fafc',
      headerColor: '#64748b',
      headerSplitColor: 'transparent',
      rowHoverBg: '#f7f8ff',
      borderColor: '#e8ecf4',
      headerBorderRadius: 0,
    },
    Menu: {
      itemBorderRadius: 10,
      itemSelectedBg: '#eef2ff',
      itemSelectedColor: '#4f46e5',
      horizontalItemSelectedColor: '#4f46e5',
      horizontalItemBorderRadius: 10,
    },
    Input: {
      activeShadow: '0 0 0 3px rgba(99, 102, 241, 0.12)',
      hoverBorderColor: '#a5b4fc',
    },
    Select: {
      optionSelectedBg: '#eef2ff',
    },
    Modal: {
      borderRadiusLG: 24,
      titleFontSize: 18,
    },
    Pagination: {
      itemActiveBg: '#eef2ff',
    },
    Tag: {
      defaultBg: '#f1f5f9',
      defaultColor: '#475569',
    },
    Layout: {
      headerBg: 'transparent',
      bodyBg: 'transparent',
      footerBg: 'transparent',
    },
    Card: {
      headerFontSize: 15,
    },
    Descriptions: {
      labelBg: '#f8fafc',
    },
  },
}
</script>

<template>
  <a-config-provider :theme="theme" :locale="zhCN">
    <BasicLayout />
  </a-config-provider>
</template>

<style>
* {
  box-sizing: border-box;
}

html,
body {
  overflow-x: hidden;
}
</style>
