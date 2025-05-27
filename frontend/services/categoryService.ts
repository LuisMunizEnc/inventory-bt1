import { api } from "./api"
import type { Category } from "../types"

export const categoryService = {
  getAllCategories: async (): Promise<Category[]> => {
    const response = await api.get("/categories")
    return response.data
  },

  getCategoryByName: async (name: string): Promise<Category> => {
    const response = await api.get(`/categories/${name}`)
    return response.data
  },

  createCategory: async (category: Category): Promise<Category> => {
    const response = await api.post("/categories", category)
    return response.data
  },
}