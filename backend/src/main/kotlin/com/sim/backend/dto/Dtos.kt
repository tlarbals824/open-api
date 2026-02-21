package com.sim.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED

@Schema(description = "로그인 요청")
data class LoginRequest(
    @get:Schema(description = "사용자 이메일", example = "admin@example.com", requiredMode = REQUIRED)
    val email: String,
    @get:Schema(description = "비밀번호", example = "password123", requiredMode = REQUIRED)
    val password: String,
)

@Schema(description = "로그인 응답")
data class LoginResponse(
    @get:Schema(description = "JWT 액세스 토큰", requiredMode = REQUIRED)
    val accessToken: String,
    @get:Schema(description = "토큰 타입", example = "Bearer", requiredMode = REQUIRED)
    val tokenType: String = "Bearer",
)

@Schema(description = "사용자 정보")
data class UserResponse(
    @get:Schema(description = "사용자 ID", example = "1", requiredMode = REQUIRED)
    val id: Long,
    @get:Schema(description = "사용자 이름", example = "홍길동", requiredMode = REQUIRED)
    val name: String,
    @get:Schema(description = "사용자 이메일", example = "admin@example.com", requiredMode = REQUIRED)
    val email: String,
    @get:Schema(description = "역할", example = "ADMIN", requiredMode = REQUIRED)
    val role: String,
)
