"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { BarChart3, RefreshCw, TrendingUp, Package, DollarSign } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { productService } from "../services/productService"
import type { InventoryMetrics, CategoryMetrics } from "../types"

export function InventoryMetrics() {
  const [metrics, setMetrics] = useState<InventoryMetrics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchMetrics = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await productService.getMetrics()
      setMetrics(data)
    } catch (error: any) {
      console.error("Error fetching metrics:", error)
      setError("Failed to load inventory metrics. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchMetrics()
  }, [])

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat("en-US").format(num)
  }

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BarChart3 className="h-5 w-5" />
            Inventory Metrics
          </CardTitle>
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
          <CardTitle className="flex items-center gap-2">
            <BarChart3 className="h-5 w-5" />
            Inventory Metrics
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
          <Button variant="outline" onClick={fetchMetrics} className="mt-4">
            <RefreshCw className="h-4 w-4 mr-2" />
            Try Again
          </Button>
        </CardContent>
      </Card>
    )
  }

  if (!metrics) {
    return null
  }

  const maxValue = Math.max(...metrics.categoryMetrics.map((cat) => cat.totalValueInStock))
  const maxProducts = Math.max(...metrics.categoryMetrics.map((cat) => cat.totalProductsInStock))

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <BarChart3 className="h-5 w-5" />
            Inventory Metrics
          </div>
          <Button variant="outline" size="sm" onClick={fetchMetrics}>
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center gap-2">
                <Package className="h-4 w-4 text-blue-600" />
                <div>
                  <p className="text-sm font-medium text-gray-600">Total Products</p>
                  <p className="text-2xl font-bold">{formatNumber(metrics.overallMetrics.totalProductsInStock)}</p>
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center gap-2">
                <DollarSign className="h-4 w-4 text-green-600" />
                <div>
                  <p className="text-sm font-medium text-gray-600">Total Value</p>
                  <p className="text-2xl font-bold">{formatCurrency(metrics.overallMetrics.totalValueInStock)}</p>
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center gap-2">
                <TrendingUp className="h-4 w-4 text-purple-600" />
                <div>
                  <p className="text-sm font-medium text-gray-600">Average Price</p>
                  <p className="text-2xl font-bold">{formatCurrency(metrics.overallMetrics.averagePriceInStock)}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Metrics Table */}
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Category</TableHead>
                <TableHead className="text-right">Products in Stock</TableHead>
                <TableHead className="text-right">Total Value</TableHead>
                <TableHead className="text-right">Average Price</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {/* Category Metrics */}
              {metrics.categoryMetrics
                .sort((a, b) => b.totalValueInStock - a.totalValueInStock)
                .map((category: CategoryMetrics) => (
                  <TableRow key={category.categoryName}>
                    <TableCell className="font-medium">{category.categoryName}</TableCell>
                    <TableCell className="text-right">
                      <span>
                        {formatNumber(category.totalProductsInStock)}
                      </span>
                    </TableCell>
                    <TableCell className="text-right">
                      <span>
                        {formatCurrency(category.totalValueInStock)}
                      </span>
                    </TableCell>
                    <TableCell className="text-right">{formatCurrency(category.averagePriceInStock)}</TableCell>
                  </TableRow>
                ))}

              {/* Overall Metrics Row */}
              <TableRow className="border-t-2 border-gray-300 bg-gray-50 font-semibold">
                <TableCell className="font-bold text-gray-900">Overall Total</TableCell>
                <TableCell className="text-right font-bold">
                  {formatNumber(metrics.overallMetrics.totalProductsInStock)}
                </TableCell>
                <TableCell className="text-right font-bold">
                  {formatCurrency(metrics.overallMetrics.totalValueInStock)}
                </TableCell>
                <TableCell className="text-right font-bold">
                  {formatCurrency(metrics.overallMetrics.averagePriceInStock)}
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  )
}
