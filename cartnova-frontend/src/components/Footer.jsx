import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div>
          <strong>CartNova</strong>
          <p>A full-stack e-commerce project built with React and Spring Boot.</p>
        </div>
        <div className="footer-links">
          <Link to="/products">Shop all</Link>
          <Link to="/orders">My orders</Link>
          <Link to="/profile">Profile</Link>
        </div>
      </div>
      <div className="container footer-copy">&copy; {new Date().getFullYear()} CartNova</div>
    </footer>
  );
}
