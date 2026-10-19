<template>
  <div class="user-info">
    <a-avatar :src="user?.userAvatar" :size="size" class="user-avatar">
      {{ user?.userName?.charAt(0) || 'U' }}
    </a-avatar>
    <span v-if="showName" class="user-name">{{ user?.userName || '未知用户' }}</span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  user?: API.UserVO
  size?: number | 'small' | 'default' | 'large'
  showName?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'default',
  showName: true,
})
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.user-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--brand-400), var(--violet));
  font-weight: 600;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
