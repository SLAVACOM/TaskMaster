package com.slavacom.s3storageservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "s3")
class S3Properties (
    var login: String = "",
    var password: String = "",
    var endpoint: String = "",
    var bucket: String = "",
    var tenant: String = "",
    var region: String = ""
) {
        override fun toString(): String {
            return "S3Properties(login='$login', password='$password', endpoint='$endpoint', bucket='$bucket', tenant='$tenant', region='$region')"
        }
}