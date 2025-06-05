<template>
  <div class="login">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <label>Email:</label>
        <input v-model="email" type="email" required />
      </div>
      <div>
        <label>Password:</label>
        <input v-model="password" type="password" required />
      </div>
      <button type="submit">Login</button>
      <p v-if="errorMessage" style="color:red">{{ errorMessage }}</p>
    </form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      email: '',
      password: '',
      errorMessage: '',
    }
  },

  methods: {
    async handleLogin() {
      const token = btoa(`${this.email}:${this.password}`)  // base64 encode

      axios.get('http://localhost:9081/api/v1/users/self', {
        headers: {
          'Authorization': `Basic ${token}`
        }
      }).then(response => {
        console.log(response.data)

        // Save token (e.g., in localStorage)
        document.cookie = "authHeader=`Basic ${token}`; SameSite='Strict', path=/; max-age=86400"
        this.$router.push('/')
      }).catch(error => {
        console.error('Auth failed:', error)
      })
    }


  },
}
</script>
