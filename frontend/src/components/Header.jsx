function Header({ user, currentView, onNavigate, onLogout }) {
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', protected: true },
    { id: 'events', label: 'Events' }
  ];

  return (
    <header className="app-header">
      <button className="brand-button" type="button" onClick={() => onNavigate('landing')}>
        Locked In For The Career Center
      </button>

      <nav className="nav-links" aria-label="Main navigation">
        {navItems.map((item) => (
          (!item.protected || user) && (
            <button
              key={item.id}
              type="button"
              className={currentView === item.id ? 'nav-link active' : 'nav-link'}
              onClick={() => onNavigate(item.id)}
            >
              {item.label}
            </button>
          )
        ))}

        {user?.role === 'ADMIN' && (
          <button
            type="button"
            className={currentView === 'create-event' ? 'nav-link active' : 'nav-link'}
            onClick={() => onNavigate('create-event')}
          >
            Create Event
          </button>
        )}
      </nav>

      <div className="auth-nav">
        {user ? (
          <>
            <span>{user.firstName} {user.lastName}</span>
            <button type="button" onClick={onLogout}>Logout</button>
          </>
        ) : (
          <>
            <button type="button" onClick={() => onNavigate('login')}>Login</button>
            <button type="button" className="secondary-button" onClick={() => onNavigate('register')}>
              Register
            </button>
          </>
        )}
      </div>
    </header>
  );
}

export default Header;
