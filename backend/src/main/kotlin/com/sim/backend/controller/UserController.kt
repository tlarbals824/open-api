package com.sim.backend.controller

import com.sim.backend.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
class UserController {

    // 간단한 인메모리 유저 목록
    private val users = listOf(
        UserResponse(id = 1, name = "관리자", email = "admin@example.com", role = "ADMIN"),
        UserResponse(id = 2, name = "홍길동", email = "user@example.com", role = "USER"),
        UserResponse(id = 3, name = "김철수", email = "chulsoo@example.com", role = "USER"),
    )

    @Operation(summary = "사용자 전체 조회", description = "등록된 모든 사용자 목록을 조회합니다.")
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(users)
    }

    @Operation(summary = "사용자 단건 조회", description = "ID로 특정 사용자를 조회합니다.")
    @GetMapping("/{id}")
    fun getUserById(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable id: Long,
    ): ResponseEntity<UserResponse> {
        val user = users.find { it.id == id }
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user)
    }
}
