import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import useFetch from '../hooks/useFetch';
import { getProfile } from '../services/authService';
import { formatDate } from '../utils/format';

export default function Profile() {
  const { data, loading, error, reload } = useFetch(() => getProfile(), []);
  if (loading) return <Loader />;
  if (error) return <ErrorMessage message={error} onRetry={reload} />;
  if (!data) return null;

  return (
    <div className="profile">
      <div className="avatar" aria-hidden="true">{data.name.charAt(0).toUpperCase()}</div>
      <h1>{data.name}</h1>
      <dl className="profile-list">
        <div><dt>Email</dt><dd>{data.email}</dd></div>
        <div><dt>Account type</dt><dd>{data.role === 'ADMIN' ? 'Administrator' : 'Customer'}</dd></div>
        <div><dt>Member since</dt><dd>{formatDate(data.createdAt)}</dd></div>
      </dl>
    </div>
  );
}
