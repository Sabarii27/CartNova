import { Link } from 'react-router-dom';
import Loader from '../../components/Loader';
import ErrorMessage from '../../components/ErrorMessage';
import StatusBadge from '../../components/StatusBadge';
import useFetch from '../../hooks/useFetch';
import { getAllOrders, getDashboard } from '../../services/adminService';
import { formatDate, formatPrice } from '../../utils/format';

export default function AdminDashboard() {
  const stats = useFetch(() => getDashboard(), []);
  const orders = useFetch(() => getAllOrders(), []);

  if (stats.loading) return <Loader />;
  if (stats.error) return <ErrorMessage message={stats.error} onRetry={stats.reload} />;
  const s = stats.data;
  const recent = (orders.data || []).slice(0, 6);

  return (
    <div>
      <h1>Dashboard</h1>
      <div className="stat-grid">
        <div className="stat"><span>Revenue</span><strong>{formatPrice(s.totalRevenue)}</strong><small>Excludes cancelled orders</small></div>
        <div className="stat"><span>Orders</span><strong>{s.totalOrders}</strong><small>{s.pendingOrders} awaiting action</small></div>
        <div className="stat"><span>Products</span><strong>{s.totalProducts}</strong><small>{s.lowStockProducts} low on stock</small></div>
        <div className="stat"><span>Customers</span><strong>{s.totalUsers}</strong><small>Registered accounts</small></div>
      </div>

      <div className="panel">
        <div className="section-head">
          <h2>Recent orders</h2>
          <Link to="/admin/orders">View all</Link>
        </div>
        {orders.loading && <Loader />}
        <ErrorMessage message={orders.error} onRetry={orders.reload} />
        {recent.length > 0 && (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Order</th><th>Customer</th><th>Date</th><th>Status</th><th className="num">Total</th></tr></thead>
              <tbody>
                {recent.map((o) => (
                  <tr key={o.id}>
                    <td><Link to={`/admin/orders/${o.id}`}>#{o.id}</Link></td>
                    <td>{o.userName}</td>
                    <td>{formatDate(o.createdAt)}</td>
                    <td><StatusBadge status={o.status} /></td>
                    <td className="num">{formatPrice(o.totalAmount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!orders.loading && !orders.error && recent.length === 0 && <p className="muted">No orders have been placed yet.</p>}
      </div>
    </div>
  );
}
