import { Link } from 'react-router-dom';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import StatusBadge from '../components/StatusBadge';
import useFetch from '../hooks/useFetch';
import { getMyOrders } from '../services/orderService';
import { formatDate, formatPrice } from '../utils/format';

export default function MyOrders() {
  const { data, loading, error, reload } = useFetch(() => getMyOrders(), []);

  if (loading) return <Loader label="Loading your orders..." />;
  if (error) return <ErrorMessage message={error} onRetry={reload} />;
  if (!data || data.length === 0) {
    return (
      <EmptyState title="No orders yet" text="When you place an order it will appear here.">
        <Link to="/products" className="btn btn-primary">Start shopping</Link>
      </EmptyState>
    );
  }

  return (
    <div>
      <h1>My orders</h1>
      <div className="order-list">
        {data.map((o) => (
          <Link key={o.id} to={`/orders/${o.id}`} className="order-card">
            <div>
              <strong>Order #{o.id}</strong>
              <span className="muted">Placed on {formatDate(o.createdAt)}</span>
              <span className="muted">{o.items.length} item{o.items.length !== 1 && 's'}</span>
            </div>
            <div className="order-right">
              <StatusBadge status={o.status} />
              <strong>{formatPrice(o.totalAmount)}</strong>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
