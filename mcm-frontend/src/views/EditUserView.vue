<script setup lang="ts">
import { deleteUser, getUsersById, updateUser } from "@/api/user.ts";
import { getAllUserRoles } from "@/api/userRole.ts";
import { Button } from "@/components/ui/button";
import Input from "@/components/ui/input/Input.vue";
import { useToast } from "@/components/ui/toast/use-toast";
import type { NewUser, UserRole } from "@/types/User";
import {
  ChevronLeft,
  HelpCircle,
  User as UserIcon,
  UserMinus,
  UserPlus,
} from "lucide-vue-next";
import { onMounted, ref } from "vue";
import Multiselect from "vue-multiselect";
import { useRoute, useRouter } from "vue-router";

const { toast } = useToast();
const router = useRouter();
const route = useRoute();

const errorMessage = ref<string | undefined>(undefined);
let user = ref<NewUser>({
  username: "",
  password: "",
  roles: [],
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
    errorMessage.value = undefined;
  } catch (error: any) {
    errorMessage.value = "Unable to fetch user Roles: " + error.message;
  }
};

const logout = () => {
  document.cookie =
    "authHeader=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
  router.push("/login");
};

const fetchUser = async () => {
  try {
    user.value = await getUsersById(route.params.id as string);
    user.value.password = ""; // Do not show password in the form
    errorMessage.value = undefined;
  } catch (error: any) {
    errorMessage.value = "Unable to fetch: " + error.message;
  }
};

const patchUser = async () => {
  try {
    user.value = await updateUser(user.value);
    user.value.password = ""; // Do not show password in the form
    errorMessage.value = undefined;
    toast({
      title: "User updated successfully.",
      duration: 3000,
    });
  } catch (error: any) {
    if (error.status === 400) {
      toast({
        title: "Password is not strong enough.",
        duration: 3000,
      });
      errorMessage.value =
        "Unable to update user: " + error.response.data.error;
    }

    errorMessage.value = "Unable to update user: " + error.message;
  }
};

const removeUser = async (username: string) => {
  if (username === localStorage.getItem("currentUser")) {
    console.error("Cannot delete logged in user.");
    errorMessage.value = "You cannot delete logged in user.";
    toast({
      title: errorMessage.value,
      duration: 3000,
    });
    return;
  }

  try {
    const confirmed = confirm("Are you sure?");
    if (confirmed) {
      await deleteUser(username);
      toast({
        title: "User updated successfully.",
        duration: 3000,
      });
    }
  } catch (error: any) {
    errorMessage.value = "Unable to delete user: " + error.message;
  }
};

// lifecycle
/**
 * Fetch all user Roles on mounted
 */
onMounted(() => {
  errorMessage.value = undefined;
  fetchUserRoles();
  fetchUser();
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
      class="bg-white rounded-lg shadow-lg w-full max-w-4xl p-4 overflow-visible"
    >
      <div class="flex items-center justify-start mb-4 gap-4">
        <Button
          @click="$router.push({ name: 'userManagement' })"
          variant="outline"
        >
          <ChevronLeft />
        </Button>
        <h1 class="text-4xl font-semibold text-gray-800">
          Edit User: {{ user.username }}
        </h1>
      </div>

      <!-- <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{
          errorMessage
        }}</label>
        <label v-else class="text-sm font-medium text-green-500"
          >Database connection OK</label
        >
      </div> -->

      <form @submit.prevent="patchUser">
        <!-- <div
          class="flex items-center justify-between my-6 border-y-1 border-gray-500"
        >
          <label>Username</label>
          <Input
            v-model.trim="user.username"
            placeholder="Enter your email"
            type="email"
            required
            class="w-[75%]"
          />
        </div> -->
        <div
          class="flex items-center justify-between my-6 border-y-1 border-gray-500"
        >
          <label>Password</label>
          <Input
            v-model.trim="user.password"
            placeholder="Enter new password"
            type="password"
            class="w-[75%]"
          />
          <!-- TODO Check for PW strength -->
        </div>

        <!-- <label v-for="role in user.roles" :key="role.name"> {{ role.name }} {{ role.permissions }} </label> -->

        <div
          class="flex items-center justify-between my-6 border-y-1 border-gray-500"
        >
          <label>Role</label>
          <div class="w-[75%]">
            <Multiselect
              id="multiselect"
              v-model="user.roles"
              :options="userRoles"
              :multiple="true"
              :close-on-select="false"
              :clear-on-select="false"
              :preserve-search="true"
              placeholder="User Roles"
              track-by="name"
              label="name"
              :preselect-first="true"
            >
              <template #selection="{ values, search, isOpen }">
                <span
                  class="multiselect__single"
                  v-if="values.length"
                  v-show="!isOpen"
                  >{{ values.length }} options selected</span
                >
              </template>
            </Multiselect>
          </div>
        </div>

        <div class="flex mt-6 gap-4 justify-end">
          <Button
            @click="removeUser(user.username)"
            type="button"
            class="flex items-center gap-2 bg-red-600 text-white hover:bg-red-700 hover:text-white"
            variant="outline"
          >
            <UserMinus />
            Delete User
          </Button>
          <Button
            type="submit"
            class="flex items-center gap-2"
            variant="outline"
          >
            <UserPlus />
            Update User
          </Button>
        </div>
      </form>
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

<style src="vue-multiselect/dist/vue-multiselect.css"></style>
