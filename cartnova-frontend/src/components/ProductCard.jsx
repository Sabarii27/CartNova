import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { getErrorMessage } from '../services/api';
import { formatPrice, onImgError, PLACEHOLDER_IMG } from '../utils/format';

export default function ProductCard({ product }) {
  const { isAuthenticated, isAdmin } = useAuth();
  const { add } = useCart();
  const navigate = useNavigate();
  const [busy, setBusy] = useState(false);
  const [note, setNote] = useState('');

  const outOfStock = product.stock <= 0;

  const handleAdd = async () => {
    if (!isAuthenticated) return navigate('/login', { state: { from: '/products' } });
    setBusy(true);
    setNote('');
    try {
      await add(product.id, 1);
      setNote('Added to cart');
    } catch (e) {
      setNote(getErrorMessage(e));
    } finally {
      setBusy(false);
      setTimeout(() => setNote(''), 2500);
    }
  };

  return (
    <article className="product-card">
      <Link to={`/products/${product.id}`} className="product-img-wrap">
        <img src={product.imageUrl || PLACEHOLDER_IMG} alt={product.name} loading="lazy" onError={onImgError} />
        {outOfStock && <span className="stock-flag">Out of stock</span>}
      </Link>
      <div className="product-body">
        <span className="product-cat">{product.categoryName}</span>
        <Link to={`/products/${product.id}`} className="product-name">
          {product.name}
        </Link>
        <div className="product-foot">
          <span className="price">{formatPrice(product.price)}</span>
          {!isAdmin && (
            <button className="btn btn-sm btn-primary" disabled={outOfStock || busy} onClick={handleAdd}>
              {busy ? 'Adding...' : 'Add to cart'}
            </button>
          )}
        </div>
        {note && <p className="card-note" role="status">{note}</p>}
      </div>
    </article>
  );
}
