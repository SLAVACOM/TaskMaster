package com.slavacom.s3cloudestorage.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CashService(
    private val redis: StringRedisTemplate
) {
    private val log = LoggerFactory.getLogger(CashService::class.java)

    fun getFromCache(key: String): String? {
        return tryRead(key)
    }

    fun putToCache(key: String, value: String, ttlSeconds: Long = 300) {
        try {
            redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds))
        } catch (ex: Exception) {
            log.warn("Redis unavailable, skipping cache write", ex)
        }
    }

    private fun tryRead(key: String): String? {
        return try {
            val value = redis.opsForValue().get(key)
            if (value != null) {
                log.debug("Cache hit for key={}", key)
            } else {
                log.debug("Cache miss for key={}", key)
            }
            value
        } catch (ex: Exception) {
            log.warn("Redis unavailable, skipping cache read", ex)
            null
        }
    }
}

