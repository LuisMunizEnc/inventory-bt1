import categoriesReducer, { fetchCategories } from "../../../store/slices/categoriesSlice"

jest.mock("../../../services/categoryService")

const initialState = {
  categories: [],
  loading: false,
  error: null,
}

describe("categoriesSlice reducer", () => {
  test("givenInitialState_whenFetchCategoriesPending_thenSetsLoading", () => {
    // when
    const state = categoriesReducer(initialState, { type: fetchCategories.pending.type })

    // then
    expect(state.loading).toBe(true)
    expect(state.error).toBeNull()
  })

  test("givenInitialState_whenFetchCategoriesFulfilled_thenSetsCategories", () => {
    // when
    const payload = [{ categoryName: "Cat" }]
    const state = categoriesReducer(initialState, { type: fetchCategories.fulfilled.type, payload })

    // then
    expect(state.loading).toBe(false)
    expect(state.categories).toEqual(payload)
  })

  test("givenInitialState_whenFetchCategoriesRejected_thenSetsError", () => {
    // when
    const error = { message: "Error fetching categories" }
    const state = categoriesReducer(initialState, { type: fetchCategories.rejected.type, error })

    // then
    expect(state.loading).toBe(false)
    expect(state.error).toBe("Error fetching categories")
  })
})