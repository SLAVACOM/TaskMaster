package com.slavacom.s3cloudestorage.controller

import com.slavacom.s3cloudestorage.domain.DownloadFileRequest
import com.slavacom.s3cloudestorage.domain.DownloadFileResponse
import com.slavacom.s3cloudestorage.domain.DownloadFilesRequest
import com.slavacom.s3cloudestorage.domain.UploadFileRequest
import com.slavacom.s3cloudestorage.domain.UploadFileResponse
import com.slavacom.s3cloudestorage.domain.UploadFilesRequest
import com.slavacom.s3cloudestorage.service.S3StorageService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/s3")
class S3Controller(private val s3Service: S3StorageService) {

    private val log = LoggerFactory.getLogger(S3Controller::class.java)


    @PostMapping("/upload-url")
    fun getUploadUrl(@RequestBody @Valid filename: UploadFileRequest): UploadFileResponse {
        log.info("Generate upload URL for {}", filename)

        val data = s3Service.generatePresignedUploadUrl(filename)

        log.debug("Generated upload URL response={}", data)
        return data
    }


    @PostMapping("/upload-urls")
    fun getUploadUrl(@RequestBody @Valid files: UploadFilesRequest): List<UploadFileResponse> {
        log.info("Generate multiple upload URLs, count={}", files.files.size)

        val data = s3Service.generatePresignedUploadUrls(files.files)

        log.debug("Generated {} upload URLs", data)

        return data
    }


    @PostMapping("/download-url")
    fun getDownloadUrl(@RequestBody @Valid key: DownloadFileRequest): DownloadFileResponse {
        log.info("Generate download URL for key={}", key)
        val data = s3Service.generatePresignedDownloadUrl(key)

        log.debug("Generated download URL={}", data)
        return data
    }

    @PostMapping("/download-urls")
    fun getDownloadUrls(@RequestBody @Valid keys: DownloadFilesRequest): List<DownloadFileResponse> {

        log.info("Generate multiple download URLs, count={}", keys.files.size)

        val urls = s3Service.generatePresignedDownloadUrls(keys.files)

        log.debug("Generated download URLs={}", urls)
        return urls
    }
}