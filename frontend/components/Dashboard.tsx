"use client"

import { InventoryMetrics } from "./InventoryMetrics"
import { ProductFilter } from "./ProductFilter"
import { ProductTable } from "./ProductTable"
import { Header } from "./ui/header"

export function Dashboard() {
  return (
    <>
      <Header/>
      <div className="container mx-auto p-6 space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-muted-foreground">Manage your Inventory and obtain metrics</p>
          </div>
        </div>
        <ProductFilter />
        <ProductTable />
        <InventoryMetrics />
      </div>
    </>
  )
}
