"use client"

import { useState, useMemo } from "react"
import type { Product, SortConfig } from "../types"

export function useSorting(data: Product[]) {
  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null)

  const sortedData = useMemo(() => {
    if (!sortConfig) return data

    return [...data].sort((a, b) => {
      let aValue: any
      let bValue: any

      switch (sortConfig.field) {
        case "name":
          aValue = a.name.toLowerCase()
          bValue = b.name.toLowerCase()
          break
        case "category":
          aValue = a.category.categoryName.toLowerCase()
          bValue = b.category.categoryName.toLowerCase()
          break
        case "unitPrice":
          aValue = a.unitPrice
          bValue = b.unitPrice
          break
        case "inStock":
          aValue = a.inStock
          bValue = b.inStock
          break
        case "expirationDate":
          // Handle null/undefined expiration dates - put them at the end
          if (!a.expirationDate && !b.expirationDate) return 0
          if (!a.expirationDate) return 1
          if (!b.expirationDate) return -1
          aValue = new Date(a.expirationDate).getTime()
          bValue = new Date(b.expirationDate).getTime()
          break
        default:
          return 0
      }

      if (aValue < bValue) {
        return sortConfig.direction === "asc" ? -1 : 1
      }
      if (aValue > bValue) {
        return sortConfig.direction === "asc" ? 1 : -1
      }
      return 0
    })
  }, [data, sortConfig])

  const handleSort = (field: SortConfig["field"]) => {
    setSortConfig((prevConfig) => {
      if (prevConfig?.field === field) {
        return {
          field,
          direction: prevConfig.direction === "asc" ? "desc" : "asc",
        }
      }
      return {
        field,
        direction: "asc",
      }
    })
  }

  const clearSort = () => {
    setSortConfig(null)
  }

  return {
    sortedData,
    sortConfig,
    handleSort,
    clearSort,
  }
}
