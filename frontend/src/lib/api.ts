import { AuthApi, UserApi, Configuration } from "@ject-2-test/backend-api-client";

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

function createConfig(accessToken?: string) {
  const token = accessToken ?? getToken() ?? undefined;
  return new Configuration({
    basePath: BASE_URL,
    accessToken: token,
  });
}

export function createAuthApi(accessToken?: string) {
  return new AuthApi(createConfig(accessToken));
}

export function createUserApi(accessToken?: string) {
  return new UserApi(createConfig(accessToken));
}

export const publicAuthApi = createAuthApi();
