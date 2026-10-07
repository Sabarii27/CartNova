import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../services/api';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const validate = () => {
    const e = {};
    if (form.name.trim().length < 2) e.name = 'Enter your full name.';
    if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = 'Enter a valid email address.';
    if (form.password.length < 6) e.password = 'Use at least 6 characters.';
    if (form.confirm !== form.password) e.confirm = 'Passwords do not match.';
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
      await register({ name: form.name.trim(), email: form.email.trim(), password: form.password });
      navigate('/', { replace: true });
    } catch (e) {
      setApiError(getErrorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-card">
      <h1>Create your account</h1>
      <p className="muted">It takes less than a minute.</p>
      {apiError && <div className="alert alert-error" role="alert">{apiError}</div>}
      <form onSubmit={submit} noValidate>
        <label className="field">
          <span>Full name</span>
          <input autoComplete="name" value={form.name} onChange={set('name')} />
          {errors.name && <small className="field-error">{errors.name}</small>}
        </label>
        <label className="field">
          <span>Email</span>
          <input type="email" autoComplete="email" value={form.email} onChange={set('email')} />
          {errors.email && <small className="field-error">{errors.email}</small>}
        </label>
        <label className="field">
          <span>Password</span>
          <input type="password" autoComplete="new-password" value={form.password} onChange={set('password')} />
          {errors.password && <small className="field-error">{errors.password}</small>}
        </label>
        <label className="field">
          <span>Confirm password</span>
          <input type="password" autoComplete="new-password" value={form.confirm} onChange={set('confirm')} />
          {errors.confirm && <small className="field-error">{errors.confirm}</small>}
        </label>
        <button className="btn btn-primary btn-block btn-lg" disabled={busy}>{busy ? 'Creating account...' : 'Create account'}</button>
      </form>
      <p className="auth-alt">Already registered? <Link to="/login">Sign in</Link></p>
    </div>
  );
}
