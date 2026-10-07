import { useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const onLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="admin-shell">
      <aside className={`admin-side ${open ? 'open' : ''}`}>
        <Link to="/admin" className="brand brand-light">CartNova <small>Admin</small></Link>
        <nav onClick={() => setOpen(false)}>
          <NavLink to="/admin" end>Dashboard</NavLink>
          <NavLink to="/admin/products">Products</NavLink>
          <NavLink to="/admin/orders">Orders</NavLink>
          <NavLink to="/admin/users">Users</NavLink>
        </nav>
        <div className="admin-side-foot">
          <Link to="/">View storefront</Link>
          <button onClick={onLogout}>Logout</button>
        </div>
      </aside>
      <div className="admin-main">
        <div className="admin-top">
          <button className="btn btn-sm btn-outline admin-menu-btn" onClick={() => setOpen(!open)}>Menu</button>
          <span>Signed in as <strong>{user.name}</strong></span>
        </div>
        <div className="admin-content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
