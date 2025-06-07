import axios from "axios"
import { AppConfig } from "@/config";
import {Model} from "@/types/Model.ts";
import {Configuration} from "@/types/Configuration.ts";


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
    baseURL: AppConfig.apiBaseUrl + '/api/v1',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': getCookie('authHeader')
    }
});

/**
 * Delete a model from a configuration
 * @param modelId the id of the model to delete
 */
export const deleteModelFromConfig = async (modelId: string): Promise<Configuration> => {
    try {
        const response = await apiClient.delete(`/model/${modelId}`)
        return response.data
    } catch (error) {
        throw error
    }
}

export const alignModels = async (models: Model[]): Promise<Model[]> => {
    try {
        const response = await apiClient.post('/model/alignModels', models);
        return response.data
    } catch (error) {
        throw error
    }
}