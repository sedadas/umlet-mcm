<script>
import { Button } from "@/components/ui/button";
import Input from "@/components/ui/input/Input.vue";
import { useToast } from "@/components/ui/toast/use-toast";
import { AppConfig } from "@/config";
import axios from "axios";

export default {
  setup() {
    const { toast } = useToast();
    return { toast };
  },
  components: {
    Button,
    Input,
  },

  data() {
    return {
      email: "",
      password: "",
      errorMessage: "",
    };
  },

  methods: {
    async handleLogin() {
      const token = btoa(`${this.email}:${this.password}`); // base64 encode

      axios
        .get(AppConfig.apiBaseUrl + "/api/v1/users/self", {
          headers: {
            "Access-Control-Allow-Origin": "*",
            Authorization: `Basic ${token}`,
          },
        })
        .then((response) => {
          // Save token
          document.cookie = `authHeader=Basic ${token}; SameSite=Strict; path=/; max-age=86400`;
          localStorage.setItem("currentUser", this.email);
          this.$router.push("/");
        })
        .catch((error) => {
          console.error("Auth failed:", error);
          if (error.status === 401) {
            this.toast({
              title: "Either email or password is incorrect.",
              duration: 3000,
            });
          }
        });
    },
  },
};
</script>

<template>
  <div
    class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4"
  >
    <img src="/mcm.png" alt="TU Wien Logo" class="mb-3 w-48" />
    <h1 class="text-4xl font-semibold text-gray-800 mb-4">
      UMLet Model Change Management
    </h1>

    <h2 class="text-4xl font-semibold text-gray-800 mb-4">Login</h2>
    <form @submit.prevent="handleLogin">
      <div class="mb-2">
        <label>Email</label>
        <Input
          v-model="email"
          placeholder="Enter your email"
          type="email"
          required
        />
      </div>
      <div class="mb-6">
        <label>Password</label>
        <Input
          v-model="password"
          placeholder="Enter your password"
          type="password"
          required
        />
      </div>

      <Button
        type="submit"
        class="w-full flex items-center gap-2"
        variant="outline"
      >
        Login
      </Button>
      <p v-if="errorMessage" style="color: red">{{ errorMessage }}</p>
    </form>
    <div class="flex items-center mt-3">
      <img src="/tu_logo.svg" alt="TU Wien Logo" class="w-12 m-2" />
    </div>
  </div>
</template>
