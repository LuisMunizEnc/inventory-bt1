import axios from "axios"

// Mock axios
jest.mock("axios")
const mockedAxios = axios as jest.Mocked<typeof axios>

describe("API tests", () => {
  beforeEach(() => {
    jest.resetAllMocks()
  })

  it("should mock API calls", async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: [{ id: "1", name: "Test Product", category: { categoryName: "Electronics" }, unitPrice: 100, inStock: 10 }],
    })

    const response = await axios.get("/api/products")

    expect(response.data).toHaveLength(1)
    expect(response.data[0].name).toBe("Test Product")

    expect(mockedAxios.get).toHaveBeenCalledWith("/api/products")
  })
})
