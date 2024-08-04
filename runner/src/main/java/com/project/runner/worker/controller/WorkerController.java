package com.project.runner.worker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping()
public class WorkerController {

    private static final String UPLOAD_DIR = "/jarDir";

    @GetMapping("/hello")
    public ResponseEntity<String> getHello() {
        return ResponseEntity.ok().body("HELLO FROM WORKER 1.0.1");
    }

    @PostMapping("/execute-jar")
    public ResponseEntity<String> executeJar(@RequestParam("file") MultipartFile file, @RequestParam("name") String name) {
        try {
            final File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                return ResponseEntity.internalServerError().body("Upload directory does not exists");
            }

            File destinationFile = new File(uploadDir, name);
            file.transferTo(destinationFile);

            return ResponseEntity.ok().body("File uploaded successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
