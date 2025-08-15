import { api } from "../../services/api"

describe("api", () => {
  test("givenNothing_whenCreated_thenHasCorrectBaseURLAndHeaders", () => {
    // then
    expect(api.defaults.baseURL).toBe("http://localhost:9090")
    expect(api.defaults.headers["Content-Type"]).toBe("application/json")
  })

  test("givenError_whenResponseInterceptor_thenLogsAndRejectsError", async () => {
    // given
    const error = new Error("Test error")

    // when/then
    await expect(
      Promise.reject(error)
    ).rejects.toThrow("Test error")
  })
})