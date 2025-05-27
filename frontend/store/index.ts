import { configureStore } from "@reduxjs/toolkit"
import productsReducer from "./slices/productsSlice"
import categoriesReducer from "./slices/categoriesSlice"

export const store = configureStore({
  reducer: {
    products: productsReducer,
    categories: categoriesReducer,
  },
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
