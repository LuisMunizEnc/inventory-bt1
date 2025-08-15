import { render, screen, waitFor, fireEvent, cleanup } from "@testing-library/react";
import { InventoryMetrics } from "../../components/InventoryMetrics";
import { productService } from "../../services/productService";
import React from "react";
import type { InventoryMetrics as TInventoryMetrics } from "../../types";

jest.mock("../../services/productService");
const mockedService = productService as jest.Mocked<typeof productService>;

const mockMetrics: TInventoryMetrics = {
    categoryMetrics: [
        { categoryName: "Electronics", totalProductsInStock: 50, totalValueInStock: 25000, averagePriceInStock: 500 },
        { categoryName: "Books", totalProductsInStock: 120, totalValueInStock: 1200, averagePriceInStock: 10 },
        { categoryName: "Clothing", totalProductsInStock: 300, totalValueInStock: 3000, averagePriceInStock: 10 },
    ],
    overallMetrics: {
        totalProductsInStock: 470,
        totalValueInStock: 29200,
        averagePriceInStock: 62.13,
    },
};

describe("InventoryMetrics", () => {
    let consoleErrorSpy: jest.SpyInstance;

    beforeEach(() => {
        consoleErrorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        jest.clearAllMocks();
    });

    afterEach(cleanup)

    test("givenComponentIsRendered_whenFetchingData_thenDisplays", async () => {
        // given
        mockedService.getMetrics.mockReturnValue(new Promise(() => {}));

        // when
        render(<InventoryMetrics />);

        // then
        expect(screen.getByText("Inventory Metrics")).toBeInTheDocument();
        expect(screen.queryByText("Failed to load inventory metrics.")).not.toBeInTheDocument();
    });

    test("givenComponentIsRendered_whenDataIsFetchedSuccessfully_thenDisplaysMetrics", async () => {
        // given
        mockedService.getMetrics.mockResolvedValue(mockMetrics);

        // when
        render(<InventoryMetrics />);

        // then
        await waitFor(() => {
            expect(screen.getByText("Total Products in Stock")).toBeInTheDocument();
        });

        expect(screen.getByText("Total Products in Stock")).toBeInTheDocument();
        expect(screen.getByText("Total Value in Stock")).toBeInTheDocument();
        expect(screen.getByText("Average Price in Stock")).toBeInTheDocument();

        const sortedCategoryMetrics = [...mockMetrics.categoryMetrics].sort((a, b) => b.totalValueInStock - a.totalValueInStock);
        expect(screen.getByRole("columnheader", { name: "Category" })).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "Products in Stock" })).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "Total Value" })).toBeInTheDocument();

        expect(screen.getByRole("row", { name: /Electronics/i })).toBeInTheDocument();
        expect(screen.getByText(sortedCategoryMetrics[0].categoryName)).toBeInTheDocument();
        expect(screen.getByText("50")).toBeInTheDocument();
        expect(screen.getByText("$25,000.00")).toBeInTheDocument();

        expect(screen.getByRole("row", { name: /Overall Total/i })).toBeInTheDocument();
    });

    test("givenComponentIsRendered_whenDataFetchingFails_thenDisplaysErrorState", async () => {
        // given
        mockedService.getMetrics.mockRejectedValue(new Error("Network Error"));

        // when
        render(<InventoryMetrics />);

        // then
        await waitFor(() => {
            expect(screen.getByText("Failed to load inventory metrics. Please try again.")).toBeInTheDocument();
        });
        expect(screen.getByRole("button", { name: "Try Again" })).toBeInTheDocument();
        expect(screen.queryByText("Total Products in Stock")).not.toBeInTheDocument();
    });

    test("givenComponentIsRenderedWithMetrics_whenRefreshIsClicked_thenFetchesDataAgain", async () => {
        // given
        mockedService.getMetrics.mockResolvedValue(mockMetrics);
        render(<InventoryMetrics />);
        await waitFor(() => {
            expect(screen.getByText("Category")).toBeInTheDocument();
        });

        // when
        fireEvent.click(screen.getByRole("button", { name: "Refresh" }));

        // then
        await waitFor(() => {
            expect(mockedService.getMetrics).toHaveBeenCalledTimes(2);
        });
    });

    test("givenComponentIsRenderedWithError_whenTryAgainIsClicked_thenFetchesDataAgain", async () => {
        // given
        mockedService.getMetrics.mockRejectedValueOnce(new Error("Network Error"));
        render(<InventoryMetrics />);
        await waitFor(() => {
            expect(screen.getByText("Failed to load inventory metrics. Please try again.")).toBeInTheDocument();
        });

        mockedService.getMetrics.mockResolvedValueOnce(mockMetrics);

        // when
        fireEvent.click(screen.getByRole("button", { name: "Try Again" }));

        // then
        await waitFor(() => {
            expect(screen.getByText("Refresh")).toBeInTheDocument();
        });
        expect(mockedService.getMetrics).toHaveBeenCalledTimes(2);
    });
});
