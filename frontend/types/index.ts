export interface Category {
    cagegoryName: string
}

export interface ProductInfo {
    id?: string
    name: string
    category: Category
    unitPrice: number
    expirationDate?: string
    inStock: number
}

export interface ProductFilters {
    name?: string
    categories?: string[]
    inStock?: boolean | null
}  