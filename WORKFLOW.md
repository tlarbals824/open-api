# 개발 워크플로우

```
Backend API 개발  →  Swagger JSON 추출  →  API Client 생성 & npm 배포  →  Frontend 사용
```

## 1. 백엔드 API 개발 (Spring Boot + Kotlin)

`backend/src/main/kotlin/com/sim/backend/` 에서 API를 작성합니다.

- **Controller** - `@RestController`로 엔드포인트 정의
- **DTO** - `@get:Schema(requiredMode = REQUIRED)`로 타입 명세
- **Swagger 어노테이션** - `@Tag`, `@Operation`, `@Parameter`로 API 문서화
- **JWT 인증** - `SecurityConfig`에서 인증 필요 여부 설정

## 2. Swagger JSON 자동 추출

서버 기동 시 **springdoc-openapi**가 코드의 어노테이션을 읽어 OpenAPI 3.1 스펙을 자동 생성합니다.

- `http://localhost:8080/v3/api-docs` → `swagger.json`
- `http://localhost:8080/swagger-ui.html` → Swagger UI (브라우저에서 테스트 가능)

## 3. TypeScript API Client 생성 & npm 배포 (GitHub Actions)

**백엔드 코드가 `main` 브랜치에 push되면 GitHub Actions가 자동으로 실행합니다.**

CI/CD 파이프라인 (`.github/workflows/generate-api-client.yml`):

1. 백엔드 clean build
2. 서버 기동 → `swagger.json` 추출
3. **openapi-generator** + 커스텀 템플릿(`openapi-templates/`)으로 TypeScript Axios 클라이언트 생성
4. 백엔드 `build.gradle.kts`의 `version`과 동일한 버전으로 npm 패키지 생성
5. `@test-org/backend-api-client` npm에 자동 publish

## 4. 프론트엔드에서 사용 (Next.js)

npm 패키지 업데이트 후 타입 안전하게 API 호출:

```bash
cd frontend && npm update @test-org/backend-api-client
```

```typescript
import { AuthApi, UserApi, Configuration } from "@test-org/backend-api-client";

// 로그인
const authApi = new AuthApi();
const { data } = await authApi.login({ email: "admin@example.com", password: "password123" });

// 인증된 API 호출
const userApi = new UserApi(new Configuration({ accessToken: data.accessToken }));
const users = await userApi.getAllUsers();
const user = await userApi.getUserById(1);
```

## 요약

| 단계 | 도구 | 산출물 |
|------|------|--------|
| API 개발 | Spring Boot + springdoc | 컨트롤러 + DTO + 어노테이션 |
| 문서 생성 | springdoc-openapi (자동) | `swagger.json` |
| 클라이언트 생성 | openapi-generator + 커스텀 템플릿 | TypeScript Axios 코드 |
| 배포 | npm publish (스크립트) | `@test-org/backend-api-client` |
| 프론트 사용 | npm install | 타입 안전한 API 호출 |

**백엔드 API가 변경되고 `main` 브랜치에 push되면 GitHub Actions가 자동으로 API 클라이언트를 생성하고 npm에 배포합니다. 프론트엔드에서 `npm update`만 하면 타입이 동기화됩니다.**
