import { DefaultApi, Configuration } from "@ject-2-test/backend-api-client";

const BASE_URL = "http://localhost:8080";
const TOKEN_KEY = "access_token";

function isBrowser() {
  return typeof window !== "undefined";
}

export function saveToken(token: string) {
  if (isBrowser()) sessionStorage.setItem(TOKEN_KEY, token);
}

export function getToken(): string | null {
  if (!isBrowser()) return null;
  return sessionStorage.getItem(TOKEN_KEY);
}

export function removeToken() {
  if (isBrowser()) sessionStorage.removeItem(TOKEN_KEY);
}

export function createApi(accessToken?: string) {
  const token = accessToken ?? getToken() ?? undefined;
  const config = new Configuration({
    basePath: BASE_URL,
    accessToken: token,
  });
  return new DefaultApi(config);
}

export const publicApi = createApi();
