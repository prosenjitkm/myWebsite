// Production environment — injected at build time via angular.json fileReplacements
// BACKEND_URL is replaced by the actual Cloud Run backend URL during `ng build --configuration production`
export const environment = {
  production: true,
  // This placeholder is replaced by the actual Cloud Run backend URL.
  // In CI/CD the ng build command passes --define or uses fileReplacements to substitute.
  apiUrl: '__BACKEND_URL__'
};


