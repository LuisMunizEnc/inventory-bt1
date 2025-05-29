"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { CalendarIcon, AlertCircle, Loader2 } from "lucide-react"
import { format, parseISO } from "date-fns"
import { cn } from "@/lib/utils"
import { useAppDispatch, useAppSelector } from "@/hooks/redux"
import { fetchCategories } from "@/store/slices/categoriesSlice"
import { productService } from "@/services/productService"
import type { Product, ProductInfo } from "@/types"

interface EditProductModalProps {
  isOpen: boolean
  productId: string | null
  onClose: () => void
  onProductUpdated: () => void
}

interface FormData {
  name: string
  categoryName: string
  inStock: string
  unitPrice: string
  expirationDate: Date | undefined
}

interface FormErrors {
  name?: string
  categoryName?: string
  inStock?: string
  unitPrice?: string
  general?: string
}

export function EditProductModal({ isOpen, productId, onClose, onProductUpdated }: EditProductModalProps) {
  const dispatch = useAppDispatch()
  const { categories } = useAppSelector((state) => state.categories)

  const [formData, setFormData] = useState<FormData>({
    name: "",
    categoryName: "",
    inStock: "",
    unitPrice: "",
    expirationDate: undefined,
  })

  const [errors, setErrors] = useState<FormErrors>({})
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingProduct, setIsLoadingProduct] = useState(false)
  const [product, setProduct] = useState<Product | null>(null)

  useEffect(() => {
    if (isOpen) {
      dispatch(fetchCategories())
    }
  }, [isOpen, dispatch])

  useEffect(() => {
    if (isOpen && productId) {
      loadProduct(productId)
    }
  }, [isOpen, productId])

  const loadProduct = async (id: string) => {
    setIsLoadingProduct(true)
    setErrors({})

    try {
      const productData = await productService.getProductById(id)
      setProduct(productData)

      // Populate form with product data
      setFormData({
        name: productData.name,
        categoryName: productData.category.categoryName,
        inStock: productData.inStock.toString(),
        unitPrice: productData.unitPrice.toString(),
        expirationDate: productData.expirationDate ? parseISO(productData.expirationDate) : undefined,
      })
    } catch (error: any) {
      console.error("Error loading product:", error)
      setErrors({ general: "Failed to load product data. Please try again." })
    } finally {
      setIsLoadingProduct(false)
    }
  }

  const resetForm = () => {
    setFormData({
      name: "",
      categoryName: "",
      inStock: "",
      unitPrice: "",
      expirationDate: undefined,
    })
    setErrors({})
    setProduct(null)
  }

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}

    if (!formData.name.trim()) {
      newErrors.name = "Product name is required"
    } else if (formData.name.length > 120) {
      newErrors.name = "Product name must be 120 characters or less"
    }

    if (!formData.categoryName) {
      newErrors.categoryName = "Category is required"
    }

    if (!formData.inStock.trim()) {
      newErrors.inStock = "Stock quantity is required"
    } else if (isNaN(Number(formData.inStock)) || Number(formData.inStock) < 0) {
      newErrors.inStock = "Stock quantity must be a valid positive number"
    }

    if (!formData.unitPrice.trim()) {
      newErrors.unitPrice = "Unit price is required"
    } else if (isNaN(Number(formData.unitPrice)) || Number(formData.unitPrice) <= 0) {
      newErrors.unitPrice = "Unit price must be a valid positive number"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm() || !productId) {
      return
    }

    setIsLoading(true)
    setErrors({})

    try {
      const productInfo: ProductInfo = {
        name: formData.name.trim(),
        categoryName: formData.categoryName,
        inStock: Number(formData.inStock),
        unitPrice: Number(formData.unitPrice),
        expirationDate: formData.expirationDate ? format(formData.expirationDate, "yyyy-MM-dd") : undefined,
      }

      await productService.updateProduct(productId, productInfo)
      resetForm()
      onProductUpdated()
    } catch (error: any) {
      console.error("Error updating product:", error)

      if (error.response?.status === 400) {
        setErrors({ general: "Invalid product data. Please check your inputs." })
      } else if (error.response?.status === 404) {
        setErrors({ general: "Product not found. It may have been deleted." })
      } else {
        setErrors({ general: "An error occurred while updating the product. Please try again." })
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleClose = () => {
    resetForm()
    onClose()
  }

  const handleInputChange = (field: keyof FormData, value: string | Date | undefined) => {
    setFormData((prev) => ({ ...prev, [field]: value }))

    // Clear field-specific error when user starts typing
    if (errors[field as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Edit Product</DialogTitle>
          <DialogDescription>
            {product ? `Update details for "${product.name}"` : "Loading product details..."}
          </DialogDescription>
        </DialogHeader>

        {isLoadingProduct ? (
          <div className="flex items-center justify-center py-8">
            <Loader2 className="h-8 w-8 animate-spin" />
            <span className="ml-2">Loading product...</span>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {errors.general && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{errors.general}</AlertDescription>
              </Alert>
            )}

            {/* Product Name */}
            <div className="space-y-2">
              <Label htmlFor="name">
                Product Name <span className="text-red-500">*</span>
              </Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                placeholder="Enter product name"
                maxLength={120}
                className={errors.name ? "border-red-500" : ""}
              />
              {errors.name && <p className="text-sm text-red-500">{errors.name}</p>}
              <p className="text-xs text-gray-500">{formData.name.length}/120 characters</p>
            </div>

            {/* Category */}
            <div className="space-y-2">
              <Label htmlFor="category">
                Category <span className="text-red-500">*</span>
              </Label>
              <Select value={formData.categoryName} onValueChange={(value) => handleInputChange("categoryName", value)}>
                <SelectTrigger className={errors.categoryName ? "border-red-500" : ""}>
                  <SelectValue placeholder="Select a category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((category) => (
                    <SelectItem key={category.categoryName} value={category.categoryName}>
                      {category.categoryName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.categoryName && <p className="text-sm text-red-500">{errors.categoryName}</p>}
            </div>

            {/* Stock Quantity */}
            <div className="space-y-2">
              <Label htmlFor="stock">
                Stock Quantity <span className="text-red-500">*</span>
              </Label>
              <Input
                id="stock"
                type="number"
                min="0"
                value={formData.inStock}
                onChange={(e) => handleInputChange("inStock", e.target.value)}
                placeholder="Enter stock quantity"
                className={errors.inStock ? "border-red-500" : ""}
              />
              {errors.inStock && <p className="text-sm text-red-500">{errors.inStock}</p>}
            </div>

            {/* Unit Price */}
            <div className="space-y-2">
              <Label htmlFor="price">
                Unit Price <span className="text-red-500">*</span>
              </Label>
              <Input
                id="price"
                type="number"
                min="0"
                step="0.01"
                value={formData.unitPrice}
                onChange={(e) => handleInputChange("unitPrice", e.target.value)}
                placeholder="Enter unit price"
                className={errors.unitPrice ? "border-red-500" : ""}
              />
              {errors.unitPrice && <p className="text-sm text-red-500">{errors.unitPrice}</p>}
            </div>

            {/* Expiration Date */}
            <div className="space-y-2">
              <Label>Expiration Date (Optional)</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      "w-full justify-start text-left font-normal",
                      !formData.expirationDate && "text-muted-foreground",
                    )}
                  >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {formData.expirationDate ? format(formData.expirationDate, "PPP") : "Pick a date"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                  <Calendar
                    mode="single"
                    selected={formData.expirationDate}
                    onSelect={(date) => handleInputChange("expirationDate", date)}
                    disabled={(date) => date < new Date()}
                    initialFocus
                  />
                </PopoverContent>
              </Popover>
            </div>

            <DialogFooter className="gap-2">
              <Button type="button" variant="outline" onClick={handleClose} disabled={isLoading}>
                Cancel
              </Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? (
                  <span className="flex items-center">
                    <Loader2 className="animate-spin -ml-1 mr-2 h-4 w-4" />
                    Updating...
                  </span>
                ) : (
                  "Update Product"
                )}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}
