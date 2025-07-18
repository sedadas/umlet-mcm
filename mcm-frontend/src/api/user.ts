import { AppConfig } from "@/config";
import { NewUser, User } from "@/types/User";
import axios from "axios";

function getCookie(name: string): string | null {
  const cookies = document.cookie.split("; ");
  for (const cookie of cookies) {
    const [key, value] = cookie.split("=");
    if (key === name) {
      return decodeURIComponent(value);
    }
  }
  return null;
}

const apiClient = axios.create({
  baseURL: AppConfig.apiBaseUrl + "/api/v1/users",
  headers: {
    "Content-Type": "application/json",
    Authorization: getCookie("authHeader"),
  },
});

/**
 * Get all users from the server
 * @return a list of all users
 */
export const getAllUsers = async (): Promise<User[]> => {
  try {
    const response = await apiClient.get("");
    return response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Get user from the server
 * @param id: the id of the user being requested
 * @return the requested user
 */
export const getUsersById = async (id: String): Promise<NewUser> => {
  try {
    const response = await apiClient.get(`/${id}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Create a new user on the the server
 * @param newUser: the user to be created
 * @return the newly created user
 */
export const createUser = async (newUser: NewUser): Promise<User> => {
  try {
    const response = await apiClient.post("", newUser);
    return response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * update a user on the the server
 * @param user: the user to be updated
 * @return the updated created user
 */
export const updateUser = async (user: NewUser): Promise<NewUser> => {
  if (user.password === "") {
    delete user.password; // Remove password if it's empty such that it does not get updated
  }
  try {
    const response = await apiClient.put("", user);
    return response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * delete a user on the the server
 * @param id: the user to be deleted
 */
export const deleteUser = async (id: String): Promise<void> => {
  try {
    const response = await apiClient.delete(`/${id}`);
  } catch (error) {
    throw error;
  }
};
