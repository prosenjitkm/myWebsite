// Production environment injected at build time.
// BACKEND_URL is replaced before the production build.
export const environment = {
  production: true,
  // This placeholder is replaced by the actual backend URL.
  apiUrl: '__BACKEND_URL__'
};
