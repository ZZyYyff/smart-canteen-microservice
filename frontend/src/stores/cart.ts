import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
  dishId: number
  dishName: string
  price: number
  quantity: number
}

const CART_KEY = 'sc_cart'

function loadCart(): CartItem[] {
  try {
    const raw = localStorage.getItem(CART_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveCart(items: CartItem[]) {
  localStorage.setItem(CART_KEY, JSON.stringify(items))
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>(loadCart())

  const totalCount = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))

  const totalPrice = computed(() =>
    items.value.reduce((sum, i) => sum + i.price * i.quantity, 0),
  )

  function addToCart(dish: { dishId: number; dishName: string; price: number }) {
    const exist = items.value.find((i) => i.dishId === dish.dishId)
    if (exist) {
      exist.quantity++
    } else {
      items.value.push({ ...dish, quantity: 1 })
    }
    saveCart(items.value)
  }

  function removeFromCart(dishId: number) {
    items.value = items.value.filter((i) => i.dishId !== dishId)
    saveCart(items.value)
  }

  function updateQuantity(dishId: number, quantity: number) {
    if (quantity <= 0) {
      removeFromCart(dishId)
      return
    }
    const item = items.value.find((i) => i.dishId === dishId)
    if (item) {
      item.quantity = quantity
      saveCart(items.value)
    }
  }

  function clearCart() {
    items.value = []
    localStorage.removeItem(CART_KEY)
  }

  return { items, totalCount, totalPrice, addToCart, removeFromCart, updateQuantity, clearCart }
})
