import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as cartService from '../services/cartService';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);
const EMPTY = { id: null, items: [], totalItems: 0, totalAmount: 0 };

export function CartProvider({ children }) {
  const { isAuthenticated, isAdmin } = useAuth();
  const [cart, setCart] = useState(EMPTY);
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (!isAuthenticated || isAdmin) {
      setCart(EMPTY);
      return EMPTY;
    }
    setLoading(true);
    try {
      const data = await cartService.getCart();
      setCart(data);
      return data;
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, isAdmin]);

  useEffect(() => {
    refresh().catch(() => setCart(EMPTY));
  }, [refresh]);

  // Each action calls the backend, then stores the cart the backend returns
  // (the backend is the source of truth for prices and totals).
  const add = async (productId, quantity = 1) => setCart(await cartService.addToCart(productId, quantity));
  const updateQty = async (itemId, quantity) => setCart(await cartService.updateCartItem(itemId, quantity));
  const remove = async (itemId) => setCart(await cartService.removeCartItem(itemId));
  const clear = async () => {
    await cartService.clearCart();
    setCart(EMPTY);
  };

  const value = useMemo(() => ({ cart, loading, refresh, add, updateQty, remove, clear }), [cart, loading, refresh]);
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export const useCart = () => useContext(CartContext);
