import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Loader from '../../components/Loader';
import ErrorMessage from '../../components/ErrorMessage';
import EmptyState from '../../components/EmptyState';
import StatusBadge from '../../components/StatusBadge';
import useFetch from '../../hooks/useFetch';
import { getAllOrders } from '../../services/adminService';
import { formatDateTime, formatPrice, ORDER_STATUSES } from '../../utils/format';

export default function AdminOrders() {
  const { data, loading, error, reload } = useFetch(() => getAllOrders(), []);
  const [status, setStatus] = useState('');
  const [q, setQ] = useState('');

  const rows = useMemo(() => {
    const term = q.trim().toLowerCase();
    return (data || []).filter(
      (o) =>
        (!status || o.status === status) &&
        (!term || String(o.id) === term || o.userName.toLowerCase().includes(term) || o.userEmail.toLowerCase().includes(term))
    );
  }, [data, status, q]);

  return (
    <div>
      <h1>Orders</h1>
      <div className="toolbar">
        <input placeholder="Order number, customer name or email" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search orders" />
        <select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Status">
          <option value="">All statuses</option>
          {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>
      {loading && <Loader />}
      <ErrorMessage message={error} onRetry={reload} />
      {!loading && !error && rows.length === 0 && <EmptyState title="No orders found" text="No orders match these filters." />}
      {rows.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead><tr><th>Order</th><th>Customer</th><th>Placed</th><th>Status</th><th className="num">Total</th><th className="num">Details</th></tr></thead>
            <tbody>
              {rows.map((o) => (
                <tr key={o.id}>
                  <td>#{o.id}</td>
                  <td>{o.userName}<br /><small className="muted">{o.userEmail}</small></td>
                  <td>{formatDateTime(o.createdAt)}</td>
                  <td><StatusBadge status={o.status} /></td>
                  <td className="num">{formatPrice(o.totalAmount)}</td>
                  <td className="num"><Link to={`/admin/orders/${o.id}`} className="btn btn-sm btn-outline">Open</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
