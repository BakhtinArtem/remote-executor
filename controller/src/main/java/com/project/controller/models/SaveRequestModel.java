package com.project.controller.models;

import org.springframework.web.multipart.MultipartFile;

public record SaveRequestModel(MultipartFile jar, String fileName) {
}
