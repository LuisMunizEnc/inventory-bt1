import { api } from "./api"
import type { Product, ProductInfo, ProductFilters, InventoryMetrics, ProductPage } from "../types"

export const productService = {
  getAllProducts: async (filters?: ProductFilters): Promise<ProductPage> => {
    const params = new URLSearchParams()

    if (filters?.name) {
      params.append("name", filters.name)
    }

    if (filters?.categories && filters.categories.length > 0) {
      filters.categories.forEach((category) => {
        params.append("categories", category)
      })
    }

    if (filters?.inStock !== null && filters?.inStock !== undefined) {
      params.append("inStock", filters.inStock.toString())
    }

    if(filters?.page !== undefined) {
      params.append("page", filters.page.toString())
    }

    if(filters?.size !== undefined) {
      params.append("size", filters.size.toString())
    }

    if(filters?.sort !== undefined) {
      params.append("sort", filters.sort)
    }

    const response = await api.get(`/products?${params.toString()}`)
    return response.data
  },

  getProductById: async (id: string): Promise<Product> => {
    const response = await api.get(`/products/${id}`)
    return response.data
  },

  createProduct: async (productInfo: ProductInfo): Promise<Product> => {
    const response = await api.post("/products", productInfo)
    return response.data
  },

  updateProduct: async (id: string, productInfo: ProductInfo): Promise<Product> => {
    const response = await api.put(`/products/${id}`, productInfo)
    return response.data
  },

  deleteProduct: async (id: string): Promise<void> => {
    await api.delete(`/products/${id}`)
  },

  markOutOfStock: async (id: string): Promise<void> => {
    await api.put(`/products/${id}/outofstock`)
  },

  markInStock: async (id: string): Promise<void> => {
    await api.put(`/products/${id}/instock`)
  },

  getMetrics: async (): Promise<InventoryMetrics> => {
    const response = await api.get("/products/metrics")
    return response.data
  },
}
