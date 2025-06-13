<script setup lang="ts">
import axios from 'axios';
import {Button} from "@/components/ui/button";
import {onMounted, ref} from "vue";
import type {UserRole} from "@/types/User";
import {getAllUserRoles} from "@/api/userRole.ts";
import {ScrollArea} from "@/components/ui/scroll-area";
import UserRoleView from "@/components/user-management/UserRoleView.vue";
import {HelpCircle} from 'lucide-vue-next'
import {User} from 'lucide-vue-next'
import { useRouter } from 'vue-router';
const router = useRouter();


// variables
const errorMessage = ref<string | undefined>(undefined);
const userRoles = ref<UserRole[]>([]);

// functions
/**
 * Fetch all user Roles
 * Uses the getAllUserRoles function from the user API
 */
const fetchUserRoles = async () => {
  try {
    userRoles.value = await getAllUserRoles();
    errorMessage.value = undefined
  } catch (error: any) {
    errorMessage.value = "Unable to fetch user Roles: " + error.message
  }
};
const logout = () => {
    document.cookie = "authHeader=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    router.push('/login');
};

// lifecycle
/**
 * Fetch all userRoless on mounted
 */
onMounted(() => {
  errorMessage.value = undefined
  fetchUserRoles();
});


</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4">
    <img src="/mcm.png" alt="TU Wien Logo" class="mb-3 w-48"/>
    <h1 class="text-4xl font-semibold text-gray-800 mb-4">
      UMLet Model Change Management
    </h1>

    <div class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-hidden">
      <div class="flex justify-center p-2">
        <h1 class="text-4xl font-semibold text-gray-800 mb-4">
          User Role Management
        </h1>
      </div>
      <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
        <label v-else class="text-sm font-medium text-green-500">Database connection OK</label>
      </div>
        <div class="space-y-2">
          <ScrollArea class="h-52 rounded-md border">
          <UserRoleView
              v-for="role in userRoles"
              :key="role.name"
              :userRole="role"
              @click="$router.push({ name: 'editRole', params: {id: role.name}})"
          />
          </ScrollArea>
            <Button @click="$router.push({ name: 'newRole'})" class="w-full flex items-center gap-2" variant="outline">
              Add Role
            </Button>
        </div>
    </div>

    <div class="flex items-center mt-3">
      <img src="/tu_logo.svg" alt="TU Wien Logo" class="w-12 m-2"/>
      <Button @click="logout()" class="w-full flex items-center gap-2" variant="outline">
        Logout
      </Button>

      <Button @click="$router.push({ name: 'home'})" class="w-full flex items-center gap-2" variant="outline">
        Home
      </Button>

      <Button @click="$router.push({ name: 'userManagement'})" class="w-full flex items-center gap-2" variant="outline">
        <User/>
        User Management
      </Button>

      <Button @click="$router.push({ name: 'userRoleManagement'})" class="w-full flex items-center gap-2" variant="outline">
        Role Management
      </Button>

      <Button @click="$router.push({ name: 'help'})" class="w-full flex items-center gap-2" variant="outline">
        <HelpCircle/>
        How to use
      </Button>
    </div>
  </div>
</template>