import { Link, useParams } from 'react-router-dom';

export default function OrderSuccess() {
  const { id } = useParams();
  return (
    <div className="success">
      <div className="success-mark" aria-hidden="true">
        <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12.5l4.5 4.5L19 7.5" /></svg>
      </div>
      <h1>Order placed</h1>
      <p>Your order number is <strong>#{id}</strong>. We will update its status as it moves along.</p>
      <div className="hero-actions center">
        <Link to={`/orders/${id}`} className="btn btn-primary">View order</Link>
        <Link to="/products" className="btn btn-outline">Keep shopping</Link>
      </div>
    </div>
  );
}
