package com.slavacom.s3cloudestorage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class S3Config(private val props: S3Properties) {
    @Bean
    fun s3Client(props: S3Properties): S3Client? {
        return S3Client.builder()
            .region(Region.of(props.region))
            .forcePathStyle(true)
            .endpointOverride(URI.create(props.endpoint))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        "${props.tenant}:${props.login}",
                        props.password
                    )
                ))
            .build()
    }



    @Bean
    fun s3Presigner(): S3Presigner? {
        return S3Presigner.builder()
            .region(Region.of(props.region))
            .endpointOverride(URI.create(props.endpoint))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        "${props.tenant}:${props.login}",
                        props.password
                    )
                ))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )
            .build()
    }
}