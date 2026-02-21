"use client";

import { useState, useEffect } from "react";
import { createApi, publicApi, saveToken, getToken, removeToken } from "@/lib/api";
import type { UserResponse, LoginResponse } from "@ject-2-test/backend-api-client";

export default function Home() {
  const [token, setToken] = useState<string | null>(null);
  const [loginResult, setLoginResult] = useState<LoginResponse | null>(null);
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [singleUser, setSingleUser] = useState<UserResponse | null>(null);
  const [userId, setUserId] = useState("1");
  const [error, setError] = useState<string | null>(null);

  // 페이지 로드 시 세션 스토리지에서 토큰 복원
  useEffect(() => {
    const saved = getToken();
    if (saved) {
      setToken(saved);
      setLoginResult({ accessToken: saved, tokenType: "Bearer" });
    }
  }, []);

  const handleLogin = async () => {
    try {
      setError(null);
      const { data } = await publicApi.login({
        email: "admin@example.com",
        password: "password123",
      });
      const accessToken = data.accessToken ?? null;
      setToken(accessToken);
      setLoginResult(data);
      if (accessToken) saveToken(accessToken);
    } catch {
      setError("로그인 실패");
    }
  };

  const handleLogout = () => {
    removeToken();
    setToken(null);
    setLoginResult(null);
    setUsers([]);
    setSingleUser(null);
  };

  const handleGetAllUsers = async () => {
    if (!token) return setError("먼저 로그인해주세요");
    try {
      setError(null);
      const { data } = await createApi().getAllUsers();
      setUsers(data);
    } catch {
      setError("사용자 목록 조회 실패");
    }
  };

  const handleGetUser = async () => {
    if (!token) return setError("먼저 로그인해주세요");
    try {
      setError(null);
      const { data } = await createApi().getUserById(Number(userId));
      setSingleUser(data);
    } catch {
      setError("사용자 조회 실패");
    }
  };

  return (
    <main style={{ maxWidth: 640, margin: "0 auto", padding: 32, fontFamily: "sans-serif" }}>
      <h1 style={{ fontSize: 24, fontWeight: "bold", marginBottom: 24 }}>API Client Demo</h1>

      {error && (
        <div style={{ background: "#fee", color: "#c00", padding: 12, borderRadius: 8, marginBottom: 16 }}>
          {error}
        </div>
      )}

      {/* 로그인 */}
      <section style={{ marginBottom: 32, padding: 16, border: "1px solid #ddd", borderRadius: 8 }}>
        <h2 style={{ fontSize: 18, fontWeight: "bold", marginBottom: 12 }}>1. 로그인</h2>
        <p style={{ fontSize: 14, color: "#666", marginBottom: 8 }}>admin@example.com / password123</p>
        <div style={{ display: "flex", gap: 8 }}>
          <button onClick={handleLogin} style={btnStyle}>
            로그인
          </button>
          {token && (
            <button onClick={handleLogout} style={{ ...btnStyle, background: "#666" }}>
              로그아웃
            </button>
          )}
        </div>
        {loginResult && (
          <pre style={preStyle}>
            {JSON.stringify(loginResult, null, 2)}
          </pre>
        )}
      </section>

      {/* 사용자 전체 조회 */}
      <section style={{ marginBottom: 32, padding: 16, border: "1px solid #ddd", borderRadius: 8 }}>
        <h2 style={{ fontSize: 18, fontWeight: "bold", marginBottom: 12 }}>2. 사용자 전체 조회</h2>
        <button onClick={handleGetAllUsers} style={btnStyle}>
          GET /api/users
        </button>
        {users.length > 0 && (
          <pre style={preStyle}>
            {JSON.stringify(users, null, 2)}
          </pre>
        )}
      </section>

      {/* 사용자 단건 조회 */}
      <section style={{ padding: 16, border: "1px solid #ddd", borderRadius: 8 }}>
        <h2 style={{ fontSize: 18, fontWeight: "bold", marginBottom: 12 }}>3. 사용자 단건 조회</h2>
        <div style={{ display: "flex", gap: 8, marginBottom: 8 }}>
          <input
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="User ID"
            style={{ padding: "8px 12px", border: "1px solid #ccc", borderRadius: 4, width: 80 }}
          />
          <button onClick={handleGetUser} style={btnStyle}>
            GET /api/users/{"{id}"}
          </button>
        </div>
        {singleUser && (
          <pre style={preStyle}>
            {JSON.stringify(singleUser, null, 2)}
          </pre>
        )}
      </section>
    </main>
  );
}

const btnStyle: React.CSSProperties = {
  padding: "8px 16px",
  background: "#0070f3",
  color: "#fff",
  border: "none",
  borderRadius: 6,
  cursor: "pointer",
  fontSize: 14,
};

const preStyle: React.CSSProperties = {
  marginTop: 12,
  padding: 12,
  background: "#f5f5f5",
  borderRadius: 6,
  fontSize: 13,
  overflow: "auto",
};
