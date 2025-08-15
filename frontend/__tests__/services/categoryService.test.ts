import { categoryService } from "../../services/categoryService"
import { api } from "../../services/api"

jest.mock("../../services/api")
const mockedApi = api as jest.Mocked<typeof api>

describe("categoryService", () => {
  beforeEach(() => jest.resetAllMocks())

  test("givenNothing_whenGetAllCategories_thenCallsEndpointAndReturnsCategories", async () => {
    // given
    mockedApi.get.mockResolvedValueOnce({ data: [{ categoryName: "Cat" }] })

    // when
    const categories = await categoryService.getAllCategories()

    // then
    expect(mockedApi.get).toHaveBeenCalledWith("/categories")
    expect(categories[0].categoryName).toBe("Cat")
  })

  test("givenCategoryName_whenGetCategoryByName_thenCallsEndpointAndReturnsCategory", async () => {
    // given
    mockedApi.get.mockResolvedValueOnce({ data: { categoryName: "Cat" } })

    // when
    const category = await categoryService.getCategoryByName("Cat")

    // then
    expect(mockedApi.get).toHaveBeenCalledWith("/categories/Cat")
    expect(category.categoryName).toBe("Cat")
  })

  test("givenCategory_whenCreateCategory_thenPostsAndReturnsCategory", async () => {
    // given
    mockedApi.post.mockResolvedValueOnce({ data: { categoryName: "Cat" } })

    // when
    const category = await categoryService.createCategory({ categoryName: "Cat" })

    // then
    expect(mockedApi.post).toHaveBeenCalledWith("/categories", { categoryName: "Cat" })
    expect(category.categoryName).toBe("Cat")
  })
})