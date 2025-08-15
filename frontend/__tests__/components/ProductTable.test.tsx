import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { ProductTable } from "../../components/ProductTable";
import { useAppDispatch, useAppSelector } from "../../hooks/redux";
import { fetchProducts } from "../../store/slices/productsSlice";
import { fetchCategories } from "../../store/slices/categoriesSlice";
import { productService } from "../../services/productService";
import React from "react";
import type { Product, ProductPage } from "../../types";
import { categoryService } from "@/services/categoryService";

jest.mock("../../hooks/redux");
jest.mock("../../store/slices/productsSlice", () => ({
    ...jest.requireActual("../../store/slices/productsSlice"),
    fetchProducts: jest.fn(),
}));
jest.mock("../../store/slices/categoriesSlice", () => ({
    ...jest.requireActual("../../store/slices/categoriesSlice"),
    fetchCategories: jest.fn(),
}));
jest.mock("../../services/productService");
const mockedService = productService as jest.Mocked<typeof productService>;
jest.mock("../../services/categoryService");
const mockedCategoryService = categoryService as jest.Mocked<typeof categoryService>;

jest.mock("../../components/modal/ProductModal", () => ({
    ProductModal: ({ isOpen, onClose, onProductCreated }: any) => isOpen ? (
        <div data-testid="product-modal">
            <button onClick={onClose}>Close Product</button>
            <button onClick={onProductCreated}>Create Product</button>
        </div>
    ) : null,
}));
jest.mock("../../components/modal/CategoryModal", () => ({
    CategoryModal: ({ isOpen, onClose, onCategoryCreated }: any) => isOpen ? (
        <div data-testid="category-modal">
            <button onClick={onClose}>Close Category</button>
            <button onClick={onCategoryCreated}>Create Category</button>
        </div>
    ) : null,
}));
jest.mock("../../components/modal/EditProductModal", () => ({
    EditProductModal: ({ isOpen, onClose, onProductUpdated }: any) => isOpen ? (
        <div data-testid="edit-modal">
            <button onClick={onClose}>Close Edit</button>
            <button onClick={onProductUpdated}>Update Product</button>
        </div>
    ) : null,
}));
jest.mock("../../components/modal/DeleteProductModal", () => ({
    DeleteProductModal: ({ isOpen, onClose, onProductDeleted }: any) => isOpen ? (
        <div data-testid="delete-modal">
            <button onClick={onClose}>Close Delete</button>
            <button onClick={onProductDeleted}>Delete Product</button>
        </div>
    ) : null,
}));

const mockProducts: Product[] = [
    { id: "1", name: "Product A", category: { categoryName: "Cat1" }, unitPrice: 100, inStock: 0, createdAt: "2023-01-01", updatedAt: "2023-01-01", expirationDate: "2023-08-20T00:00:00.000Z" },
    { id: "2", name: "Product B", category: { categoryName: "Cat2" }, unitPrice: 50, inStock: 5, createdAt: "2023-01-01", updatedAt: "2023-01-01", expirationDate: "2023-08-25T00:00:00.000Z" },
    { id: "3", name: "Product C", category: { categoryName: "Cat1" }, unitPrice: 200, inStock: 15, createdAt: "2023-01-01", updatedAt: "2023-01-01", expirationDate: "2023-09-01T00:00:00.000Z" },
];

const mockProductPage: ProductPage = {
    content: mockProducts,
    page: { totalElements: 3, totalPages: 1 },
};

const mockUseAppDispatch = useAppDispatch as jest.Mock;
const mockUseAppSelector = useAppSelector as jest.Mock;

