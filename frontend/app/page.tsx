"use client"

import { Provider } from "react-redux"
import { store } from "../store"
import { Dashboard } from "../components/Dashboard"

export default function Home() {
  return (
    <Provider store={store}>
      <Dashboard />
    </Provider>
  )
}
