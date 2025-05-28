"use client"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertTriangle, Loader2 } from "lucide-react"
import { productService } from "@/services/productService"
import type { Product } from "@/types"

interface DeleteProductModalProps {
  isOpen: boolean
  product: Product | null
  onClose: () => void
  onProductDeleted: () => void
}

export function DeleteProductModal({ isOpen, product, onClose, onProductDeleted }: DeleteProductModalProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleDelete = async () => {
    if (!product) return

    setIsLoading(true)
    setError(null)

    try {
      await productService.deleteProduct(product.id)
      onProductDeleted()
    } catch (error: any) {
      console.error("Error deleting product:", error)

      if (error.response?.status === 404) {
        setError("Product not found. It may have already been deleted.")
      } else {
        setError("An error occurred while deleting the product. Please try again.")
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleClose = () => {
    setError(null)
    onClose()
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-red-500" />
            Delete Product
          </DialogTitle>
          <DialogDescription>
            This action cannot be undone. This will permanently delete the product from your inventory.
          </DialogDescription>
        </DialogHeader>

        {error && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {product && (
          <div className="bg-gray-50 p-4 rounded-lg space-y-2">
            <div className="flex justify-between">
              <span className="font-medium">Product:</span>
              <span>{product.name}</span>
            </div>
            <div className="flex justify-between">
              <span className="font-medium">Category:</span>
              <span>{product.category.categoryName}</span>
            </div>
            <div className="flex justify-between">
              <span className="font-medium">Stock:</span>
              <span>{product.inStock} units</span>
            </div>
          </div>
        )}

        <DialogFooter className="gap-2">
          <Button type="button" variant="outline" onClick={handleClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="button" variant="destructive" onClick={handleDelete} disabled={isLoading}>
            {isLoading ? (
              <span className="flex items-center">
                <Loader2 className="animate-spin -ml-1 mr-2 h-4 w-4" />
                Deleting...
              </span>
            ) : (
              "Delete Product"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
