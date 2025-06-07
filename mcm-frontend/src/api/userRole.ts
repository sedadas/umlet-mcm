import axios from "axios"
import {User} from "@/types/User";
import {AppConfig} from "@/config";



function getCookie(name: string): string | null {
  const cookies = document.cookie.split('; ')
  for (const cookie of cookies) {
    const [key, value] = cookie.split('=')
    if (key === name) {
      return decodeURIComponent(value)
    }
  }
  return null
}

const apiClient = axios.create({
    baseURL: 'http://localhost:9081/api/v1/users',
    //TODO fix env
    //baseURL: AppConfig.apiBaseUrl + '/api/v1/users',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': getCookie('authHeader')
    }
});

/**
 * Get all users from the server
 * @return a list of all users
 */
export const getAllUsers = async (): Promise<User[]> => {
    try {
        const response = await apiClient.get('');
        return response.data;
    } catch (error) {
        throw error;
    }
};
