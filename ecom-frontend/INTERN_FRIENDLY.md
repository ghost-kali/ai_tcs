# Intern-friendly map (quick review)

If you're presenting this in a review, these are the only files you usually need:

- `src/app/AppRouter.jsx` — all app routes in one place
- `src/App.jsx` — thin wrapper (keeps `App.css` global styles)
- `src/main.jsx` — app bootstrap + Redux provider

## Where features live

- Customer pages: `src/components/home`, `src/components/products`, `src/components/cart`, `src/components/checkout`, `src/components/profile`
- Auth: `src/components/auth`
- Admin: `src/components/admin/*`
- Shared UI: `src/components/shared/*` (mostly thin wrappers around MUI)

## Small cleanup done

- Checkout now reuses the shared delete confirmation modal:
  - `src/components/shared/DeleteModal.jsx` (single source of truth)
  - `src/components/checkout/DeleteModal.jsx` (re-export shim to avoid breaking imports)
