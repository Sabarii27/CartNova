import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [q, setQ] = useState('');

  const close = () => setOpen(false);

  const onSearch = (e) => {
    e.preventDefault();
    navigate(q.trim() ? `/products?search=${encodeURIComponent(q.trim())}` : '/products');
    close();
  };

  const onLogout = () => {
    logout();
    close();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="container nav-inner">
        <Link to="/" className="brand" onClick={close}>
          <svg width="30" height="30" viewBox="0 0 64 64" aria-hidden="true">
            <rect width="64" height="64" rx="14" fill="#0b6e6e" />
            <path d="M14 18h8l5 20h18l5-14H25" fill="none" stroke="#ffc233" strokeWidth="5" strokeLinecap="round" strokeLinejoin="round" />
            <circle cx="29" cy="47" r="3.5" fill="#fff" />
            <circle cx="43" cy="47" r="3.5" fill="#fff" />
          </svg>
          CartNova
        </Link>

        <form className="nav-search" onSubmit={onSearch} role="search">
          <input type="search" placeholder="Search products" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search products" />
          <button type="submit" className="btn btn-primary btn-sm">Search</button>
        </form>

        <button className="nav-toggle" aria-label="Toggle menu" aria-expanded={open} onClick={() => setOpen(!open)}>
          <span /><span /><span />
        </button>

        <nav className={`nav-links ${open ? 'open' : ''}`}>
          <NavLink to="/products" onClick={close}>Shop</NavLink>
          {isAuthenticated && !isAdmin && <NavLink to="/orders" onClick={close}>Orders</NavLink>}
          {isAdmin && <NavLink to="/admin" onClick={close}>Admin</NavLink>}
          {!isAdmin && (
            <NavLink to="/cart" className="cart-link" onClick={close}>
              Cart
              {cart.totalItems > 0 && <span className="cart-count">{cart.totalItems}</span>}
            </NavLink>
          )}
          {isAuthenticated ? (
            <>
              <NavLink to="/profile" onClick={close}>{user.name.split(' ')[0]}</NavLink>
              <button className="btn btn-sm btn-outline" onClick={onLogout}>Logout</button>
            </>
          ) : (
            <>
              <NavLink to="/login" onClick={close}>Login</NavLink>
              <Link to="/register" className="btn btn-sm btn-primary" onClick={close}>Sign up</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
