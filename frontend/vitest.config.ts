import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    coverage: {
      provider: 'v8',
      reportsDirectory: './coverage',
      reporter: ['lcov', 'text-summary'],
      reportOnFailure: true,
    },
  },
});

