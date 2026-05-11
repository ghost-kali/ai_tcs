import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography,
} from '@mui/material'

export function InputField({
  label,
  id,
  type,
  errors,
  register,
  required,
  message,
  className,
  min,
  placeholder,
}) {
  const errorMessage = errors?.[id]?.message

  return (
    <TextField
      className={className}
      label={label}
      type={type}
      placeholder={placeholder}
      fullWidth
      size='small'
      error={Boolean(errorMessage)}
      helperText={errorMessage || ' '}
      {...register(id, {
        required: { value: required, message },
        minLength: min
          ? { value: min, message: `Minimum ${min} character is required` }
          : undefined,
        pattern:
          type === 'email'
            ? {
                value: /^[a-zA-Z0-9]+@(?:[a-zA-Z0-9]+\.)+com+$/,
                message: 'Invalid email',
              }
            : type === 'url'
              ? {
                  value:
                    /^(https?:\/\/)?(([a-zA-Z0-9\u00a1-\uffff-]+\.)+[a-zA-Z\u00a1-\uffff]{2,})(:\d{2,5})?(\/[^\s]*)?$/,
                  message: 'Please enter a valid url',
                }
              : undefined,
      })}
    />
  )
}

export function SelectTextField({ label, select, setSelect, lists }) {
  return (
    <FormControl fullWidth size='small'>
      <InputLabel>{label}</InputLabel>
      <Select
        label={label}
        value={select || ''}
        onChange={(e) => setSelect(e.target.value)}
        renderValue={(val) =>
          val?.categoryName ? (
            val.categoryName
          ) : (
            <Typography color='text.secondary'>Select</Typography>
          )
        }
      >
        {lists?.map((category) => (
          <MenuItem key={category.categoryId} value={category}>
            {category.categoryName}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  )
}

