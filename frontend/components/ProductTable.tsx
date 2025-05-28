"use client"

import { useEffect, useState } from "react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"
import { Package, Edit, Plus, Trash } from "lucide-react"
import { useAppDispatch, useAppSelector } from "../hooks/redux"
import { fetchProducts } from "../store/slices/productsSlice"
import { fetchCategories } from "../store/slices/categoriesSlice"
import type { Product, Category } from "../types"
import { productService } from "../services/productService"
import { ProductModal } from "./modal/ProductModal"
import { CategoryModal } from "./modal/CategoryModal"

export function ProductTable() {
  const dispatch = useAppDispatch()
  const { products, loading, error } = useAppSelector((state) => state.products)
  const [loadingProductId, setLoadingProductId] = useState<string | null>(null)
  const [isProductModalOpen, setIsProductModalOpen] = useState(false)
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false)

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
          <span>Products ({products.length})</span>
          <div className="gap-2 flex items-center">
            <Button variant="outline" onClick={() => setIsCategoryModalOpen(true)}>
                <Plus className="h-4 w-4 mr-2" />
                New Category
            </Button>
            <Button onClick={() => setIsProductModalOpen(true)}>
                <Package className="h-4 w-4 mr-2"/>
                New Product
          </Button>
          </div>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {products.length === 0 ? (
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
                  <TableHead>Name</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Unit Price</TableHead>
                  <TableHead>Stock</TableHead>
                  <TableHead>Expiration</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {products.map((product: Product) => (
                  <TableRow key={product.id}>
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
                          "Set Out of Stock"
                        ) : (
                          "Set In Stock"
                        )}
                      </Button>
                    </TableCell>
                    <TableCell className="font-medium">{product.name}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{product.category.categoryName}</Badge>
                    </TableCell>
                    <TableCell>{formatCurrency(product.unitPrice)}</TableCell>
                    <TableCell>{getStockStatus(product.inStock)}</TableCell>
                    <TableCell>{product.expirationDate ? formatDate(product.expirationDate) : "N/A"}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button variant="ghost" size="sm" title="Edit Product">
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm" title="Delete Product">
                          <Trash className="h-4 w-4 text-red-500"/>
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

      </CardContent>
    </Card>
  )
}
