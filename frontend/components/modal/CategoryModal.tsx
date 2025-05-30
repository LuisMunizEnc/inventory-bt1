"use client"

import type React from "react"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, Tag } from "lucide-react"
import { categoryService } from "@/services/categoryService"
import type { Category } from "@/types"

interface CategoryModalProps {
  isOpen: boolean
  onClose: () => void
  onCategoryCreated: (category: Category) => void
}

interface FormErrors {
  categoryName?: string
  general?: string
}

export function CategoryModal({ isOpen, onClose, onCategoryCreated }: CategoryModalProps) {
  const [categoryName, setCategoryName] = useState("")
  const [errors, setErrors] = useState<FormErrors>({})
  const [isLoading, setIsLoading] = useState(false)

  const resetForm = () => {
    setCategoryName("")
    setErrors({})
  }

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}

    if (!categoryName.trim()) {
      newErrors.categoryName = "Category name is required"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    setIsLoading(true)
    setErrors({})

    try {
      const newCategory: Category = {
        categoryName: categoryName.trim(),
      }

      const createdCategory = await categoryService.createCategory(newCategory)
      resetForm()
      onCategoryCreated(createdCategory)
    } catch (error: any) {
      console.error("Error creating category:", error)

      if (error.response?.status === 400) {
        const errorMessage =
          error.response?.data?.message || error.response?.data?.error || "A category with this name already exists"
        setErrors({ general: errorMessage })
      } else {
        setErrors({ general: "An error occurred while creating the category. Please try again." })
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleClose = () => {
    resetForm()
    onClose()
  }

  const handleInputChange = (value: string) => {
    setCategoryName(value)

    if (errors.categoryName || errors.general) {
      setErrors({})
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Tag className="h-5 w-5" />
            Create New Category
          </DialogTitle>
          <DialogDescription>Add a new product category to organize your inventory</DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {errors.general && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{errors.general}</AlertDescription>
            </Alert>
          )}

          {/* Category Name */}
          <div className="space-y-2">
            <Label htmlFor="categoryName">
              Category Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="categoryName"
              value={categoryName}
              onChange={(e) => handleInputChange(e.target.value)}
              placeholder="Enter category name"
              maxLength={50}
              className={errors.categoryName ? "border-red-500" : ""}
              autoFocus
            />
            {errors.categoryName && <p className="text-sm text-red-500">{errors.categoryName}</p>}
            <p className="text-xs text-gray-500">{categoryName.length}/50 characters</p>
          </div>

          <DialogFooter className="gap-2">
            <Button type="button" variant="outline" onClick={handleClose} disabled={isLoading}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? (
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
                  Creating...
                </span>
              ) : (
                "Create Category"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
