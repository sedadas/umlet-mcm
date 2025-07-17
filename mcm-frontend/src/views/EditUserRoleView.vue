<script setup lang="ts">
import { deleteRole, getUserRolesById, updateRole } from "@/api/userRole.ts";
import { Button } from "@/components/ui/button";
import Input from "@/components/ui/input/Input.vue";
import type { UserRole } from "@/types/User";
import {
  ChevronLeft,
  HelpCircle,
  SquarePlus,
  Trash,
  User,
} from "lucide-vue-next";
import { onMounted, ref } from "vue";
import Multiselect from "vue-multiselect";
import { useRoute, useRouter } from "vue-router";

const router = useRouter();
const route = useRoute();

const errorMessage = ref<string | undefined>(undefined);
const newUserRole = ref<UserRole>({
  name: "",
  permissions: [""],
});

// functions
/**
 * Fetch all user Roles
 * Uses the getAllUserRoles function from the user API
 */
const updateNewUserRole = async () => {
  try {
    await updateRole(newUserRole.value);
    errorMessage.value = undefined;
    router.push({ name: "userRoleManagement" });
  } catch (error: any) {
    errorMessage.value = "Unable to update user: " + error.response.data.error;
  }
};

const fetchUserRole = async () => {
  try {
    newUserRole.value = await getUserRolesById(route.params.id as string);
    errorMessage.value = undefined;
  } catch (error: any) {
    errorMessage.value = "Unable to fetch role: " + error.message;
  }
};

const removeRole = async (roleName: any) => {
  try {
    const confirmed = confirm("Are you sure?");
    if (confirmed) {
      await deleteRole(roleName as string);
      errorMessage.value = undefined;
      router.push({ name: "userRoleManagement" });
    }
  } catch (error: any) {
    errorMessage.value = "Unable to delete role: " + error.message;
  }
};

const addPermission = () => {
  newUserRole.value.permissions.push("");
};

const logout = () => {
  document.cookie =
    "authHeader=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
  router.push("/login");
};

const checkToAddNewInput = (index: any) => {
  if (
    index === newUserRole.value.permissions.length - 1 &&
    newUserRole.value.permissions[index].trim() !== ""
  ) {
    newUserRole.value.permissions.push("");
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
  <div
    class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4"
  >
    <img src="/mcm.png" alt="TU Wien Logo" class="mb-3 w-48" />
    <h1 class="text-4xl font-semibold text-gray-800 mb-4">
      UMLet Model Change Management
    </h1>

    <div class="bg-white rounded-lg shadow-lg w-full max-w-4xl p-4">
      <div class="flex items-center justify-start mb-4 gap-4">
        <Button
          @click="$router.push({ name: 'userRoleManagement' })"
          variant="outline"
        >
          <ChevronLeft />
        </Button>
        <h1 class="text-4xl font-semibold text-gray-800">Update Role</h1>
      </div>

      <!-- <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{
          errorMessage
        }}</label>
      </div> -->

      <form @submit.prevent="updateNewUserRole">

        <div
          class="flex items-center justify-between my-6 border-y-1 border-gray-500"
        >
          <label>Name</label>
          <Input
            v-model.trim="newUserRole.name"
            placeholder="Enter Role Name"
            type="text"
            required
            class="w-[75%]"
          />
        </div>

        <div
          class="flex items-start justify-between my-6 border-y-1 border-gray-500"
        >
          <label>Permissions</label>
          <div class="w-[75%]">
            <div
              v-for="(permission, index) in newUserRole.permissions"
              :key="index"
              class="flex items-center gap-2 mb-2"
            >
              <Input
                type="text"
                v-model="newUserRole.permissions[index]"
                placeholder="Enter text"
                required
              />
              <Button
                variant="outline"
                @click="newUserRole.permissions.splice(index, 1)"
                class="bg-red-600 text-white"
              >
                <Trash />
              </Button>
            </div>
            <p class="text-sm cursor-pointer inline" @click="addPermission()">
              <div class="flex items-center gap-2">
              <SquarePlus /> Add Permission
              </div>
            </p>
          </div>
        </div>
        <div class="flex justify-end gap-2">
          <Button
            @click="removeRole(newUserRole.name)"
            variant="outline"
            class="bg-red-600 text-white"
          >
            Delete User Role
          </Button>
          <Button type="submit" variant="outline"> Upate User Role </Button>
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
        <User />
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
