import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import ProductCard from '../components/ProductCard';
import Pagination from '../components/Pagination';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import useFetch from '../hooks/useFetch';
import { getCategories, getProducts } from '../services/productService';

const PAGE_SIZE = 8;

export default function Products() {
  const [params, setParams] = useSearchParams();
  const search = params.get('search') || '';
  const category = params.get('category') || '';
  const minPrice = params.get('minPrice') || '';
  const maxPrice = params.get('maxPrice') || '';
  const available = params.get('available') || '';
  const sort = params.get('sort') || 'createdAt,desc';
  const page = Number(params.get('page') || 0);

  // Local form state so typing does not fire a request per keystroke
  const [form, setForm] = useState({ search, minPrice, maxPrice });
  useEffect(() => setForm({ search, minPrice, maxPrice }), [search, minPrice, maxPrice]);

  const categories = useFetch(() => getCategories(), []);
  const products = useFetch(
    () => getProducts({ search, category, minPrice, maxPrice, available, sort, page, size: PAGE_SIZE }),
    [search, category, minPrice, maxPrice, available, sort, page]
  );

  const update = (changes) => {
    const next = new URLSearchParams(params);
    Object.entries(changes).forEach(([k, v]) => (v === '' || v === null ? next.delete(k) : next.set(k, v)));
    if (!('page' in changes)) next.delete('page');
    setParams(next);
  };

  const applyFilters = (e) => {
    e.preventDefault();
    update({ search: form.search.trim(), minPrice: form.minPrice, maxPrice: form.maxPrice });
  };

  const hasFilters = search || category || minPrice || maxPrice || available;
  const list = products.data?.content || [];

  return (
    <div className="shop-layout">
      <aside className="filters">
        <form onSubmit={applyFilters}>
          <h3>Filters</h3>
          <label className="field">
            <span>Search</span>
            <input value={form.search} onChange={(e) => setForm({ ...form, search: e.target.value })} placeholder="Laptop, shirt..." />
          </label>
          <label className="field">
            <span>Category</span>
            <select value={category} onChange={(e) => update({ category: e.target.value })}>
              <option value="">All categories</option>
              {(categories.data || []).map((c) => (
                <option key={c.id} value={c.name}>{c.name}</option>
              ))}
            </select>
          </label>
          <div className="field-row">
            <label className="field">
              <span>Min price</span>
              <input type="number" min="0" value={form.minPrice} onChange={(e) => setForm({ ...form, minPrice: e.target.value })} />
            </label>
            <label className="field">
              <span>Max price</span>
              <input type="number" min="0" value={form.maxPrice} onChange={(e) => setForm({ ...form, maxPrice: e.target.value })} />
            </label>
          </div>
          <label className="check">
            <input type="checkbox" checked={available === 'true'} onChange={(e) => update({ available: e.target.checked ? 'true' : '' })} />
            In stock only
          </label>
          <button className="btn btn-primary btn-block" type="submit">Apply filters</button>
          {hasFilters && (
            <button type="button" className="btn btn-outline btn-block" onClick={() => setParams({})}>
              Clear all
            </button>
          )}
        </form>
      </aside>

      <section className="shop-results">
        <div className="shop-bar">
          <h1>{category || 'All products'}</h1>
          <label className="sort">
            <span>Sort by</span>
            <select value={sort} onChange={(e) => update({ sort: e.target.value })}>
              <option value="createdAt,desc">Newest</option>
              <option value="price,asc">Price: low to high</option>
              <option value="price,desc">Price: high to low</option>
              <option value="name,asc">Name: A to Z</option>
            </select>
          </label>
        </div>

        {products.loading && <Loader label="Loading products..." />}
        <ErrorMessage message={products.error} onRetry={products.reload} />

        {!products.loading && !products.error && list.length === 0 && (
          <EmptyState title="No products match" text="Try removing a filter or searching for something else.">
            {hasFilters && <button className="btn btn-primary" onClick={() => setParams({})}>Clear filters</button>}
          </EmptyState>
        )}

        {!products.loading && list.length > 0 && (
          <>
            <p className="muted">{products.data.totalElements} products</p>
            <div className="product-grid">
              {list.map((p) => <ProductCard key={p.id} product={p} />)}
            </div>
            <Pagination page={page} totalPages={products.data.totalPages} onChange={(p) => update({ page: String(p) })} />
          </>
        )}
      </section>
    </div>
  );
}
