package com.slavacom.organizationservice.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                val claims = jwtTokenProvider.extractClaims(authHeader)
                val userId = claims["userId"]?.toString()
                val role = claims["role"]?.toString()

                if (userId != null && SecurityContextHolder.getContext().authentication == null) {
                    val authorities = if (role != null) listOf(SimpleGrantedAuthority("ROLE_$role")) else emptyList()
                    val auth = UsernamePasswordAuthenticationToken(userId, null, authorities)
                    SecurityContextHolder.getContext().authentication = auth
                }
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }
}
