import { useMemo, useState } from 'react';
import Loader from '../../components/Loader';
import ErrorMessage from '../../components/ErrorMessage';
import EmptyState from '../../components/EmptyState';
import useFetch from '../../hooks/useFetch';
import { getUser, getUsers } from '../../services/adminService';
import { getErrorMessage } from '../../services/api';
import { formatDateTime } from '../../utils/format';

export default function AdminUsers() {
  const { data, loading, error, reload } = useFetch(() => getUsers(), []);
  const [q, setQ] = useState('');
  const [selected, setSelected] = useState(null);
  const [detailError, setDetailError] = useState('');

  const rows = useMemo(() => {
    const term = q.trim().toLowerCase();
    return (data || []).filter((u) => !term || u.name.toLowerCase().includes(term) || u.email.toLowerCase().includes(term));
  }, [data, q]);

  const open = async (id) => {
    setDetailError('');
    try {
      setSelected(await getUser(id));
    } catch (e) {
      setDetailError(getErrorMessage(e));
    }
  };

  return (
    <div>
      <h1>Users</h1>
      <div className="toolbar">
        <input placeholder="Search by name or email" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search users" />
      </div>
      <ErrorMessage message={detailError} />
      {loading && <Loader />}
      <ErrorMessage message={error} onRetry={reload} />
      {!loading && !error && rows.length === 0 && <EmptyState title="No users found" text="Try a different search." />}
      {rows.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th className="num">Details</th></tr></thead>
            <tbody>
              {rows.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td><span className={`badge ${u.role === 'ADMIN' ? 'badge-processing' : 'badge-confirmed'}`}>{u.role}</span></td>
                  <td>{formatDateTime(u.createdAt)}</td>
                  <td className="num"><button className="btn btn-sm btn-outline" onClick={() => open(u.id)}>View</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selected && (
        <div className="modal-backdrop" onClick={() => setSelected(null)}>
          <div className="modal" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
            <h3>{selected.name}</h3>
            <dl className="profile-list">
              <div><dt>User ID</dt><dd>{selected.id}</dd></div>
              <div><dt>Email</dt><dd>{selected.email}</dd></div>
              <div><dt>Role</dt><dd>{selected.role}</dd></div>
              <div><dt>Joined</dt><dd>{formatDateTime(selected.createdAt)}</dd></div>
            </dl>
            <button className="btn btn-primary" onClick={() => setSelected(null)}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}
