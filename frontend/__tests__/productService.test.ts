import { productService } from "../services/productService"
import { api } from "../services/api"

// Mock the api module
jest.mock("../services/api")
const mockedApi = api as jest.Mocked<typeof api>

describe("Product Service", () => {
  beforeEach(() => {
    jest.resetAllMocks()
  })

  it("should get all products", async () => {
    mockedApi.get.mockResolvedValueOnce({
      data: [{ id: "1", name: "Test Product", category: { categoryName: "Electronics" }, unitPrice: 100, inStock: 10 }],
    })

    const products = await productService.getAllProducts()

    expect(products).toHaveLength(1)
    expect(products[0].name).toBe("Test Product")

    expect(mockedApi.get).toHaveBeenCalledWith("/products?")
  })

  it("should get products with filters", async () => {
    mockedApi.get.mockResolvedValueOnce({
      data: [{ id: "1", name: "Test Product", category: { categoryName: "Electronics" }, unitPrice: 100, inStock: 10 }],
    })

    const filters = { name: "Test", categories: ["Electronics"], inStock: true }
    const products = await productService.getAllProducts(filters)

    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("name=Test"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("categories=Electronics"))
    expect(mockedApi.get).toHaveBeenCalledWith(expect.stringContaining("inStock=true"))
  })
})
