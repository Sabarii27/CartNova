export default function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  const pages = Array.from({ length: totalPages }, (_, i) => i);
  const visible = pages.filter((p) => p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 1);

  return (
    <nav className="pagination" aria-label="Pagination">
      <button className="btn btn-sm btn-outline" disabled={page === 0} onClick={() => onChange(page - 1)}>
        Previous
      </button>
      {visible.map((p, i) => (
        <span key={p} className="page-group">
          {i > 0 && p - visible[i - 1] > 1 && <span className="page-gap">...</span>}
          <button
            className={`btn btn-sm ${p === page ? 'btn-primary' : 'btn-outline'}`}
            aria-current={p === page ? 'page' : undefined}
            onClick={() => onChange(p)}
          >
            {p + 1}
          </button>
        </span>
      ))}
      <button className="btn btn-sm btn-outline" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Next
      </button>
    </nav>
  );
}
