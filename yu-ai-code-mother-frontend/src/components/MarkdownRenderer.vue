<template>
  <!-- 流式生成期间使用纯文本渲染，避免每个 token 都重新解析 Markdown 和高亮代码 -->
  <pre v-if="streaming" class="streaming-output">{{ content }}</pre>
  <div v-else class="markdown-content" v-html="renderedMarkdown"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 代码高亮使用深色主题，与浅色气泡形成舒适的对比
import 'highlight.js/styles/github-dark.css'

// 超长代码块跳过高亮，避免一次性高亮几十 KB 代码阻塞主线程
const MAX_HIGHLIGHT_SIZE = 60000

interface Props {
  content: string
  streaming?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  streaming: false,
})

// 配置 markdown-it 实例
const md: MarkdownIt = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (str.length > MAX_HIGHLIGHT_SIZE) {
      return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
    }
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch {
        // 忽略错误，使用默认处理
      }
    }

    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})

// 计算渲染后的 Markdown
const renderedMarkdown = computed(() => {
  return md.render(props.content)
})
</script>

<style scoped>
.streaming-output {
  max-height: 340px;
  margin: 0;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: var(--font-mono);
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--ink-3);
}

.markdown-content {
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.75;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 1.35em 0 0.55em;
  color: var(--ink);
  font-weight: 650;
  line-height: 1.4;
  letter-spacing: -0.01em;
}

.markdown-content :deep(h1) {
  font-size: 1.35em;
}

.markdown-content :deep(h2) {
  font-size: 1.2em;
}

.markdown-content :deep(h3) {
  font-size: 1.08em;
}

.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  font-size: 1em;
}

.markdown-content :deep(h1:first-child),
.markdown-content :deep(h2:first-child),
.markdown-content :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-content :deep(p) {
  margin: 0.75em 0;
}

.markdown-content :deep(strong) {
  color: var(--ink);
  font-weight: 650;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.75em 0;
  padding-left: 1.35em;
}

.markdown-content :deep(li) {
  margin: 0.28em 0;
}

.markdown-content :deep(li::marker) {
  color: var(--brand-400);
}

.markdown-content :deep(blockquote) {
  margin: 1em 0;
  padding: 8px 12px;
  border-left: 3px solid var(--brand-200);
  border-radius: 0 8px 8px 0;
  background: rgba(238, 242, 255, 0.6);
  color: var(--muted);
}

.markdown-content :deep(blockquote p) {
  margin: 0.2em 0;
}

.markdown-content :deep(code) {
  padding: 2px 5px;
  border-radius: 5px;
  border: 1px solid #e0e7ff;
  background: #f3f4ff;
  color: #4338ca;
  font-family: var(--font-mono);
  font-size: 0.87em;
}

.markdown-content :deep(pre) {
  margin: 1em 0;
  padding: 14px 16px;
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: #0f172a;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.markdown-content :deep(pre code) {
  display: block;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: #e2e8f0;
  font-size: 11.5px;
  line-height: 1.7;
  white-space: pre;
}

.markdown-content :deep(.hljs) {
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: #0f172a !important;
  color: #e2e8f0;
  font-family: var(--font-mono);
}

.markdown-content :deep(table) {
  width: 100%;
  margin: 1em 0;
  border-collapse: separate;
  border-spacing: 0;
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
  font-size: 12px;
}

.markdown-content :deep(table th),
.markdown-content :deep(table td) {
  padding: 7px 10px;
  text-align: left;
  border-bottom: 1px solid var(--line);
}

.markdown-content :deep(table th) {
  background: var(--surface-soft);
  color: var(--muted);
  font-weight: 600;
}

.markdown-content :deep(table tr:last-child td) {
  border-bottom: 0;
}

.markdown-content :deep(a) {
  color: var(--brand-600);
  font-weight: 500;
  text-decoration: underline;
  text-decoration-color: rgba(99, 102, 241, 0.35);
  text-underline-offset: 2px;
  transition: color 0.2s ease;
}

.markdown-content :deep(a:hover) {
  color: var(--brand-500);
  text-decoration-color: var(--brand-400);
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 0.6em 0;
  border-radius: 10px;
  border: 1px solid var(--line);
}

.markdown-content :deep(hr) {
  margin: 1.4em 0;
  border: 0;
  border-top: 1px dashed var(--line-strong);
}

.markdown-content :deep(del) {
  color: var(--muted-2);
}

/* 代码高亮补充配色（深色背景下更柔和） */
.markdown-content :deep(.hljs-comment),
.markdown-content :deep(.hljs-quote) {
  color: #8b9bab;
  font-style: italic;
}

.markdown-content :deep(.hljs-keyword),
.markdown-content :deep(.hljs-selector-tag) {
  color: #c4b5fd;
}

.markdown-content :deep(.hljs-string),
.markdown-content :deep(.hljs-attr) {
  color: #86efac;
}

.markdown-content :deep(.hljs-number),
.markdown-content :deep(.hljs-literal) {
  color: #fdba74;
}

.markdown-content :deep(.hljs-title),
.markdown-content :deep(.hljs-function) {
  color: #93c5fd;
}

.markdown-content :deep(.hljs-tag) {
  color: #f9a8d4;
}
</style>
