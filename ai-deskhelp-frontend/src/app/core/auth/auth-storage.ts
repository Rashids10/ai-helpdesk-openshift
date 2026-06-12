const AUTH_TOKEN_KEYS = ['accessToken', 'token', 'jwt'] as const;

export function getStoredAuthToken(): string | null {
  for (const key of AUTH_TOKEN_KEYS) {
    const token = localStorage.getItem(key);

    if (token?.trim()) {
      return token;
    }
  }

  return null;
}

export function clearStoredAuthTokens(): void {
  for (const key of AUTH_TOKEN_KEYS) {
    localStorage.removeItem(key);
  }
}

export function isUnexpiredJwt(token: string): boolean {
  const payload = token.split('.')[1];

  if (!payload) {
    return false;
  }

  try {
    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
    const decodedPayload = JSON.parse(atob(normalizedPayload)) as { exp?: unknown };

    return (
      typeof decodedPayload.exp === 'number' &&
      decodedPayload.exp * 1000 > Date.now()
    );
  } catch {
    return false;
  }
}
