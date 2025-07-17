<script setup lang="ts">
import { createUser } from "@/api/user.ts";
import { getAllUserRoles } from "@/api/userRole.ts";
import { Button } from "@/components/ui/button";
import Input from "@/components/ui/input/Input.vue";
import { useToast } from "@/components/ui/toast/use-toast";
import type { NewUser, UserRole } from "@/types/User";
import { ChevronLeft, HelpCircle, User as UserIcon } from "lucide-vue-next";
import { onMounted, ref } from "vue";
import Multiselect from "vue-multiselect";
import { useRouter } from "vue-router";

const router = useRouter();
const errorMessage = ref<string | undefined>(undefined);
const newUser = ref<NewUser>({
  username: "",
  password: "",
  roles: [],
});
const userRoles = ref<UserRole[]>([]);
const { toast } = useToast();
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

const createNewUser = async () => {
  try {
    await createUser(newUser.value);
    errorMessage.value = undefined;
    router.push({ name: "userManagement" });
  } catch (error: any) {
    if (error.status === 400) {
      toast({
        title: "Password is not strong enough.",
        duration: 3000,
      });
    }
    errorMessage.value = "Unable to create user: " + error.message;
  }
};

// lifecycle
/**
 * Fetch all user Roles on mounted
 */
onMounted(() => {
  errorMessage.value = undefined;
  fetchUserRoles();
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
      class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-visible p-4"
    >
      <div class="flex items-center justify-start mb-4 gap-4">
        <Button
          @click="$router.push({ name: 'userManagement' })"
          variant="outline"
        >
          <ChevronLeft />
        </Button>
        <h1 class="text-4xl font-semibold text-gray-800">Create User</h1>
      </div>

      <!-- <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{
          errorMessage
        }}</label>
        <label v-else class="text-sm font-medium text-green-500"
          >Database connection OK</label
        >
      </div> -->

      <form @submit.prevent="createNewUser">
        <div
          class="flex items-center justify-between my-6 border-y-1 border-gray-500"
        >
          <label>Email</label>
          <Input
            v-model.trim="newUser.username"
            placeholder="Enter your email"
            type="email"
            required
            class="w-[75%]"
          />
        </div>
        <div
          class="flex items-center justify-between my-6 border-y-1 border-gray-500"
        >
          <label>Password</label>
          <Input
            v-model.trim="newUser.password"
            placeholder="Enter your password"
            type="password"
            required
            class="w-[75%]"
          />
          <!-- TODO: pw strength -->
        </div>

        <div
          class="flex items-center justify-between my-6 border-y-1 border-gray-500"
        >
          <label> Role </label>
          <div class="w-[75%]">
            <multiselect
              id="multiselect"
              v-model="newUser.roles"
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
            </multiselect>
          </div>
        </div>
        <div class="flex justify-end">
          <Button
            type="submit"
            class="bg-green-600 text-white"
            variant="outline"
          >
            <UserPlus />
            Create User
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
