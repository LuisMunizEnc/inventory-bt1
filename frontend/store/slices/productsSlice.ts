import { createSlice, createAsyncThunk, type PayloadAction } from "@reduxjs/toolkit"
import type { Product, ProductInfo, ProductFilters } from "../../types"
import { productService } from "../../services/productService"

interface ProductsState {
  products: Product[]
  totalElements: number
  totalPages: number
  loading: boolean
  error: string | null
  filters: ProductFilters
}

const initialState: ProductsState = {
  products: [],
  totalElements: 0,
  totalPages: 0,
  loading: false,
  error: null,
  filters: {
    name: "",
    categories: [],
    inStock: null,
    page: 0,
    size: 10,
    sort: "name,asc",
  },
}

export const fetchProducts = createAsyncThunk("products/fetchProducts", async (filters?: ProductFilters) => {
  return await productService.getAllProducts(filters)
})

export const markProductOutOfStock = createAsyncThunk(
  "products/markOutOfStock",
  async (id: string) => {
    return await productService.markOutOfStock(id)
  },
)

export const markProductInStock = createAsyncThunk(
  "products/markInStock", async (id: string, { rejectWithValue }) => {
    return await productService.markInStock(id)
  },
)

const productsSlice = createSlice({
  name: "products",
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<ProductFilters>) => {
      state.filters = action.payload
    },
    clearFilters: (state) => {
      state.filters = {
        name: "",
        categories: [],
        inStock: null,
        page: 0,
        size: 10,
        sort: "name,asc",
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.loading = false
        state.products = action.payload.content
        state.totalElements = action.payload.totalElements
        state.totalPages = action.payload.totalPages
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message || "Error fetching products"
      })
      .addCase(markProductOutOfStock.fulfilled, (state) => {
        state.loading = false
      })
      .addCase(markProductInStock.fulfilled, (state) => {
        state.loading = false
      })
  },
})

export const { setFilters, clearFilters } = productsSlice.actions
export default productsSlice.reducer
