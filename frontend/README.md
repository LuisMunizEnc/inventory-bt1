# Inventory Management Dashboard

This application aims to create a comprehensive solution for managing products and categories with real-time data synchronization with a Spring Boot backend.


## **Product Management**
- **CRUD Operations** - Create, Read, Update, Delete products
- **Stock Management** - Toggle between in-stock and out-of-stock status
- **Expiration Tracking** - Visual indicators for product expiration dates
- **Category Assignment** - Organize products by categories
- **Price Management** - Set and update product prices


## Tech Stack
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript 5
- **State Management**: Redux Toolkit
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui
- **HTTP Client**: Axios
- **Date Handling**: date-fns
- **Icons**: Lucide React

## API Integration

### **Base Configuration**
```typescript
// services/api.ts
const API_BASE_URL = "http://localhost:9090"

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
})
```

### **Product Endpoints**

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/products` | Get all products with optional filters | Query params: `name`, `categories[]`, `inStock` |
| `GET` | `/products/{id}` | Get product by ID | - |
| `POST` | `/products` | Create new product | `ProductInfo` |
| `PUT` | `/products/{id}` | Update product | `ProductInfo` |
| `DELETE` | `/products/{id}` | Delete product | - |
| `PUT` | `/products/{id}/outofstock` | Mark product as out of stock | - |
| `PUT` | `/products/{id}/instock` | Mark product as in stock | - |

### **Category Endpoints**

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/categories` | Get all categories | - |
| `GET` | `/categories/{name}` | Get category by name | - |
| `POST` | `/categories` | Create new category | `Category` |