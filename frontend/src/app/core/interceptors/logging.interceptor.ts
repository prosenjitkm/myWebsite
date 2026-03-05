import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { tap, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

/**
 * Logs every outgoing HTTP request and its response/error to the browser console.
 *
 * SUCCESS → console.log  (collapsed group, not noisy)
 * ERROR   → console.error (always visible, with full details)
 *
 * Active in ALL environments — remove or guard with !isDevMode() before prod if desired.
 */
export const loggingInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const start = Date.now();
  const label = `[HTTP] ${req.method} ${req.url}`;

  console.groupCollapsed(label);
  console.log('Headers:', req.headers.keys().reduce((acc, k) => {
    // mask the Authorization token value for security
    acc[k] = k.toLowerCase() === 'authorization' ? '***' : req.headers.get(k);
    return acc;
  }, {} as Record<string, string | null>));
  if (req.body) console.log('Body:', req.body);
  console.groupEnd();

  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse) {
        const ms = Date.now() - start;
        console.log(`[HTTP] ✔ ${req.method} ${req.url} → ${event.status} (${ms}ms)`);
      }
    }),
    catchError((err: HttpErrorResponse) => {
      const ms = Date.now() - start;
      console.group(`[HTTP] ✖ ${req.method} ${req.url} → ${err.status} (${ms}ms)`);
      console.error('Status :', err.status, err.statusText);
      console.error('URL    :', err.url);
      console.error('Error  :', err.error);
      console.error('Message:', err.message);
      console.groupEnd();
      return throwError(() => err);
    })
  );
};

