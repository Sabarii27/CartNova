import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../services/api';

export default function Login() {
  const { login, sessionMessage, clearSessionMessage } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [busy, setBusy] = useState(false);

  const validate = () => {
    const e = {};
    if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'Enter a valid email address.';
    if (!form.password) e.password = 'Enter your password.';
    return e;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    const v = validate();
    setErrors(v);
    if (Object.keys(v).length) return;
    setBusy(true);
    setApiError('');
    try {
      const user = await login({ email: form.email.trim(), password: form.password });
      clearSessionMessage();
      navigate(user.role === 'ADMIN' ? '/admin' : location.state?.from || '/', { replace: true });
    } catch (e) {
      setApiError(e.response?.status === 401 ? 'Email or password is incorrect.' : getErrorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-card">
      <h1>Welcome back</h1>
      <p className="muted">Sign in to see your cart and orders.</p>
      {sessionMessage && <div className="alert alert-info">{sessionMessage}</div>}
      {apiError && <div className="alert alert-error" role="alert">{apiError}</div>}
      <form onSubmit={submit} noValidate>
        <label className="field">
          <span>Email</span>
          <input type="email" autoComplete="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          {errors.email && <small className="field-error">{errors.email}</small>}
        </label>
        <label className="field">
          <span>Password</span>
          <input type="password" autoComplete="current-password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          {errors.password && <small className="field-error">{errors.password}</small>}
        </label>
        <button className="btn btn-primary btn-block btn-lg" disabled={busy}>{busy ? 'Signing in...' : 'Sign in'}</button>
      </form>
      <p className="auth-alt">New to CartNova? <Link to="/register">Create an account</Link></p>
    </div>
  );
}
