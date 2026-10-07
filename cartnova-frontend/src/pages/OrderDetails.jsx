import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import StatusBadge from '../components/StatusBadge';
import useFetch from '../hooks/useFetch';
import { cancelOrder, getOrder } from '../services/orderService';
import { getErrorMessage } from '../services/api';
import { CANCELLABLE, formatDateTime, formatPrice, onImgError, PLACEHOLDER_IMG } from '../utils/format';

const STEPS = ['PLACED', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];

export default function OrderDetails() {
  const { id } = useParams();
  const { data: order, setData, loading, error, reload } = useFetch(() => getOrder(id), [id]);
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState('');

  if (loading) return <Loader />;
  if (error) return <ErrorMessage message={error} onRetry={reload} />;
  if (!order) return null;

  const cancel = async () => {
    if (!window.confirm('Cancel this order? Items will be returned to stock.')) return;
    setBusy(true);
    setActionError('');
    try {
      setData(await cancelOrder(order.id));
    } catch (e) {
      setActionError(getErrorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  const stepIndex = STEPS.indexOf(order.status);

  return (
    <div>
      <p className="breadcrumb"><Link to="/orders">My orders</Link> / #{order.id}</p>
      <div className="page-head">
        <h1>Order #{order.id}</h1>
        <StatusBadge status={order.status} />
      </div>
      <ErrorMessage message={actionError} />

      {order.status === 'CANCELLED' ? (
        <div className="alert alert-info">This order was cancelled.</div>
      ) : (
        <ol className="tracker">
          {STEPS.map((s, i) => (
            <li key={s} className={i <= stepIndex ? 'done' : ''}><span>{s.charAt(0) + s.slice(1).toLowerCase()}</span></li>
          ))}
        </ol>
      )}

      <div className="cart-layout">
        <ul className="cart-list">
          {order.items.map((i) => (
            <li key={i.id} className="cart-item order-item">
              <img src={i.imageUrl || PLACEHOLDER_IMG} alt={i.productName} onError={onImgError} />
              <div className="cart-info">
                <Link to={`/products/${i.productId}`}>{i.productName}</Link>
                <span className="muted">{formatPrice(i.unitPrice)} x {i.quantity}</span>
              </div>
              <strong className="cart-sub">{formatPrice(i.subtotal)}</strong>
            </li>
          ))}
        </ul>
        <aside className="summary">
          <h3>Details</h3>
          <div className="summary-row"><span>Placed</span><span>{formatDateTime(order.createdAt)}</span></div>
          <div className="summary-row"><span>Last update</span><span>{formatDateTime(order.updatedAt)}</span></div>
          <div className="summary-row total"><span>Total</span><span>{formatPrice(order.totalAmount)}</span></div>
          {order.shippingAddress && <p className="address"><strong>Deliver to</strong><br />{order.shippingAddress}</p>}
          {CANCELLABLE.includes(order.status) && (
            <button className="btn btn-danger btn-block" disabled={busy} onClick={cancel}>{busy ? 'Cancelling...' : 'Cancel order'}</button>
          )}
        </aside>
      </div>
    </div>
  );
}
