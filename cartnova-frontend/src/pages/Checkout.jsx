import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { placeOrder } from '../services/orderService';
import { getErrorMessage } from '../services/api';
import ErrorMessage from '../components/ErrorMessage';
import { formatPrice } from '../utils/format';

export default function Checkout() {
  const { cart, refresh } = useCart();
  const navigate = useNavigate();
  const [address, setAddress] = useState('');
  const [fieldError, setFieldError] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (cart.items.length === 0 && !busy) return <Navigate to="/cart" replace />;

  const submit = async (e) => {
    e.preventDefault();
    if (address.trim().length < 10) return setFieldError('Enter a full delivery address (at least 10 characters).');
    setFieldError('');
    setBusy(true);
    setError('');
    try {
      // The backend rebuilds the order from the DB cart; no prices are sent from here.
      const order = await placeOrder(address.trim());
      await refresh();
      navigate(`/order-success/${order.id}`, { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
      refresh().catch(() => {});
      setBusy(false);
    }
  };

  return (
    <div>
      <h1>Checkout</h1>
      <ErrorMessage message={error} />
      <div className="cart-layout">
        <form className="panel" onSubmit={submit} noValidate>
          <h3>Delivery address</h3>
          <label className="field">
            <span>Where should we deliver?</span>
            <textarea rows="4" value={address} onChange={(e) => setAddress(e.target.value)} placeholder="House number, street, city, PIN code" />
            {fieldError && <small className="field-error">{fieldError}</small>}
          </label>
          <h3>Payment</h3>
          <p className="muted">This is a learning project, so no payment is taken. Orders are placed as cash on delivery.</p>
          <button className="btn btn-accent btn-lg" disabled={busy}>{busy ? 'Placing order...' : `Place order (${formatPrice(cart.totalAmount)})`}</button>
        </form>
        <aside className="summary">
          <h3>Items in this order</h3>
          {cart.items.map((i) => (
            <div key={i.id} className="summary-row"><span>{i.productName} x {i.quantity}</span><span>{formatPrice(i.subtotal)}</span></div>
          ))}
          <div className="summary-row total"><span>Total</span><span>{formatPrice(cart.totalAmount)}</span></div>
          <Link to="/cart" className="summary-link">Edit cart</Link>
        </aside>
      </div>
    </div>
  );
}
