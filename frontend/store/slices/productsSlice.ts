import { createSlice, createAsyncThunk, type PayloadAction } from "@reduxjs/toolkit"
import type { ProductInfo, ProductFilters } from "../../types"
import { productService } from "../../services/productService"

interface ProductsState {
  products: ProductInfo[]
  loading: boolean
  error: string | null
  filters: ProductFilters
}

const initialState: ProductsState = {
  products: [],
  loading: false,
  error: null,
  filters: {
    name: "",
    categories: [],
    inStock: null,
  },
}

export const fetchProducts = createAsyncThunk("products/fetchProducts", async (filters?: ProductFilters) => {
  return await productService.getAllProducts(filters)
})

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
        console.log("Fetched products:", action.payload)

        state.loading = false
        state.products = action.payload
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message || "Error fetching products"
      })
  },
})

export const { setFilters, clearFilters } = productsSlice.actions
export default productsSlice.reducer
