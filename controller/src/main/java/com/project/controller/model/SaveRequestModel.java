package com.project.controller.model;

import org.springframework.web.multipart.MultipartFile;

public record SaveRequestModel(MultipartFile jar, String fileName) {
}
