import { useState } from 'react';
import { Link } from 'react-router-dom';
import Loader from '../../components/Loader';
import ErrorMessage from '../../components/ErrorMessage';
import EmptyState from '../../components/EmptyState';
import Pagination from '../../components/Pagination';
import useFetch from '../../hooks/useFetch';
import { getCategories, getProducts } from '../../services/productService';
import { deleteProduct } from '../../services/adminService';
import { getErrorMessage } from '../../services/api';
import { formatPrice, onImgError, PLACEHOLDER_IMG } from '../../utils/format';

const SIZE = 10;

export default function AdminProducts() {
  const [search, setSearch] = useState('');
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [available, setAvailable] = useState('');
  const [page, setPage] = useState(0);
  const [actionError, setActionError] = useState('');

  const cats = useFetch(() => getCategories(), []);
  const { data, loading, error, reload } = useFetch(
    () => getProducts({ search: query, category, available, page, size: SIZE, sort: 'createdAt,desc' }),
    [query, category, available, page]
  );

  const onSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setQuery(search.trim());
  };

  const remove = async (p) => {
    if (!window.confirm(`Delete "${p.name}"? This cannot be undone.`)) return;
    setActionError('');
    try {
      await deleteProduct(p.id);
      reload();
    } catch (e) {
      setActionError(getErrorMessage(e));
    }
  };

  const list = data?.content || [];

  return (
    <div>
      <div className="page-head">
        <h1>Products</h1>
        <Link to="/admin/products/new" className="btn btn-primary">Add product</Link>
      </div>

      <form className="toolbar" onSubmit={onSearch}>
        <input placeholder="Search by name" value={search} onChange={(e) => setSearch(e.target.value)} aria-label="Search products" />
        <select value={category} onChange={(e) => { setPage(0); setCategory(e.target.value); }} aria-label="Category">
          <option value="">All categories</option>
          {(cats.data || []).map((c) => <option key={c.id} value={c.name}>{c.name}</option>)}
        </select>
        <select value={available} onChange={(e) => { setPage(0); setAvailable(e.target.value); }} aria-label="Availability">
          <option value="">Any stock</option>
          <option value="true">In stock</option>
          <option value="false">Out of stock</option>
        </select>
        <button className="btn btn-outline">Search</button>
      </form>

      <ErrorMessage message={actionError} />
      {loading && <Loader />}
      <ErrorMessage message={error} onRetry={reload} />
      {!loading && !error && list.length === 0 && <EmptyState title="No products found" text="Add your first product or change the filters." />}

      {list.length > 0 && (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Product</th><th>Category</th><th className="num">Price</th><th className="num">Stock</th><th className="num">Actions</th></tr></thead>
              <tbody>
                {list.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <div className="cell-product">
                        <img src={p.imageUrl || PLACEHOLDER_IMG} alt="" onError={onImgError} />
                        <span>{p.name}</span>
                      </div>
                    </td>
                    <td>{p.categoryName}</td>
                    <td className="num">{formatPrice(p.price)}</td>
                    <td className="num"><span className={p.stock === 0 ? 'text-danger' : p.stock <= 5 ? 'text-warn' : ''}>{p.stock}</span></td>
                    <td className="num actions">
                      <Link to={`/admin/products/${p.id}/edit`} className="btn btn-sm btn-outline">Edit</Link>
                      <button className="btn btn-sm btn-danger" onClick={() => remove(p)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
