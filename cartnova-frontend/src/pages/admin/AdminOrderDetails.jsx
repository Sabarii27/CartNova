import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Loader from '../../components/Loader';
import ErrorMessage from '../../components/ErrorMessage';
import StatusBadge from '../../components/StatusBadge';
import useFetch from '../../hooks/useFetch';
import { getAdminOrder, updateOrderStatus } from '../../services/adminService';
import { getErrorMessage } from '../../services/api';
import { formatDateTime, formatPrice, ORDER_STATUSES, onImgError, PLACEHOLDER_IMG } from '../../utils/format';

export default function AdminOrderDetails() {
  const { id } = useParams();
  const { data: order, setData, loading, error, reload } = useFetch(() => getAdminOrder(id), [id]);
  const [status, setStatus] = useState('');
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState({ type: '', text: '' });

  useEffect(() => { if (order) setStatus(order.status); }, [order]);

  if (loading) return <Loader />;
  if (error) return <ErrorMessage message={error} onRetry={reload} />;
  if (!order) return null;

  const save = async () => {
    setBusy(true);
    setMsg({ type: '', text: '' });
    try {
      setData(await updateOrderStatus(order.id, status));
      setMsg({ type: 'success', text: 'Order status updated.' });
    } catch (e) {
      setMsg({ type: 'error', text: getErrorMessage(e) });
      setStatus(order.status);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div>
      <p className="breadcrumb"><Link to="/admin/orders">Orders</Link> / #{order.id}</p>
      <div className="page-head"><h1>Order #{order.id}</h1><StatusBadge status={order.status} /></div>
      {msg.text && <div className={`alert alert-${msg.type}`} role="status">{msg.text}</div>}

      <div className="cart-layout">
        <div className="panel">
          <h3>Items</h3>
          <ul className="cart-list flat">
            {order.items.map((i) => (
              <li key={i.id} className="cart-item order-item">
                <img src={i.imageUrl || PLACEHOLDER_IMG} alt={i.productName} onError={onImgError} />
                <div className="cart-info"><span>{i.productName}</span><span className="muted">{formatPrice(i.unitPrice)} x {i.quantity}</span></div>
                <strong className="cart-sub">{formatPrice(i.subtotal)}</strong>
              </li>
            ))}
          </ul>
        </div>
        <aside className="summary">
          <h3>Customer</h3>
          <p className="address">{order.userName}<br /><span className="muted">{order.userEmail}</span></p>
          {order.shippingAddress && <p className="address"><strong>Deliver to</strong><br />{order.shippingAddress}</p>}
          <div className="summary-row"><span>Placed</span><span>{formatDateTime(order.createdAt)}</span></div>
          <div className="summary-row total"><span>Total</span><span>{formatPrice(order.totalAmount)}</span></div>
          <label className="field">
            <span>Update status</span>
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </label>
          <button className="btn btn-primary btn-block" disabled={busy || status === order.status} onClick={save}>{busy ? 'Saving...' : 'Save status'}</button>
        </aside>
      </div>
    </div>
  );
}
