<script setup lang="ts">
import { getAllUsers } from "@/api/user.ts";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useToast } from "@/components/ui/toast/use-toast";
import UserView from "@/components/user-management/UserView.vue";
import type { User } from "@/types/User";
import axios from "axios";
import { HelpCircle, User as UserIcon, UserPlus } from "lucide-vue-next";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();

// variables
const errorMessage = ref<string | undefined>(undefined);
const users = ref<User[]>([]);
const { toast } = useToast();

// functions
/**
 * Fetch all users
 * Uses the getAllUsers function from the user API
 */
const fetchUsers = async () => {
  try {
    users.value = await getAllUsers();
    errorMessage.value = undefined;
  } catch (error: any) {
    errorMessage.value = "Unable to fetch users: " + error.message;
  }
};
const logout = () => {
  document.cookie =
    "authHeader=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
  router.push("/login");
};

// lifecycle
/**
 * Fetch all users on mounted
 */
onMounted(() => {
  errorMessage.value = undefined;
  fetchUsers();
});
</script>

<template>
  <div
    class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4"
  >
    <img src="/mcm.png" alt="TU Wien Logo" class="mb-3 w-48" />
    <h1 class="text-4xl font-semibold text-gray-800 mb-4">
      UMLet Model Change Management
    </h1>

    <div
      class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-hidden p-4"
    >
      <div class="flex items-center justify-between mb-4">
        <h1 class="text-4xl font-semibold text-gray-800">User Management</h1>
      </div>
      <!-- <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
      </div> -->
      <div>
        <ScrollArea class="h-80">
          <UserView
            v-for="user in users"
            :key="user.username"
            :user="user"
            @click="
              $router.push({ name: 'editUser', params: { id: user.username } })
            "
          />
        </ScrollArea>

        <div class="flex justify-end">
          <Button
            @click="$router.push({ name: 'newUser' })"
            variant="outline"
            class="bg-green-500 text-white hover:bg-green-600 hover:text-white"
          >
            <UserPlus />
            Add User
          </Button>
        </div>
      </div>
    </div>

    <div class="flex items-center mt-3">
      <img src="/tu_logo.svg" alt="TU Wien Logo" class="w-12 m-2" />
      <Button
        @click="logout()"
        class="w-full flex items-center gap-2"
        variant="outline"
      >
        Logout
      </Button>

      <Button
        @click="$router.push({ name: 'home' })"
        class="w-full flex items-center gap-2"
        variant="outline"
      >
        Home
      </Button>

      <Button
        @click="$router.push({ name: 'userManagement' })"
        class="w-full flex items-center gap-2"
        variant="outline"
      >
        <UserIcon />
        User Management
      </Button>

      <Button
        @click="$router.push({ name: 'userRoleManagement' })"
        class="w-full flex items-center gap-2"
        variant="outline"
      >
        Role Management
      </Button>

      <Button
        @click="$router.push({ name: 'help' })"
        class="w-full flex items-center gap-2"
        variant="outline"
      >
        <HelpCircle />
        How to use
      </Button>
    </div>
  </div>
</template>
