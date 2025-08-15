import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { DeleteProductModal } from "../../../components/modal/DeleteProductModal";
import { productService } from "../../../services/productService";
import React from "react";

jest.spyOn(console, 'error').mockImplementation(() => {});

jest.mock("../../../services/productService");
const mockedService = productService as jest.Mocked<typeof productService>;

const mockProduct = {
    id: "prod-1",
    name: "Test Product",
    category: { categoryName: "Cat1" },
    inStock: 10,
    unitPrice: 50.0,
    createdAt: "2023-01-01T00:00:00Z",
    updatedAt: "2023-01-01T00:00:00Z",
};

describe("DeleteProductModal", () => {
    const onClose = jest.fn();
    const onProductDeleted = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
    });


    function renderModal() {
        return render(
            <DeleteProductModal
                isOpen={true}
                product={mockProduct}
                onClose={onClose}
                onProductDeleted={onProductDeleted}
            />
        );
    }

    test("givenModalIsOpen_whenRendered_thenDisplaysProductDetailsAndButtons", () => {
        // given
        renderModal();

        // when 

        // then
        expect(screen.getByRole("heading", { name: /Delete Product/i })).toBeInTheDocument();
        expect(screen.getByText("This action cannot be undone. This will permanently delete the product from your inventory.")).toBeInTheDocument();
        expect(screen.getByText(mockProduct.name)).toBeInTheDocument();
        expect(screen.getByText(mockProduct.category.categoryName)).toBeInTheDocument();
        expect(screen.getByText(`${mockProduct.inStock} units`)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Cancel" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Delete Product" })).toBeInTheDocument();
    });

    test("givenDeleteButtonIsClicked_whenServiceSucceeds_thenCallsServiceAndCallbacks", async () => {
        // given
        mockedService.deleteProduct.mockResolvedValue(Promise.resolve());
        renderModal();

        // when
        fireEvent.click(screen.getByRole("button", { name: "Delete Product" }));

        // then
        await waitFor(() => {
            expect(mockedService.deleteProduct).toHaveBeenCalledWith(mockProduct.id);
        });
        expect(onProductDeleted).toHaveBeenCalled();
    });

    test("givenDeleteButtonIsClicked_whenServiceReturns404_thenShowsNotFoundError", async () => {
        // given
        mockedService.deleteProduct.mockRejectedValueOnce({
            response: { status: 404, data: { message: "Product not found" } }
        });
        renderModal();

        // when
        fireEvent.click(screen.getByRole("button", { name: "Delete Product" }));

        // then
        await waitFor(() => {
            expect(screen.getByText("Product not found. It may have already been deleted.")).toBeInTheDocument();
            expect(onProductDeleted).not.toHaveBeenCalled();
            expect(onClose).not.toHaveBeenCalled();
        });
    });

    test("givenDeleteButtonIsClicked_whenServiceFails_thenShowsGenericError", async () => {
        // given
        mockedService.deleteProduct.mockRejectedValueOnce(new Error("Network error"));
        renderModal();

        // when
        fireEvent.click(screen.getByRole("button", { name: "Delete Product" }));

        // then
        await waitFor(() => {
            expect(screen.getByText("An error occurred while deleting the product. Please try again.")).toBeInTheDocument();
            expect(onProductDeleted).not.toHaveBeenCalled();
            expect(onClose).not.toHaveBeenCalled();
        });
    });

    test("givenCancelButtonIsClicked_whenInteracted_thenCallsOnCloseAndDoesNotDelete", () => {
        // given
        renderModal();

        // when
        fireEvent.click(screen.getByRole("button", { name: "Cancel" }));

        // then
        expect(onClose).toHaveBeenCalled();
        expect(mockedService.deleteProduct).not.toHaveBeenCalled();
    });

    test("givenDeleting_whenLoading_thenButtonsAreDisabled", () => {
        // given
        mockedService.deleteProduct.mockResolvedValue(new Promise(() => {}));
        renderModal();

        // when
        fireEvent.click(screen.getByRole("button", { name: "Delete Product" }));

        // then
        expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
        expect(screen.getByRole("button", { name: /Deleting.../i })).toBeDisabled();
    });
});
