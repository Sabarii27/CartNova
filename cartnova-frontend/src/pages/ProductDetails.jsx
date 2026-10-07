import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import useFetch from '../hooks/useFetch';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { getProduct } from '../services/productService';
import { getErrorMessage } from '../services/api';
import { formatPrice, onImgError, PLACEHOLDER_IMG } from '../utils/format';

export default function ProductDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isAdmin } = useAuth();
  const { add } = useCart();
  const { data: product, loading, error, reload } = useFetch(() => getProduct(id), [id]);
  const [qty, setQty] = useState(1);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState({ type: '', text: '' });

  if (loading) return <Loader />;
  if (error) return <ErrorMessage message={error} onRetry={reload} />;
  if (!product) return null;

  const outOfStock = product.stock <= 0;
  const maxQty = Math.max(1, product.stock);

  const handleAdd = async () => {
    if (!isAuthenticated) return navigate('/login', { state: { from: `/products/${id}` } });
    setBusy(true);
    setMsg({ type: '', text: '' });
    try {
      await add(product.id, qty);
      setMsg({ type: 'success', text: `${qty} added to your cart.` });
    } catch (e) {
      setMsg({ type: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div>
      <p className="breadcrumb"><Link to="/products">Products</Link> / {product.categoryName} / {product.name}</p>
      <div className="detail">
        <div className="detail-img">
          <img src={product.imageUrl || PLACEHOLDER_IMG} alt={product.name} onError={onImgError} />
        </div>
        <div className="detail-info">
          <span className="product-cat">{product.categoryName}</span>
          <h1>{product.name}</h1>
          <p className="detail-price">{formatPrice(product.price)}</p>
          <p className={`stock-line ${outOfStock ? 'out' : product.stock <= 5 ? 'low' : 'ok'}`}>
            {outOfStock ? 'Out of stock' : product.stock <= 5 ? `Only ${product.stock} left` : `In stock (${product.stock} available)`}
          </p>
          <p className="detail-desc">{product.description}</p>

          {!isAdmin && (
            <div className="buy-box">
              <div className="qty">
                <button aria-label="Decrease quantity" onClick={() => setQty(Math.max(1, qty - 1))} disabled={qty <= 1}>-</button>
                <span aria-live="polite">{qty}</span>
                <button aria-label="Increase quantity" onClick={() => setQty(Math.min(maxQty, qty + 1))} disabled={outOfStock || qty >= maxQty}>+</button>
              </div>
              <button className="btn btn-primary btn-lg" disabled={outOfStock || busy} onClick={handleAdd}>
                {busy ? 'Adding...' : 'Add to cart'}
              </button>
            </div>
          )}
          {msg.text && (
            <div className={`alert alert-${msg.type}`} role="status">
              {msg.text} {msg.type === 'success' && <Link to="/cart">View cart</Link>}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
