import { Link } from 'react-router-dom';
import ProductCard from '../components/ProductCard';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import useFetch from '../hooks/useFetch';
import { getCategories, getProducts } from '../services/productService';

export default function Home() {
  const featured = useFetch(() => getProducts({ page: 0, size: 8, sort: 'createdAt,desc' }), []);
  const cats = useFetch(() => getCategories(), []);

  return (
    <>
      <section className="hero">
        <div className="hero-text">
          <h1>Everything you need, delivered without the wait.</h1>
          <p>
            Browse electronics, fashion, home and books from one catalogue. Live stock, honest prices and
            order tracking from placed to delivered.
          </p>
          <div className="hero-actions">
            <Link to="/products" className="btn btn-accent btn-lg">Start shopping</Link>
            <Link to="/register" className="btn btn-ghost btn-lg">Create an account</Link>
          </div>
        </div>
        <div className="hero-panel" aria-hidden="true">
          <div className="hero-tag">In stock now</div>
          <div className="hero-stack">
            <span>Free returns on eligible orders</span>
            <span>Secure sign-in with JWT</span>
            <span>Track every order status</span>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="section-head">
          <h2>Shop by category</h2>
          <Link to="/products">View all</Link>
        </div>
        {cats.loading && <Loader />}
        <ErrorMessage message={cats.error} onRetry={cats.reload} />
        <div className="cat-grid">
          {(cats.data || []).map((c) => (
            <Link key={c.id} to={`/products?category=${encodeURIComponent(c.name)}`} className="cat-tile">
              <strong>{c.name}</strong>
              <span>{c.description}</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="section">
        <div className="section-head">
          <h2>New arrivals</h2>
          <Link to="/products">See more</Link>
        </div>
        {featured.loading && <Loader label="Loading products..." />}
        <ErrorMessage message={featured.error} onRetry={featured.reload} />
        {featured.data && featured.data.content?.length === 0 && (
          <EmptyState title="No products yet" text="Check back soon, new items are on the way." />
        )}
        <div className="product-grid">
          {(featured.data?.content || []).map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </section>

      <section className="cta">
        <h2>Ready to build your first cart?</h2>
        <p>Sign up in under a minute and keep track of every order.</p>
        <Link to="/register" className="btn btn-accent btn-lg">Create an account</Link>
      </section>
    </>
  );
}
