import { render, screen, fireEvent, waitFor, cleanup } from "@testing-library/react";
import { ProductFilter } from "../../components/ProductFilter";
import { useAppDispatch, useAppSelector } from "../../hooks/redux";
import { setFilters, fetchProducts } from "../../store/slices/productsSlice";
import { fetchCategories } from "../../store/slices/categoriesSlice";
import React from "react";
import type { ProductFilters } from "../../types";

jest.mock("../../hooks/redux");
jest.mock("../../store/slices/productsSlice", () => ({
    ...jest.requireActual("../../store/slices/productsSlice"),
    setFilters: jest.fn(),
    fetchProducts: jest.fn(),
}));
jest.mock("../../store/slices/categoriesSlice", () => ({
    ...jest.requireActual("../../store/slices/categoriesSlice"),
    fetchCategories: jest.fn(),
}));

const mockUseAppDispatch = useAppDispatch as jest.Mock;
const mockUseAppSelector = useAppSelector as jest.Mock;
const mockSetFilters = setFilters as unknown as jest.Mock;
const mockFetchProducts = fetchProducts as unknown as jest.Mock;
const mockFetchCategories = fetchCategories as unknown as jest.Mock;

describe("ProductFilter", () => {
    const mockState = {
        products: { filters: { name: "", categories: [], inStock: null } },
        categories: { categories: [{ categoryName: "Cat1" }, { categoryName: "Cat2" }] },
    };

    const mockDispatch = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
        mockUseAppDispatch.mockReturnValue(mockDispatch);
        mockUseAppSelector.mockImplementation((selector) => selector(mockState));
    });

    afterEach(cleanup);

    const renderFilter = () => render(<ProductFilter />);

    test("givenComponentIsRendered_whenInitialState_thenDisplaysCorrectUI", () => {
        // given
        renderFilter();

        // then
        expect(screen.getByPlaceholderText("Search by name...")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Search" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Clear Filters" })).toBeInTheDocument();
        expect(screen.getByText("Select categories")).toBeInTheDocument();
        expect(screen.getByText("Availability")).toBeInTheDocument();
        expect(mockDispatch).toHaveBeenCalledWith(mockFetchCategories());
        expect(mockDispatch).toHaveBeenCalledWith(mockFetchProducts(mockState.products.filters));
    });


    test("givenFiltersAreSet_whenClearFiltersIsClicked_thenResetsAndDispatches", async () => {
        // given
        mockUseAppSelector.mockImplementation((selector) =>
            selector({
                ...mockState,
                products: { filters: { name: "Test", categories: ["Cat1"], inStock: true } },
            })
        );
        renderFilter();
        await waitFor(() => {
            expect(screen.getByPlaceholderText("Search by name...")).toHaveValue("Test");
            expect(screen.getByText("Cat1")).toBeInTheDocument();
        });

        // when
        fireEvent.click(screen.getByRole("button", { name: "Clear Filters" }));

        // then
        const clearedFilters: ProductFilters = { name: "", categories: [], inStock: null };
        expect(mockDispatch).toHaveBeenCalledWith(mockSetFilters(clearedFilters));
        expect(mockDispatch).toHaveBeenCalledWith(mockFetchProducts(clearedFilters));
    });

    test("givenNameIsTyped_whenSearchIsClicked_thenDispatchesWithCorrectFilter", () => {
        // given
        renderFilter();
        const nameInput = screen.getByPlaceholderText("Search by name...");
        const newName = "Test Product";
        fireEvent.change(nameInput, { target: { value: newName } });

        // when
        fireEvent.click(screen.getByRole("button", { name: "Search" }));

        // then
        const expectedFilters: ProductFilters = { ...mockState.products.filters, name: newName };
        expect(mockDispatch).toHaveBeenCalledWith(mockSetFilters(expectedFilters));
        expect(mockDispatch).toHaveBeenCalledWith(mockFetchProducts(expectedFilters));
    });
});
