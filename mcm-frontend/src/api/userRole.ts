import axios from "axios"
import {UserRole} from "@/types/User";
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
    baseURL: 'http://localhost:9081/api/v1/roles',
    //TODO fix env
    //baseURL: AppConfig.apiBaseUrl + '/api/v1/roles',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': getCookie('authHeader')
    }
});

/**
 * Get all usersRoles from the server
 * @return a list of all usersRoles
 */
export const getAllUserRoles = async (): Promise<UserRole[]> => {
    try {
        const response = await apiClient.get('');
        return response.data;
    } catch (error) {
        throw error;
    }
};




/**
 * Get role  from the server
 * @param id: the id of the role being requested
 * @return the requested role
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
 * Create a new role on the the server
 * @param role: the role to be created
 * @return the newly created role
 */
export const createRole = async (role: UserRole): Promise<UserRole> => {
    try {
        const response = await apiClient.post('', role);
        return response.data;
    } catch (error) {
        throw error;
    }
};


/**
 * update a role on the the server
 * @param role: the role to be updated
 * @return the updated role
 */
export const updateRole = async (role: UserRole): Promise<UserRole> => {
    try {
        const response = await apiClient.put(`/${role.name}`, role);
        return response.data;
    } catch (error) {
        throw error;
    }
};




/**
 * delete a role on the the server
 * @param id: the role to be deleted
 */
export const deleteRole = async (id: String): Promise<void> => {
    try {
        const response = await apiClient.delete(`/${id}`);
        return null;
    } catch (error) {
        throw error;
    }
};