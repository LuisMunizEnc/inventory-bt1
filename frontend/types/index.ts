export interface Category {
    categoryName: string
}

export interface ProductInfo {
    name: string
    categoryName: string
    unitPrice: number
    expirationDate?: string
    inStock: number
}
  
export interface Product {
    id: string
    name: string
    category: Category
    unitPrice: number
    expirationDate?: string
    inStock: number
    createdAt: string
    updatedAt: string
}

export interface ProductPage{
    content: Product[]
    totalElements: number
    totalPages: number
}

export interface ProductFilters {
    name?: string
    categories?: string[]
    inStock?: boolean | null
    page?: number
    size?: number
    sort?: string
}

export interface SortConfig {
    field: "name" | "category" | "unitPrice" | "inStock" | "expirationDate"
    direction: "asc" | "desc"
}

export interface CategoryMetrics {
    categoryName: string
    totalProductsInStock: number
    totalValueInStock: number
    averagePriceInStock: number
}
    
export interface OverallMetrics {
    totalProductsInStock: number
    totalValueInStock: number
    averagePriceInStock: number
}
    
export interface InventoryMetrics {
    categoryMetrics: CategoryMetrics[]
    overallMetrics: OverallMetrics
}