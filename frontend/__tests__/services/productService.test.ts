import { productService } from "../../services/productService"
import { api } from "../../services/api"

jest.mock("../../services/api")
const mockedApi = api as jest.Mocked<typeof api>

describe("productService", () => {
  beforeEach(() => jest.resetAllMocks())

  test("givenFilters_whenGetAllProducts_thenCallsEndpointWithParamsAndReturnsData", async () => {
    // given
    const filters = { name: "Test", categories: ["Electronics"], inStock: true, page: 1, size: 5, sort: "name,asc" }
    mockedApi.get.mockResolvedValueOnce({ data: { content: [], totalElements: 0, totalPages: 0 } })

    // when
    const result = await productService.getAllProducts(filters)

    // then
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("/products?"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("name=Test"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("categories=Electronics"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("inStock=true"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("page=1"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("size=5"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("sort=name%2Casc"))
    expect(result).toEqual({ content: [], totalElements: 0, totalPages: 0 })
  })

  test("givenProductId_whenGetProductById_thenCallsEndpointAndReturnsProduct", async () => {
    // given
    mockedApi.get.mockResolvedValueOnce({ data: { id: "1" } })

    // when
    const product = await productService.getProductById("1")

    // then
    expect(mockedApi.get).toHaveBeenCalledWith("/products/1")
    expect(product.id).toBe("1")
  })

  test("givenProductInfo_whenCreateProduct_thenPostsAndReturnsProduct", async () => {
    // given
    const info = { name: "Test", categoryName: "Cat", unitPrice: 10, inStock: 1 }
    mockedApi.post.mockResolvedValueOnce({ data: { id: "1" } })

    // when
    const product = await productService.createProduct(info)

    // then
    expect(mockedApi.post).toHaveBeenCalledWith("/products", info)
    expect(product.id).toBe("1")
  })

  test("givenProductIdAndInfo_whenUpdateProduct_thenPutsAndReturnsProduct", async () => {
    // given
    const info = { name: "Test", categoryName: "Cat", unitPrice: 10, inStock: 1 }
    mockedApi.put.mockResolvedValueOnce({ data: { id: "1" } })

    // when
    const product = await productService.updateProduct("1", info)

    // then
    expect(mockedApi.put).toHaveBeenCalledWith("/products/1", info)
    expect(product.id).toBe("1")
  })

  test("givenProductId_whenDeleteProduct_thenDeletesProduct", async () => {
    // given
    mockedApi.delete.mockResolvedValueOnce({})

    // when
    await productService.deleteProduct("1")

    // then
    expect(mockedApi.delete).toHaveBeenCalledWith("/products/1")
  })

  test("givenProductId_whenMarkOutOfStock_thenPutsOutOfStock", async () => {
    // given
    mockedApi.put.mockResolvedValueOnce({})

    // when
    await productService.markOutOfStock("1")

    // then
    expect(mockedApi.put).toHaveBeenCalledWith("/products/1/outofstock")
  })

  test("givenProductId_whenMarkInStock_thenPutsInStock", async () => {
    // given
    mockedApi.put.mockResolvedValueOnce({})

    // when
    await productService.markInStock("1")

    // then
    expect(mockedApi.put).toHaveBeenCalledWith("/products/1/instock")
  })

  test("whenGetMetrics_thenCallsMetricsEndpointAndReturnsData", async () => {
    // given
    mockedApi.get.mockResolvedValueOnce({ data: { overallMetrics: {}, categoryMetrics: [] } })

    // when
    const metrics = await productService.getMetrics()

    // then
    expect(mockedApi.get).toHaveBeenCalledWith("/products/metrics")
    expect(metrics).toHaveProperty("overallMetrics")
    expect(metrics).toHaveProperty("categoryMetrics")
  })
})