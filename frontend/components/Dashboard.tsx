"use client"

import { ProductFilter } from "./ProductFilter"
import { ProductTable } from "./ProductTable"

export function Dashboard() {
  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Inventory Dashboard</h1>
          <p className="text-muted-foreground">Manage your Inventory and obtain metrics</p>
        </div>
      </div>
      <ProductFilter />
      <ProductTable />
    </div>
  )
}
