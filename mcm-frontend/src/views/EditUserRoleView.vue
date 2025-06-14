<script setup lang="ts">
import {Button} from "@/components/ui/button";
import type {UserRole} from "@/types/User";
import {getUserRolesById, updateRole, deleteRole} from "@/api/userRole.ts";
import {HelpCircle} from 'lucide-vue-next'
import {User} from 'lucide-vue-next'
import {onMounted, ref} from "vue";
import Multiselect from 'vue-multiselect'
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const errorMessage = ref<string | undefined>(undefined);
const newUserRole = ref<UserRole>({
  name: '',
  permissions: ['']
});

// functions
/**
 * Fetch all user Roles
 * Uses the getAllUserRoles function from the user API
 */
const updateNewUserRole = async () => {
  try {
    await updateRole(newUserRole.value);
    errorMessage.value = undefined
    router.push({ name: 'userRoleManagement'})
  } catch (error: any) {
    errorMessage.value = "Unable to update role: " + error.message
  }
};

const fetchUserRole = async () => {
  try {
    newUserRole.value = await getUserRolesById(route.params.id as string);
    errorMessage.value = undefined
  } catch (error: any) {
    errorMessage.value = "Unable to fetch role: " + error.message
  }
};

const removeRole = async (roleName) => {
  try {
    const confirmed = confirm("Are you sure?");
    if(confirmed) {
      await deleteRole(roleName as string);
      errorMessage.value = undefined
      router.push({ name: 'userRoleManagement'})
    }
  } catch (error: any) {
    errorMessage.value = "Unable to delete role: " + error.message
  }
};



const logout = () => {
    document.cookie = "authHeader=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    router.push('/login');
};

const checkToAddNewInput = (index) => {
  if (index === newUserRole.value.permissions.length - 1 && newUserRole.value.permissions[index].trim() !== '') {
    newUserRole.value.permissions.push('');
  }
};
// lifecycle
/**
 * Fetch all user Roles on mounted
 */
onMounted(() => {
  errorMessage.value = undefined;
  fetchUserRole();
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
          Update User Role
        </h1>
      </div>

      <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
        <label v-else class="text-sm font-medium text-green-500">Database connection OK</label>
      </div>

      <form @submit.prevent="updateNewUserRole">
        <div>
          <label>Name:</label>
          <input v-model.trim="newUserRole.name" placeholder="Enter Role Name" type="text" required />
        </div>

        <label>Permissions:</label>

        <div v-for="(permission, index) in newUserRole.permissions" :key="index">
          <input
            type="text"
            v-model="newUserRole.permissions[index]"
            @input="checkToAddNewInput(index)"
            placeholder="Enter text"
          />
        </div>

        <Button type="submit" class="w-full flex items-center gap-2" variant="outline">
          Upate User Role
        </Button>
      </form>
      <Button @click="removeRole(newUserRole.name)" class="w-full flex items-center gap-2" variant="outline">
        Delete User Role
      </Button>
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
