import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Loader from '../../components/Loader';
import ErrorMessage from '../../components/ErrorMessage';
import useFetch from '../../hooks/useFetch';
import { getCategories, getProduct } from '../../services/productService';
import { createProduct, updateProduct } from '../../services/adminService';
import { getErrorMessage } from '../../services/api';

const EMPTY = { name: '', description: '', price: '', stock: '', imageUrl: '', categoryId: '' };

export default function AdminProductForm() {
  const { id } = useParams();
  const editing = !!id;
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [busy, setBusy] = useState(false);

  const cats = useFetch(() => getCategories(), []);
  const existing = useFetch(() => (editing ? getProduct(id) : Promise.resolve(null)), [id]);

  useEffect(() => {
    const p = existing.data;
    if (p) {
      setForm({
        name: p.name, description: p.description, price: String(p.price), stock: String(p.stock),
        imageUrl: p.imageUrl || '', categoryId: String(p.categoryId),
      });
    }
  }, [existing.data]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const validate = () => {
    const e = {};
    if (form.name.trim().length < 2) e.name = 'Name must be at least 2 characters.';
    if (form.description.trim().length < 10) e.description = 'Description must be at least 10 characters.';
    if (!(Number(form.price) > 0)) e.price = 'Price must be greater than 0.';
    if (form.stock === '' || !Number.isInteger(Number(form.stock)) || Number(form.stock) < 0) e.stock = 'Stock must be 0 or more.';
    if (!form.categoryId) e.categoryId = 'Choose a category.';
    return e;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    const v = validate();
    setErrors(v);
    if (Object.keys(v).length) return;
    setBusy(true);
    setApiError('');
    const payload = {
      name: form.name.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      stock: Number(form.stock),
      imageUrl: form.imageUrl.trim() || null,
      categoryId: Number(form.categoryId),
    };
    try {
      if (editing) await updateProduct(id, payload);
      else await createProduct(payload);
      navigate('/admin/products');
    } catch (e) {
      setApiError(getErrorMessage(e));
      setBusy(false);
    }
  };

  if (existing.loading || cats.loading) return <Loader />;
  if (existing.error) return <ErrorMessage message={existing.error} onRetry={existing.reload} />;

  return (
    <div>
      <p className="breadcrumb"><Link to="/admin/products">Products</Link> / {editing ? 'Edit' : 'New'}</p>
      <h1>{editing ? 'Edit product' : 'Add product'}</h1>
      <ErrorMessage message={cats.error} onRetry={cats.reload} />
      {apiError && <div className="alert alert-error" role="alert">{apiError}</div>}
      <form className="panel form-grid" onSubmit={submit} noValidate>
        <label className="field span-2">
          <span>Name</span>
          <input value={form.name} onChange={set('name')} />
          {errors.name && <small className="field-error">{errors.name}</small>}
        </label>
        <label className="field span-2">
          <span>Description</span>
          <textarea rows="4" value={form.description} onChange={set('description')} />
          {errors.description && <small className="field-error">{errors.description}</small>}
        </label>
        <label className="field">
          <span>Price (INR)</span>
          <input type="number" step="0.01" min="0" value={form.price} onChange={set('price')} />
          {errors.price && <small className="field-error">{errors.price}</small>}
        </label>
        <label className="field">
          <span>Stock</span>
          <input type="number" min="0" step="1" value={form.stock} onChange={set('stock')} />
          {errors.stock && <small className="field-error">{errors.stock}</small>}
        </label>
        <label className="field">
          <span>Category</span>
          <select value={form.categoryId} onChange={set('categoryId')}>
            <option value="">Select category</option>
            {(cats.data || []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          {errors.categoryId && <small className="field-error">{errors.categoryId}</small>}
        </label>
        <label className="field">
          <span>Image URL (optional)</span>
          <input value={form.imageUrl} onChange={set('imageUrl')} placeholder="https://..." />
        </label>
        <div className="span-2 form-actions">
          <button className="btn btn-primary btn-lg" disabled={busy}>{busy ? 'Saving...' : editing ? 'Save changes' : 'Create product'}</button>
          <Link to="/admin/products" className="btn btn-outline btn-lg">Cancel</Link>
        </div>
      </form>
    </div>
  );
}
