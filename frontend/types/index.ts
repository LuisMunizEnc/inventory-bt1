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

export interface ProductFilters {
    name?: string
    categories?: string[]
    inStock?: boolean | null
}  