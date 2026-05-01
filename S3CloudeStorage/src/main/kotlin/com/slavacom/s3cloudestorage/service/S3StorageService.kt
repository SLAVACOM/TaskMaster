package com.slavacom.s3cloudestorage.service

import com.slavacom.s3cloudestorage.config.S3Properties
import com.slavacom.s3cloudestorage.domain.DownloadFileRequest
import com.slavacom.s3cloudestorage.domain.DownloadFileResponse
import com.slavacom.s3cloudestorage.domain.UploadFileRequest
import com.slavacom.s3cloudestorage.domain.UploadFileResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.time.LocalDate
import java.util.*

@Service
class S3StorageService(
    private val s3Presigner: S3Presigner,
    private val props: S3Properties,
    private val cash: CashService
) {
    private val log = LoggerFactory.getLogger(S3StorageService::class.java)

    fun generatePresignedDownloadUrl(file: DownloadFileRequest, expiresMinutes: Long = 10): DownloadFileResponse {
        log.info("Generating presigned download URL for key={} with {} minutes expiration", file, expiresMinutes)

        val redisKey = generateCacheKey(file, expiresMinutes)

        cash.getFromCache(redisKey)?.let { cachedUrl ->
            log.debug("Returning cached presigned URL for key={}", file)
            return DownloadFileResponse(file.fileName, cachedUrl)
        }


        s3Presigner.use { resigned ->

            val getObjectRequest = GetObjectRequest.builder()
                .bucket(props.bucket)
                .key(file.fileName)
                .build()

            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiresMinutes))
                .getObjectRequest(getObjectRequest)
                .build()

            val url = resigned.presignGetObject(presignRequest).url().toString()
            log.debug("Generated download URL: {}", url)

            cash.putToCache(redisKey, url, expiresMinutes * 60)

            return DownloadFileResponse(file.fileName, url)
        }
    }

    fun generatePresignedDownloadUrls(
        keys: List<DownloadFileRequest>,
        expiresMinutes: Long = 10
    ): List<DownloadFileResponse> {
        log.info("Generating presigned download URLs for {} keys", keys.toString())

        val urls = keys.map { key -> generatePresignedDownloadUrl(key, expiresMinutes) }
        return urls
    }

    fun generatePresignedUploadUrl(
        file: UploadFileRequest,
        expiresMinutes: Long = 10
    ): UploadFileResponse {
        log.info(
            "Generating presigned upload URL for file={} (user={}, category={})",
            file.originalFilename,
            file.userId,
            file.category
        )

        s3Presigner.use { resigned ->

            val key = generateS3Key(file)
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(props.bucket)
                .key(key)
                .build()

            val presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiresMinutes))
                .putObjectRequest(putObjectRequest)
                .build()

            val url = resigned.presignPutObject(presignRequest).url().toString()

            val response = UploadFileResponse(key, url)
            log.debug("Generated presigned upload URL: {}", response)

            return response
        }
    }

    fun generatePresignedUploadUrls(
        keys: List<UploadFileRequest>,
        expiresMinutes: Long = 10
    ): List<UploadFileResponse> {

        log.info("Generating presigned upload URLs for {}, expires={}", keys, expiresMinutes)

        val urls = keys.map { key -> generatePresignedUploadUrl(key) }

        log.debug("Generated {} presigned upload URLs", urls)

        return urls
    }

    private fun generateS3Key(
        file: UploadFileRequest
    ): String {
        val date = LocalDate.now().toString()
        val extension = file.originalFilename.substringAfterLast('.', "")
        val baseName = file.originalFilename.substringBeforeLast('.')
        val uuid = UUID.randomUUID().toString()

        val filename = if (extension.isEmpty()) "$baseName-$uuid" else "$baseName-$uuid.$extension"
        val key = "${file.category}-category/$date/user-${file.userId}/$filename"

        log.debug("Generated S3 key: {}", key)
        return "${file.category}-category/$date/user-${file.userId}/$filename"
    }

    private fun generateCacheKey(file: DownloadFileRequest, expiresMinutes: Long): String {
        return "presigned:download:${file.fileName}:expires:$expiresMinutes"
    }


}