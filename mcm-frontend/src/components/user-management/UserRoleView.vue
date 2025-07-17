<script setup lang="ts">
import type { UserRole } from "@/types/User";

/**
 * @param {UserRole} userRole, userRole to display
 */
defineProps({
  userRole: {
    type: Object as () => UserRole,
    required: true,
  },
});

/**
 * @emits {UserRole} select, selected userRole
 */
defineEmits<{
  select: [userRole: UserRole];
}>();
</script>

<template>
  <button
    @click="$emit('select', userRole)"
    class="w-full text-left p-3 rounded-lg hover:bg-gray-100 transition-colors flex items-center gap-3"
  >
    <div class="">
      <p class="font-medium">
        {{ userRole.name }}
      </p>
      <p class="text-gray-500" v-for="permission in userRole.permissions">
        @{{ permission }}
      </p>
    </div>
  </button>
</template>
