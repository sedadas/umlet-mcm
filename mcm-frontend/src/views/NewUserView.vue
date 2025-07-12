<script setup lang="ts">
import {Button} from "@/components/ui/button";
import {HelpCircle} from 'lucide-vue-next';
import type {NewUser, UserRole} from "@/types/User";
import {getAllUserRoles} from "@/api/userRole.ts";
import {createUser} from "@/api/user.ts";
import {User as UserIcon} from 'lucide-vue-next'
import {onMounted, ref} from "vue";
import Multiselect from 'vue-multiselect'
import { useRouter } from 'vue-router';
const router = useRouter();




const errors = ref<string | undefined>([]);
const errorMessage = ref<string | undefined>(undefined);
const newUser = ref<NewUser>({
  username: '',
  password: '',
  roles: []
});
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

const createNewUser = async () => {
  try {
    errors.value = [];


    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const digitPattern = /\d/;
    const lowercasePattern = /[a-z]/;
    const uppercasePattern = /[A-Z]/;
    const specialPattern = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/;
    const whitePattern = /^\S+$/;
    if (!newUser.value.username) {
        errors.value.push('Email is required');
    } else if (!emailPattern.test(newUser.value.username)) {
        errors.value.push('Please enter a valid email.');
    }

    if (!digitPattern.test(newUser.value.password)) {
        errors.value.push('Password requires at least one digit');
    }
    if (!lowercasePattern.test(newUser.value.password)) {
        errors.value.push('Password requires at least one lowercase character');
    }
    if (!uppercasePattern.test(newUser.value.password)) {
        errors.value.push('Password requires at least one uppercase character');
    }
    if (!specialPattern.test(newUser.value.password)) {
        errors.value.push('Password requires at least one special character');
    }
    if (!whitePattern.test(newUser.value.password)) {
        errors.value.push('No whitespaces allowed in the password');
    }

    if (newUser.value.password.length < 8) {
        errors.value.push('Password is too short');
    }

    if(newUser.value.roles.length === 0){
        errors.value.push('No roles selected');
    }
    if(errors.value.length != 0){
        throw new Error(errors.value);
    }
    await createUser(newUser.value);
    errorMessage.value = undefined
    router.push({ name: 'userManagement'})
  } catch (error: any) {
    errorMessage.value = "Unable to create user: " + error.response.data;
  }
};

// lifecycle
/**
 * Fetch all user Roles on mounted
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
          Create User
        </h1>
      </div>

      <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
        <label v-else class="text-sm font-medium text-green-500">Database connection OK</label>
      </div>

      <form @submit.prevent="createNewUser">
        <div>
          <label>Email:</label>
          <input v-model.trim="newUser.username" placeholder="Enter your email" type="email" required />
        </div>
        <div>
          <label>Password:</label>
          <input v-model.trim="newUser.password" placeholder="Enter your password" type="password" required />
        </div>

        <div>
          <multiselect id="multiselect" v-model="newUser.roles" :options="userRoles" :multiple="true" :close-on-select="false" :clear-on-select="false"
                       :preserve-search="true" placeholder="User Roles" track-by="name" label="name" :preselect-first="true">
            <template #selection="{ values, search, isOpen }">
              <span class="multiselect__single"
                    v-if="values.length"
                    v-show="!isOpen">{{ values.length}} options selected</span>
            </template>
          </multiselect>

          <label v-for="role in newUser.roles" :key="role.name">
            {{ role.name }} {{ role.permissions }}
          </label>
        </div>


        <Button type="submit" class="w-full flex items-center gap-2" variant="outline">
          Create User
        </Button>
      </form>

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
        <UserIcon/>
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
