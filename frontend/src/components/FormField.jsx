function FormField({
  label,
  name,
  type = 'text',
  value,
  onChange,
  required = false,
  placeholder = '',
  children,
  min
}) {
  return (
    <label>
      {label}
      {children || (
        <input
          type={type}
          name={name}
          value={value}
          onChange={onChange}
          required={required}
          placeholder={placeholder}
          min={min}
        />
      )}
    </label>
  );
}

export default FormField;
