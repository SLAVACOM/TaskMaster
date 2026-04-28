package com.slavacom.s3storageservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class S3StorageServiceApplication

fun main(args: Array<String>) {
    runApplication<S3StorageServiceApplication>(*args)
}
