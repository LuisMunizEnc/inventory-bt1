"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { X, Search, Plus } from "lucide-react"
import { useAppDispatch, useAppSelector } from "../hooks/redux"
import { setFilters, fetchProducts } from "../store/slices/productsSlice"
import { fetchCategories } from "../store/slices/categoriesSlice"
import type { ProductFilters } from "../types"

export function ProductFilter() {
  const dispatch = useAppDispatch()
  const { filters } = useAppSelector((state) => state.products)
  const { categories } = useAppSelector((state) => state.categories)

  const [localFilters, setLocalFilters] = useState<ProductFilters>(filters)

  useEffect(() => {
    dispatch(fetchCategories())
    dispatch(fetchProducts(filters))
  }, [dispatch])

  const handleNameChange = (value: string) => {
    setLocalFilters((prev) => ({ ...prev, name: value }))
  }

  const handleCategoryToggle = (categoryName: string) => {
    setLocalFilters((prev) => {
      const currentCategories = prev.categories || []
      const isSelected = currentCategories.includes(categoryName)

      return {
        ...prev,
        categories: isSelected
          ? currentCategories.filter((cat) => cat !== categoryName)
          : [...currentCategories, categoryName],
      }
    })
  }

  const handleStockChange = (value: string) => {
    let stockValue: boolean | null = null
    if (value === "in") stockValue = true
    else if (value === "out") stockValue = false

    setLocalFilters((prev) => ({ ...prev, inStock: stockValue }))
  }

  const handleSearch = () => {
    dispatch(setFilters(localFilters))
    dispatch(fetchProducts(localFilters))
  }

  const handleClearFilters = () => {
    const clearedFilters: ProductFilters = {
      name: "",
      categories: [],
      inStock: null,
    }
    setLocalFilters(clearedFilters)
    dispatch(setFilters(clearedFilters))
    dispatch(fetchProducts(clearedFilters))
  }

  const removeCategoryFilter = (categoryName: string) => {
    setLocalFilters((prev) => ({
      ...prev,
      categories: (prev.categories || []).filter((cat) => cat !== categoryName),
    }))
  }

  const getStockValue = () => {
    if (localFilters.inStock === true) return "in"
    if (localFilters.inStock === false) return "out"
    return "all"
  }

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <span>Product Filters</span>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Name Filter */}
          <div className="space-y-2 col-span-3">
            <Label htmlFor="name">Product Name</Label>
            <Input
              id="name"
              placeholder="Search by name..."
              value={localFilters.name || ""}
              onChange={(e) => handleNameChange(e.target.value)}
            />
          </div>

          {/* Category Filter */}
          <div className="space-y-2">
            <Label>Categories</Label>
            <Select>
              <SelectTrigger>
                <SelectValue placeholder="Select categories" />
              </SelectTrigger>
              <SelectContent>
                {categories.map((category) => (
                  <div key={category.categoryName} className="flex items-center space-x-2 px-2 py-1">
                    <Checkbox
                      id={category.categoryName}
                      checked={(localFilters.categories || []).includes(category.categoryName)}
                      onCheckedChange={() => handleCategoryToggle(category.categoryName)}
                    />
                    <Label htmlFor={category.categoryName} className="text-sm font-normal">
                      {category.categoryName}
                    </Label>
                  </div>
                ))}
              </SelectContent>
            </Select>

            {/* Selected Categories */}
            {localFilters.categories && localFilters.categories.length > 0 && (
              <div className="flex flex-wrap gap-1 mt-2">
                {localFilters.categories.map((categoryName) => (
                  <Badge key={categoryName} className="text-xs">
                    {categoryName}
                    <button
                      onClick={() => removeCategoryFilter(categoryName)}
                      className="ml-1 hover:bg-gray-200 rounded-full p-0.5"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </Badge>
                ))}
              </div>
            )}
          </div>

          {/* Stock Availability Filter */}
          <div className="space-y-2">
            <Label htmlFor="stock">Availability</Label>
            <Select value={getStockValue()} onValueChange={handleStockChange}>
              <SelectTrigger>
                <SelectValue placeholder="Select availability" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Stock</SelectItem>
                <SelectItem value="in">In Stock</SelectItem>
                <SelectItem value="out">Out of Stock</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-end gap-2 pt-4">
          <Button onClick={handleSearch} className="flex items-center gap-2">
            <Search className="h-4 w-4" />
            Search
          </Button>
          <Button variant="outline" onClick={handleClearFilters}>
            Clear Filters
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
