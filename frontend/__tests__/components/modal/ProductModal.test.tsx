import { render, screen, fireEvent, waitFor } from "@testing-library/react"
import { ProductModal } from "../../../components/modal/ProductModal"
import { productService } from "../../../services/productService"
import { Provider } from "react-redux"
import { store } from "../../../store"

jest.spyOn(console, 'error').mockImplementation(() => {});

jest.mock("../../../services/productService")
const mockedService = productService as jest.Mocked<typeof productService>

const categories = [{ categoryName: "Cat1" }, { categoryName: "Cat2" }]

jest.mock("../../../hooks/redux", () => ({
    ...jest.requireActual("../../../hooks/redux"),
    useAppSelector: jest.fn(() => ({ categories })),
}))

describe("ProductModal", () => {
    const onClose = jest.fn()
    const onProductCreated = jest.fn()

    beforeEach(() => {
        jest.clearAllMocks()
    })

    function renderModal() {
        return render(
            <Provider store={store}>
                <ProductModal isOpen={true} onClose={onClose} onProductCreated={onProductCreated} />
            </Provider>
        )
    }

    test("givenIsOpen_whenRendered_thenShowsModal", () => {
        // given
        renderModal()

        // then
        expect(screen.getByText("Create New Product")).toBeInTheDocument()
        expect(screen.getByPlaceholderText("Enter product name")).toBeInTheDocument()
        expect(screen.getByText("Create Product")).toBeInTheDocument()
    })

    test("givenEmptyFields_whenSubmit_thenShowsValidationErrors", async () => {
        // given
        renderModal()

        // when
        fireEvent.click(screen.getByText("Create Product"))

        // then
        expect(await screen.findByText("Product name is required")).toBeInTheDocument()
        expect(await screen.findByText("Category is required")).toBeInTheDocument()
        expect(await screen.findByText("Stock quantity is required")).toBeInTheDocument()
        expect(await screen.findByText("Unit price is required")).toBeInTheDocument()
        expect(mockedService.createProduct).not.toHaveBeenCalled()
    })

    test("givenValidFields_whenSubmit_thenCallsServiceAndCallback", async () => {
        // given
        mockedService.createProduct.mockResolvedValueOnce({id: "1",name: "TestProduct", category: {categoryName:"Cat1"}, inStock: 10, unitPrice: 99.99, createdAt: new Date().toString(), updatedAt: new Date().toString()})
        renderModal()

        fireEvent.change(screen.getByPlaceholderText("Enter product name"), { target: { value: "TestProduct" } })
        fireEvent.click(screen.getByText("Select a category"))
        fireEvent.click(screen.getByRole("option", { name: "Cat1" }))
        fireEvent.change(screen.getByPlaceholderText("Enter stock quantity"), { target: { value: "10" } })
        fireEvent.change(screen.getByPlaceholderText("Enter unit price"), { target: { value: "99.99" } })

        // when
        fireEvent.click(screen.getByText("Create Product"))

        // then
        await waitFor(() => {
            expect(mockedService.createProduct).toHaveBeenCalledWith(
                expect.objectContaining({
                    name: "TestProduct",
                    categoryName: "Cat1",
                    inStock: 10,
                    unitPrice: 99.99,
                })
            )
            expect(onProductCreated).toHaveBeenCalled()
        })
    })

    test("givenService400Error_whenSubmit_thenShowsGeneralError", async () => {
        // given
        mockedService.createProduct.mockRejectedValueOnce({
            response: { status: 400, data: { message: "Product already exists" } }
        })
        renderModal()

        fireEvent.change(screen.getByPlaceholderText("Enter product name"), { target: { value: "TestProduct" } })
        fireEvent.click(screen.getByText("Select a category"))
        fireEvent.click(screen.getByRole("option", { name: "Cat1" }))
        fireEvent.change(screen.getByPlaceholderText("Enter stock quantity"), { target: { value: "10" } })
        fireEvent.change(screen.getByPlaceholderText("Enter unit price"), { target: { value: "99.99" } })

        // when
        fireEvent.click(screen.getByText("Create Product"))

        // then
        expect(await screen.findByText("A product with this name already exists. Please choose a different name.")).toBeInTheDocument()
    })

    test("givenServiceOtherError_whenSubmit_thenShowsGenericError", async () => {
        // given
        mockedService.createProduct.mockRejectedValueOnce(new Error("Network error"))
        renderModal()

        fireEvent.change(screen.getByPlaceholderText("Enter product name"), { target: { value: "TestProduct" } })
        fireEvent.click(screen.getByText("Select a category"))
        fireEvent.click(screen.getByRole("option", { name: "Cat1" }))
        fireEvent.change(screen.getByPlaceholderText("Enter stock quantity"), { target: { value: "10" } })
        fireEvent.change(screen.getByPlaceholderText("Enter unit price"), { target: { value: "99.99" } })

        // when
        fireEvent.click(screen.getByText("Create Product"))

        // then
        expect(await screen.findByText("An error occurred while creating the product. Please try again.")).toBeInTheDocument()
    })

    test("givenCancelButton_whenClicked_thenCallsOnClose", () => {
        // given
        renderModal()

        // when
        fireEvent.click(screen.getByText("Cancel"))

        // then
        expect(onClose).toHaveBeenCalled()
    })
})