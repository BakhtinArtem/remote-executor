package com.project.runner.worker.model;


import org.springframework.web.multipart.MultipartFile;

public record ExecuteRequestModel(MultipartFile jar, String fileName, String mainClassName, String mainMethodName, String[] args) {
}