describe("ProductTable", () => {
    let consoleErrorSpy: jest.SpyInstance;
    const mockDispatch = jest.fn();

    beforeEach(() => {
        consoleErrorSpy = jest.spyOn(console, "error").mockImplementation(() => { });
        jest.clearAllMocks();
        mockUseAppDispatch.mockReturnValue(mockDispatch);
        jest.useFakeTimers().setSystemTime(new Date("2023-08-16T10:00:00.000Z"));
    });

    afterEach(() => {
        consoleErrorSpy.mockRestore();
        jest.useRealTimers();
    });

    test("givenLoadingIsTrue_whenRendered_thenDisplaysLoadingSkeletons", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [],
            totalElements: 0,
            loading: true,
            error: null,
        });

        // when
        render(<ProductTable />);

        // then
        expect(screen.getAllByLabelText("skeleton")).toHaveLength(5);
    });

    test("givenErrorIsPresent_whenRendered_thenDisplaysErrorState", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [],
            totalElements: 0,
            loading: false,
            error: "Network error",
        });

        // when
        render(<ProductTable />);

        // then
        expect(screen.getByText("Error loading products: Network error")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Try Again" })).toBeInTheDocument();
    });

    test("givenProductsArrayIsEmpty_whenRendered_thenDisplaysNoProductsMessage", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [],
            totalElements: 0,
            loading: false,
            error: null,
        });

        // when
        render(<ProductTable />);

        // then
        expect(screen.getByText("No products found")).toBeInTheDocument();
        expect(screen.getByText("Try adjusting your filters or add a new product")).toBeInTheDocument();
    });

    test("givenProductsArePresent_whenRendered_thenDisplaysProductsAndActions", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: mockProductPage.page.totalElements,
            loading: false,
            error: null,
        });

        // when
        render(<ProductTable />);

        // then
        expect(screen.getByText("Products (3)")).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "Name" })).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "Category" })).toBeInTheDocument();
        expect(screen.getByText("Product A")).toBeInTheDocument();
        expect(screen.getByText("Product B")).toBeInTheDocument();
        expect(screen.getByText("Product C")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "New Product" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "New Category" })).toBeInTheDocument();
    });

    test("givenPagination_whenNextPageIsClicked_thenDispatchesWithCorrectPage", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: 30,
            loading: false,
            error: null,
        });

        // when
        render(<ProductTable />);
        fireEvent.click(screen.getByRole("button", { name: "next-page" }));

        // then
        expect(mockDispatch).toHaveBeenCalledWith(fetchProducts(expect.objectContaining({ page: 1 })));
    });

    test("givenSortableHeaderIsClicked_whenInteracted_thenDispatchesWithCorrectSortConfig", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: 3,
            loading: false,
            error: null,
        });

        // when
        render(<ProductTable />);
        fireEvent.click(screen.getByLabelText("next-page"));

        // then
        expect(mockDispatch).toHaveBeenCalledWith(fetchProducts(expect.objectContaining({ sort: "name,desc" })));
    });

    test("givenSortableHeaderIsClickedTwice_thenSortDirectionChangesFromAscToDesc", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: 3,
            loading: false,
            error: null,
        });

        // when
        render(<ProductTable />);
        const nameHeader = screen.getByRole("button", { name: /Name/i });
        fireEvent.click(nameHeader);
        fireEvent.click(nameHeader);

        // then
        expect(mockDispatch).toHaveBeenLastCalledWith(fetchProducts(expect.objectContaining({ sort: "name,asc" })));
        expect(mockDispatch).toHaveBeenCalledWith(fetchProducts(expect.objectContaining({ sort: "name,desc" })));
    });

    test("givenProductIsInStock_whenToggleStockIsClicked_thenMarksOutOfStockAndRefreshes", async () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [mockProducts[1]],
            totalElements: 1,
            loading: false,
            error: null,
        });
        mockedService.markOutOfStock.mockResolvedValueOnce(Promise.resolve());
        render(<ProductTable />);

        // when
        fireEvent.click(screen.getByRole("button", { name: "Empty stock" }));

        // then
        await waitFor(() => {
            expect(mockedService.markOutOfStock).toHaveBeenCalledWith("2");
            expect(mockDispatch).toHaveBeenCalledWith(fetchProducts());
        });
    });

    test("givenProductIsInStock_whenRendered_thenTextStyleIsCorrect", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [mockProducts[0], mockProducts[1], mockProducts[2]],
            totalElements: 3,
            loading: false,
            error: null,
        });
    
        // when
        render(<ProductTable />);
        
        // then
        expect(screen.getByText("Product A")).toHaveClass("line-through");
        expect(screen.getByText("$100.00")).toHaveClass("line-through");
        expect(screen.getByText("Product B")).not.toHaveClass("line-through");
        expect(screen.getByText("$50.00")).not.toHaveClass("line-through");
    });

    test("givenProductIsOutOfStock_whenToggleStockIsClicked_thenMarksInStockAndRefreshes", async () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [mockProducts[0]],
            totalElements: 1,
            loading: false,
            error: null,
        });
        mockedService.markInStock.mockResolvedValueOnce(Promise.resolve());
        render(<ProductTable />);

        // when
        fireEvent.click(screen.getByRole("button", { name: "Fill stock" }));

        // then
        await waitFor(() => {
            expect(mockedService.markInStock).toHaveBeenCalledWith("1");
            expect(mockDispatch).toHaveBeenCalledWith(fetchProducts());
        });
    });

    test("givenToggleStockFails_thenLogsErrorAndButtonIsReenabled", async () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [mockProducts[1]],
            totalElements: 1,
            loading: false,
            error: null,
        });
        mockedService.markOutOfStock.mockRejectedValue(new Error("API Error"));
        render(<ProductTable />);

        // when
        fireEvent.click(screen.getByRole("button", { name: "Empty stock" }));

        // then
        await waitFor(() => {
            expect(screen.getByRole("button", { name: "Empty stock" })).not.toBeDisabled();
            expect(consoleErrorSpy).toHaveBeenCalledWith("Error toggling stock status:", expect.any(Error));
        });
    });

    test("givenProductIsExpiringSoon_whenRendered_thenRowHasCorrectBackgroundColor", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: [mockProducts[0], mockProducts[1], mockProducts[2]],
            totalElements: 3,
            loading: false,
            error: null,
        });

        // when
        render(<ProductTable />);
        const rows = screen.getAllByRole("row");

        // then
        expect(rows[1]).toHaveClass("bg-red-100");
        expect(rows[2]).toHaveClass("bg-yellow-100");
        expect(rows[3]).toHaveClass("bg-green-100");
    });

    test("givenNewProductButtonIsClicked_thenProductModalOpens", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: 3,
            loading: false,
            error: null,
        });
        render(<ProductTable />);

        // when
        fireEvent.click(screen.getByRole("button", { name: "New Product" }));

        // then
        expect(screen.getByTestId("product-modal")).toBeInTheDocument();
    });

    test("givenNewCategoryButtonIsClicked_thenCategoryModalOpens", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: 3,
            loading: false,
            error: null,
        });
        render(<ProductTable />);

        // when
        fireEvent.click(screen.getByRole("button", { name: "New Category" }));

        // then
        expect(screen.getByTestId("category-modal")).toBeInTheDocument();
    });

    test("givenEditButtonIsClicked_thenEditModalOpens", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: 3,
            loading: false,
            error: null,
        });
        render(<ProductTable />);

        // when
        fireEvent.click(screen.getAllByTitle("Edit Product")[0]);

        // then
        expect(screen.getByTestId("edit-modal")).toBeInTheDocument();
    });

    test("givenDeleteButtonIsClicked_thenDeleteModalOpens", () => {
        // given
        mockUseAppSelector.mockReturnValue({
            products: mockProducts,
            totalElements: 3,
            loading: false,
            error: null,
        });
        render(<ProductTable />);

        // when
        fireEvent.click(screen.getAllByTitle("Delete Product")[0]);

        // then
        expect(screen.getByTestId("delete-modal")).toBeInTheDocument();
    });
});
