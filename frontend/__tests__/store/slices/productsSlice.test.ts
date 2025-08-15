import productsReducer, { setFilters, clearFilters, fetchProducts, markProductInStock, markProductOutOfStock } from "../../../store/slices/productsSlice"

jest.mock("../../../services/productService")

const initialState = {
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

describe("productsSlice reducer", () => {
    test("givenInitialState_whenSetFilters_thenUpdatesFilters", () => {
        // given
        const newFilters = { name: "test", categories: ["Cat"], inStock: true, page: 1, size: 5, sort: "name,desc" }

        // when
        const state = productsReducer(initialState, setFilters(newFilters))

        // then
        expect(state.filters).toEqual(newFilters)
    })

    test("givenInitialState_whenClearFilters_thenResetsFilters", () => {
        // when
        const state = productsReducer(initialState, clearFilters())

        // then
        expect(state.filters).toEqual({
            name: "",
            categories: [],
            inStock: null,
            page: 0,
            size: 10,
            sort: "name,asc",
        })
    })

    test("givenInitialState_whenFetchProductsPending_thenSetsLoading", () => {
        // when
        const state = productsReducer(initialState, { type: fetchProducts.pending.type })

        // then
        expect(state.loading).toBe(true)
        expect(state.error).toBeNull()
    })

    test("givenInitialState_whenFetchProductsFulfilled_thenSetsProducts", () => {
        // given
        const payload = {
            content: [{ id: 1, name: "Product1" }],
            page: {
                totalElements: 1,
                totalPages: 1,
            }
        }

        // when
        const state = productsReducer(initialState, { type: fetchProducts.fulfilled.type, payload })

        // then
        expect(state.loading).toBe(false)
        expect(state.products).toEqual(payload.content)
        expect(state.totalElements).toBe(payload.page.totalElements)
        expect(state.totalPages).toBe(payload.page.totalPages)
    })

    test("givenInitialState_whenFetchProductsRejected_thenSetsError", () => {
        // when
        const error = { message: "Error fetching products" }
        const state = productsReducer(initialState, { type: fetchProducts.rejected.type, error })

        // then
        expect(state.loading).toBe(false)
        expect(state.error).toBe("Error fetching products")
    })
})