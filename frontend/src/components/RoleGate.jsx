function RoleGate({ user, roles, children, fallback = null }) {
  if (!user || !roles.includes(user.role)) {
    return fallback;
  }

  return children;
}

export default RoleGate;
