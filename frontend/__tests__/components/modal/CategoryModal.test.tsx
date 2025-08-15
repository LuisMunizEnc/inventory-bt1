import { render, screen, fireEvent, waitFor } from "@testing-library/react"
import { CategoryModal } from "../../../components/modal/CategoryModal"
import { categoryService } from "../../../services/categoryService"

jest.spyOn(console, 'error').mockImplementation(() => {});

jest.mock("../../../services/categoryService")
const mockedService = categoryService as jest.Mocked<typeof categoryService>

describe("CategoryModal", () => {
  const onClose = jest.fn()
  const onCategoryCreated = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
  })

  test("givenIsOpen_whenRendered_thenShowsModal", () => {
    // given
    render(
      <CategoryModal isOpen={true} onClose={onClose} onCategoryCreated={onCategoryCreated} />
    )

    // then
    expect(screen.getByText("Create New Category")).toBeInTheDocument()
    expect(screen.getByPlaceholderText("Enter category name")).toBeInTheDocument()
    expect(screen.getByText("Create Category")).toBeInTheDocument()
  })

  test("givenEmptyCategoryName_whenSubmit_thenShowsValidationError", async () => {
    // given
    render(
      <CategoryModal isOpen={true} onClose={onClose} onCategoryCreated={onCategoryCreated} />
    )

    // when
    fireEvent.click(screen.getByText("Create Category"))

    // then
    expect(await screen.findByText("Category name is required")).toBeInTheDocument()
    expect(mockedService.createCategory).not.toHaveBeenCalled()
  })

  test("givenValidCategoryName_whenSubmit_thenCallsServiceAndCallback", async () => {
    // given
    mockedService.createCategory.mockResolvedValueOnce({ categoryName: "TestCat" })
    render(
      <CategoryModal isOpen={true} onClose={onClose} onCategoryCreated={onCategoryCreated} />
    )

    // when
    fireEvent.change(screen.getByPlaceholderText("Enter category name"), { target: { value: "TestCat" } })
    fireEvent.click(screen.getByText("Create Category"))

    // then
    await waitFor(() => {
      expect(mockedService.createCategory).toHaveBeenCalledWith({ categoryName: "TestCat" })
      expect(onCategoryCreated).toHaveBeenCalledWith({ categoryName: "TestCat" })
    })
  })

  test("givenService400Error_whenSubmit_thenShowsGeneralError", async () => {
    // given
    mockedService.createCategory.mockRejectedValueOnce({
      response: { status: 400, data: { message: "Category already exists" } }
    })
    render(
      <CategoryModal isOpen={true} onClose={onClose} onCategoryCreated={onCategoryCreated} />
    )

    // when
    fireEvent.change(screen.getByPlaceholderText("Enter category name"), { target: { value: "TestCat" } })
    fireEvent.click(screen.getByText("Create Category"))

    // then
    expect(await screen.findByText("Category already exists")).toBeInTheDocument()
  })

  test("givenServiceOtherError_whenSubmit_thenShowsGenericError", async () => {
    // given
    mockedService.createCategory.mockRejectedValueOnce(new Error("Network error"))
    render(
      <CategoryModal isOpen={true} onClose={onClose} onCategoryCreated={onCategoryCreated} />
    )

    // when
    fireEvent.change(screen.getByPlaceholderText("Enter category name"), { target: { value: "TestCat" } })
    fireEvent.click(screen.getByText("Create Category"))

    // then
    expect(await screen.findByText("An error occurred while creating the category. Please try again.")).toBeInTheDocument()
  })

  test("givenCancelButton_whenClicked_thenCallsOnClose", () => {
    // given
    render(
      <CategoryModal isOpen={true} onClose={onClose} onCategoryCreated={onCategoryCreated} />
    )

    // when
    fireEvent.click(screen.getByText("Cancel"))

    // then
    expect(onClose).toHaveBeenCalled()
  })
})