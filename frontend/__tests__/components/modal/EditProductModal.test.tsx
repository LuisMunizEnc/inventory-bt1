import { render, screen, fireEvent, waitFor } from "@testing-library/react"
import { EditProductModal } from "../../../components/modal/EditProductModal"
import { productService } from "../../../services/productService"
import { Provider } from "react-redux"
import { store } from "../../../store"
import React from "react"
import { format } from "date-fns"

jest.spyOn(console, 'error').mockImplementation(() => {});

jest.mock("../../../services/productService")
const mockedService = productService as jest.Mocked<typeof productService>

const categories = [{ categoryName: "Cat1" }, { categoryName: "Cat2" }]
const mockProduct = {
    id: "prod-1",
    name: "Original Product",
    category: { categoryName: "Cat1" },
    inStock: 10,
    unitPrice: 50.0,
    createdAt: "2023-01-01T00:00:00Z",
    updatedAt: "2023-01-01T00:00:00Z",
    expirationDate: "2024-12-31T00:00:00Z",
}

jest.mock("../../../hooks/redux", () => ({
    ...jest.requireActual("../../../hooks/redux"),
    useAppSelector: jest.fn(() => ({ categories })),
    useAppDispatch: jest.fn(() => jest.fn()),
}))

describe("EditProductModal", () => {
    const onClose = jest.fn()
    const onProductUpdated = jest.fn()
    const productId = mockProduct.id

    beforeEach(() => {
        jest.clearAllMocks()
        mockedService.getProductById.mockResolvedValue(mockProduct)
        mockedService.updateProduct.mockResolvedValue({} as any)
    })

    function renderModal() {
        return render(
            <Provider store={store}>
                <EditProductModal
                    isOpen={true}
                    productId={productId}
                    onClose={onClose}
                    onProductUpdated={onProductUpdated}
                />
            </Provider>
        )
    }

    test("givenModalIsOpen_whenRendered_thenDisplaysCorrectInfoAndPopulatesFields", async () => {
        // given
        renderModal()

        // when 

        // then
        await waitFor(() => {
            expect(screen.getByRole("heading", { name: "Edit Product" })).toBeInTheDocument()
            expect(screen.getByText(`Update details for "Original Product"`)).toBeInTheDocument()

            expect(screen.getByPlaceholderText("Enter product name")).toHaveValue(mockProduct.name)
            expect(screen.getByDisplayValue(mockProduct.category.categoryName)).toBeInTheDocument()
            expect(screen.getByPlaceholderText("Enter stock quantity")).toHaveValue(mockProduct.inStock)
            expect(screen.getByPlaceholderText("Enter unit price")).toHaveValue(mockProduct.unitPrice)
            expect(screen.getByRole("button", { name: format(new Date(mockProduct.expirationDate), "PPP") })).toBeInTheDocument()
        })
        expect(mockedService.getProductById).toHaveBeenCalledWith(productId)
    })

    test("givenEmptyFields_whenSubmit_thenShowsValidationErrors", async () => {
        // given
        const emptyProduct = { ...mockProduct, name: "", inStock: 0, unitPrice: 0.0, category: { categoryName: "" } };
        mockedService.getProductById.mockResolvedValue(emptyProduct);
        renderModal();

        await waitFor(() => {
            expect(screen.getByText("Product Name")).toBeInTheDocument();
        });

        // when
        fireEvent.click(screen.getByRole("button", { name: "Update Product" }));

        // then
        expect(await screen.findByText("Product name is required")).toBeInTheDocument();
        expect(await screen.findByText("Category is required")).toBeInTheDocument();
        expect(mockedService.updateProduct).not.toHaveBeenCalled();
    });

    test("givenService404Error_whenSubmit_thenShowsNotFoundError", async () => {
        // given
        mockedService.updateProduct.mockRejectedValueOnce({
            response: { status: 404, data: { message: "Product not found" } }
        })
        renderModal()
        await waitFor(() => expect(screen.getByText("Product Name")).toBeInTheDocument())
        fireEvent.change(screen.getByPlaceholderText("Enter product name"), { target: { value: "Updated Product" } })

        // when
        fireEvent.click(screen.getByRole("button", { name: "Update Product" }))

        // then
        expect(await screen.findByText("Product not found. It may have been deleted.")).toBeInTheDocument()
    })

    test("givenServiceOtherError_whenSubmit_thenShowsGenericError", async () => {
        // given
        mockedService.updateProduct.mockRejectedValueOnce(new Error("Network error"))
        renderModal()
        await waitFor(() => expect(screen.getByText("Product Name")).toBeInTheDocument())
        fireEvent.change(screen.getByPlaceholderText("Enter product name"), { target: { value: "Updated Product" } })

        // when
        fireEvent.click(screen.getByRole("button", { name: "Update Product" }))

        // then
        expect(await screen.findByText("An error occurred while updating the product. Please try again.")).toBeInTheDocument()
    })

    test("givenCancelButton_whenClicked_thenCallsOnClose", async () => {
        // given
        renderModal()
        await waitFor(() => expect(screen.getByText("Product Name")).toBeInTheDocument())

        // when
        fireEvent.click(screen.getByRole("button", { name: "Cancel" }))

        // then
        expect(onClose).toHaveBeenCalled()
    })
})
