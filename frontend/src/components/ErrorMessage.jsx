function ErrorMessage({ message }) {
  if (!message) {
    return null;
  }

  return <p className="error-text">{message}</p>;
}

export default ErrorMessage;
