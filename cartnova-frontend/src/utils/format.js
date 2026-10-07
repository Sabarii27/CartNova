const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 });

export const formatPrice = (value) => money.format(Number(value || 0));

export const formatDate = (iso) =>
  iso
    ? new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
    : '-';

export const formatDateTime = (iso) =>
  iso
    ? new Date(iso).toLocaleString('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    : '-';

export const ORDER_STATUSES = ['PLACED', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
export const CANCELLABLE = ['PLACED', 'CONFIRMED'];

export const PLACEHOLDER_IMG =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300" viewBox="0 0 400 300"><rect width="400" height="300" fill="#e6efee"/><g fill="none" stroke="#0b6e6e" stroke-width="6" stroke-linecap="round" stroke-linejoin="round" opacity=".45"><rect x="150" y="95" width="100" height="95" rx="10"/><path d="M175 95v-12a25 25 0 0 1 50 0v12"/></g></svg>`
  );

export const onImgError = (e) => {
  e.currentTarget.onerror = null;
  e.currentTarget.src = PLACEHOLDER_IMG;
};
