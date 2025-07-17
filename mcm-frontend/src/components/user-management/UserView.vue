<script setup lang="ts">
import type { User } from "@/types/User";

/**
 * @param {User} user, user to display
 */
defineProps({
  user: {
    type: Object as () => User,
    required: true,
  },
});

/**
 * @emits {User} select, selected user
 */
defineEmits<{
  select: [user: User];
}>();
</script>

<template>
  <button
    @click="$emit('select', user)"
    class="w-full text-left p-3 rounded-lg hover:bg-gray-100 transition-colors flex items-center gap-3"
  >
    
  <div class="space-y-1">
      <p class="font-medium">
        {{ user.username }} 
        <div class="text-gray-500 text-sm" v-for="role in user.roles">
          Role: @{{ role.name }}
        </div>   
      </p>
    </div>
  </button>
</template>
