package com.sim.backend.controller

import com.sim.backend.dto.LoginRequest
import com.sim.backend.dto.LoginResponse
import com.sim.backend.security.JwtProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtProvider: JwtProvider,
) {
    // 간단한 하드코딩 유저 (DB 없이)
    private val validUsers = mapOf(
        "admin@example.com" to "password123",
        "user@example.com" to "password123",
    )

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val storedPassword = validUsers[request.email]
            ?: return ResponseEntity.status(401).build()

        if (storedPassword != request.password) {
            return ResponseEntity.status(401).build()
        }

        val token = jwtProvider.generateToken(request.email)
        return ResponseEntity.ok(LoginResponse(accessToken = token))
    }
}
