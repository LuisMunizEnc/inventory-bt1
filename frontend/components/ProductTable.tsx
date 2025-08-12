"use client"

import { useEffect, useState } from "react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Package, Edit, Plus, Trash, ArrowUpDown, ArrowUp, ArrowDown,
  ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight
} from "lucide-react"
import { useAppDispatch, useAppSelector } from "../hooks/redux"
import { fetchProducts } from "../store/slices/productsSlice"
import { fetchCategories } from "../store/slices/categoriesSlice"
import type { Product, Category, SortConfig } from "../types"
import { productService } from "../services/productService"
import { ProductModal } from "./modal/ProductModal"
import { CategoryModal } from "./modal/CategoryModal"
import { DeleteProductModal } from "./modal/DeleteProductModal"
import { EditProductModal } from "./modal/EditProductModal"
import { useSorting } from "../hooks/useSorting"

const ITEMS_PER_PAGE = 10

export function ProductTable() {
  const dispatch = useAppDispatch()
  const { products, loading, error } = useAppSelector((state) => state.products)
  const [loadingProductId, setLoadingProductId] = useState<string | null>(null)
  const { sortedData, sortConfig, handleSort, clearSort } = useSorting(products)
  const [currentPage, setCurrentPage] = useState(1)

  const [isProductModalOpen, setIsProductModalOpen] = useState(false)
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false)

  const [selectedProductId, setSelectedProductId] = useState<string | null>(null)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)

  // Pagination
  const totalPages = Math.ceil(sortedData.length / ITEMS_PER_PAGE)
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE
  const endIndex = startIndex + ITEMS_PER_PAGE
  const currentData = sortedData.slice(startIndex, endIndex)

  useEffect(() => {
    setCurrentPage(1)
  }, [products.length])

  useEffect(() => {
    dispatch(fetchProducts())
  }, [dispatch])

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "numeric",
      day: "numeric",
    })
  }

  const getStockStatus = (quantity: number) => {
    if (quantity < 5) {
      return <Badge variant="destructive">{quantity}</Badge>
    } else if (quantity <= 10 && quantity >= 5) {
      return <Badge variant="secondary">{quantity}</Badge>
    } else {
      return <Badge variant="default">{quantity}</Badge>
    }
  }

  const getSortIcon = (field: SortConfig["field"]) => {
    if (!sortConfig || sortConfig.field !== field) {
      return <ArrowUpDown className="h-4 w-4 text-gray-400" />
    }

    return sortConfig.direction === "asc" ? (
      <ArrowUp className="h-4 w-4 text-blue-600" />
    ) : (
      <ArrowDown className="h-4 w-4 text-blue-600" />
    )
  }

  const SortableHeader = ({
    field,
    children,
  }: {
    field: SortConfig["field"]
    children: React.ReactNode
  }) => (
    <TableHead>
      <Button
        variant="ghost"
        className="h-auto p-0 font-semibold hover:bg-transparent"
        onClick={() => handleSort(field)}
      >
        <div className="flex items-center gap-2">
          {children}
          {getSortIcon(field)}
        </div>
      </Button>
    </TableHead>
  )

  const getRowBackgroundColor = (expirationDate?: string) => {
    if (!expirationDate) return ""

    const today = new Date()
    const expDate = new Date(expirationDate)
    const diffTime = expDate.getTime() - today.getTime()
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

    if (diffDays < 7) {
      return "bg-red-100 hover:bg-red-200"
    } else if (diffDays >= 7 && diffDays <= 14) {
      return "bg-yellow-100 hover:bg-yellow-200"
    } else {
      return "bg-green-100 hover:bg-green-200"
    }
  }

  const getTextStyle = (inStock: number) => {
    return inStock === 0 ? "line-through text-gray-500" : ""
  }

  const handleToggleStock = async (product: Product) => {
    try {
      setLoadingProductId(product.id)

      if (product.inStock > 0) {
        await productService.markOutOfStock(product.id)
      } else {
        await productService.markInStock(product.id)
      }

      dispatch(fetchProducts())
    } catch (error) {
      console.error("Error toggling stock status:", error)
    } finally {
      setLoadingProductId(null)
    }
  }

  const handleEditProduct = (product: Product) => {
    setSelectedProductId(product.id)
    setIsEditModalOpen(true)
  }

  const handleDeleteProduct = (product: Product) => {
    setSelectedProduct(product)
    setIsDeleteModalOpen(true)
  }

  const goToPage = (page: number) => {
    setCurrentPage(page)
  }

  const goToFirstPage = () => {
    setCurrentPage(1)
  }

  const goToLastPage = () => {
    setCurrentPage(totalPages)
  }

  const goToPreviousPage = () => {
    setCurrentPage((prev) => Math.max(prev - 1, 1))
  }

  const goToNextPage = () => {
    setCurrentPage((prev) => Math.min(prev + 1, totalPages))
  }

  const getPageNumbers = () => {
    const pageNumbers = []
    const maxPagesToShow = 5

    let startPage = Math.max(1, currentPage - Math.floor(maxPagesToShow / 2))
    let endPage = startPage + maxPagesToShow - 1

    if (endPage > totalPages) {
      endPage = totalPages
      startPage = Math.max(1, endPage - maxPagesToShow + 1)
    }

    for (let i = startPage; i <= endPage; i++) {
      pageNumbers.push(i)
    }

    return pageNumbers
  }

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Products</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Products</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8">
            <Package className="h-12 w-12 mx-auto text-gray-400 mb-4" />
            <p className="text-gray-500">Error loading products: {error}</p>
            <Button variant="outline" onClick={() => dispatch(fetchProducts())} className="mt-4">
              Try Again
            </Button>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Products ({sortedData.length})</span>
          <div className="gap-2 flex items-center">
            <Button variant="outline" onClick={() => setIsCategoryModalOpen(true)}>
              <Plus className="h-4 w-4 mr-2" />
              New Category
            </Button>
            <Button onClick={() => setIsProductModalOpen(true)}>
              <Package className="h-4 w-4 mr-2" />
              New Product
            </Button>
          </div>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {sortedData.length === 0 ? (
          <div className="text-center py-8">
            <Package className="h-12 w-12 mx-auto text-gray-400 mb-4" />
            <p className="text-gray-500">No products found</p>
            <p className="text-sm text-gray-400">Try adjusting your filters or add a new product</p>
          </div>
        ) : (
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Stock Action</TableHead>
                  <SortableHeader field="name">Name</SortableHeader>
                  <SortableHeader field="category">Category</SortableHeader>
                  <SortableHeader field="unitPrice">Unit Price</SortableHeader>
                  <SortableHeader field="inStock">Stock</SortableHeader>
                  <SortableHeader field="expirationDate">Expiration</SortableHeader>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {currentData.map((product: Product) => (
                  <TableRow key={product.id} className={getRowBackgroundColor(product.expirationDate)}>
                    <TableCell>
                      <Button
                        variant={product.inStock > 0 ? "destructive" : "default"}
                        size="sm"
                        disabled={loadingProductId === product.id}
                        onClick={() => handleToggleStock(product)}
                      >
                        {loadingProductId === product.id ? (
                          <span className="flex items-center">
                            <svg
                              className="animate-spin -ml-1 mr-2 h-4 w-4"
                              xmlns="http://www.w3.org/2000/svg"
                              fill="none"
                              viewBox="0 0 24 24"
                            >
                              <circle
                                className="opacity-25"
                                cx="12"
                                cy="12"
                                r="10"
                                stroke="currentColor"
                                strokeWidth="4"
                              ></circle>
                              <path
                                className="opacity-75"
                                fill="currentColor"
                                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                              ></path>
                            </svg>
                            Loading
                          </span>
                        ) : product.inStock > 0 ? (
                          "Empty stock"
                        ) : (
                          "Fill stock"
                        )}
                      </Button>
                    </TableCell>
                    <TableCell className={`font-medium ${getTextStyle(product.inStock)}`}>{product.name}</TableCell>
                    <TableCell className={getTextStyle(product.inStock)}>
                      <Badge variant="outline">{product.category.categoryName}</Badge>
                    </TableCell>
                    <TableCell className={getTextStyle(product.inStock)}>{formatCurrency(product.unitPrice)}</TableCell>
                    <TableCell>{getStockStatus(product.inStock)}</TableCell>
                    <TableCell className={getTextStyle(product.inStock)}>
                      {product.expirationDate ? formatDate(product.expirationDate) : "N/A"}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button variant="ghost" size="sm" title="Edit Product"
                          onClick={() => handleEditProduct(product)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm" title="Delete Product"
                          onClick={() => handleDeleteProduct(product)}>
                          <Trash className="h-4 w-4 text-red-500" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        <ProductModal
          isOpen={isProductModalOpen}
          onClose={() => setIsProductModalOpen(false)}
          onProductCreated={() => {
            setIsProductModalOpen(false)
            dispatch(fetchProducts())
          }}
        />

        <CategoryModal
          isOpen={isCategoryModalOpen}
          onClose={() => setIsCategoryModalOpen(false)}
          onCategoryCreated={() => {
            setIsCategoryModalOpen(false)
            dispatch(fetchCategories())
          }}
        />

        <EditProductModal
          isOpen={isEditModalOpen}
          productId={selectedProductId}
          onClose={() => {
            setIsEditModalOpen(false)
            setSelectedProductId(null)
          }}
          onProductUpdated={() => {
            setIsEditModalOpen(false)
            setSelectedProductId(null)
            dispatch(fetchProducts())
          }}
        />

        <DeleteProductModal
          isOpen={isDeleteModalOpen}
          product={selectedProduct}
          onClose={() => {
            setIsDeleteModalOpen(false)
            setSelectedProduct(null)
          }}
          onProductDeleted={() => {
            setIsDeleteModalOpen(false)
            setSelectedProduct(null)
            dispatch(fetchProducts())
          }}
        />
      </CardContent>

      {sortedData.length > 0 && (
        <CardFooter className="flex justify-between items-center border-t px-6 py-4">
          <div className="text-sm text-gray-500">
            Showing {startIndex + 1}-{Math.min(endIndex, sortedData.length)} of {sortedData.length} products
          </div>

          <div className="flex items-center space-x-2">
            <Button
              variant="outline"
              size="sm"
              onClick={goToFirstPage}
              disabled={currentPage === 1}
              className="hidden sm:flex"
            >
              <ChevronsLeft className="h-4 w-4" />
            </Button>
            <Button variant="outline" size="sm" onClick={goToPreviousPage} disabled={currentPage === 1}>
              <ChevronLeft className="h-4 w-4" />
            </Button>

            <div className="flex items-center">
              {getPageNumbers().map((page) => (
                <Button
                  key={page}
                  variant={currentPage === page ? "default" : "outline"}
                  size="sm"
                  onClick={() => goToPage(page)}
                  className="w-9"
                >
                  {page}
                </Button>
              ))}
            </div>

            <Button variant="outline" size="sm" onClick={goToNextPage} disabled={currentPage === totalPages}>
              <ChevronRight className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={goToLastPage}
              disabled={currentPage === totalPages}
              className="hidden sm:flex"
            >
              <ChevronsRight className="h-4 w-4" />
            </Button>
          </div>
        </CardFooter>
      )}
    </Card>
  )
}
