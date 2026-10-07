import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../services/api';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import { formatPrice, onImgError, PLACEHOLDER_IMG } from '../utils/format';

export default function Cart() {
  const { cart, loading, updateQty, remove, clear } = useCart();
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [busyId, setBusyId] = useState(null);

  const run = async (id, fn) => {
    setBusyId(id);
    setError('');
    try {
      await fn();
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setBusyId(null);
    }
  };

  if (isAdmin) return <EmptyState title="Admins do not have a cart" text="Sign in with a customer account to shop." />;
  if (loading && cart.items.length === 0) return <Loader label="Loading your cart..." />;

  if (cart.items.length === 0) {
    return (
      <EmptyState title="Your cart is empty" text="Add a few products and they will show up here.">
        <Link to="/products" className="btn btn-primary">Browse products</Link>
      </EmptyState>
    );
  }

  return (
    <div>
      <div className="page-head">
        <h1>Your cart</h1>
        <button className="btn btn-sm btn-outline" onClick={() => run('all', clear)} disabled={busyId === 'all'}>Clear cart</button>
      </div>
      <ErrorMessage message={error} />
      <div className="cart-layout">
        <ul className="cart-list">
          {cart.items.map((item) => (
            <li key={item.id} className="cart-item">
              <img src={item.imageUrl || PLACEHOLDER_IMG} alt={item.productName} onError={onImgError} />
              <div className="cart-info">
                <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                <span className="muted">{formatPrice(item.unitPrice)} each</span>
                <button className="link-btn" disabled={busyId === item.id} onClick={() => run(item.id, () => remove(item.id))}>Remove</button>
              </div>
              <div className="qty">
                <button aria-label="Decrease quantity" disabled={busyId === item.id || item.quantity <= 1} onClick={() => run(item.id, () => updateQty(item.id, item.quantity - 1))}>-</button>
                <span>{item.quantity}</span>
                <button aria-label="Increase quantity" disabled={busyId === item.id || item.quantity >= item.stock} onClick={() => run(item.id, () => updateQty(item.id, item.quantity + 1))}>+</button>
              </div>
              <strong className="cart-sub">{formatPrice(item.subtotal)}</strong>
            </li>
          ))}
        </ul>
        <aside className="summary">
          <h3>Order summary</h3>
          <div className="summary-row"><span>Items</span><span>{cart.totalItems}</span></div>
          <div className="summary-row"><span>Shipping</span><span>Free</span></div>
          <div className="summary-row total"><span>Total</span><span>{formatPrice(cart.totalAmount)}</span></div>
          <button className="btn btn-accent btn-block btn-lg" onClick={() => navigate('/checkout')}>Proceed to checkout</button>
          <Link to="/products" className="summary-link">Continue shopping</Link>
        </aside>
      </div>
    </div>
  );
}
