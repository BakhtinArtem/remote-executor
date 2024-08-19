package com.project.controller;

import com.project.controller.models.RunJarTaskModel;
import com.project.controller.models.SaveRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(Controller.class);


    @Value("${JAR_FILES_PATH}")
    private String JAR_FILES_PATH;

    @Autowired
    private RunnerService runnerService;

    @GetMapping(path = "/v1/jar")
    @ResponseBody
    public ResponseEntity<String[]> getSavedFiles() {
        File jarsDir = new File(JAR_FILES_PATH);
        if (!jarsDir.isDirectory()) {
            logger.error("Given path is not a directory");
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(jarsDir.list());
    }

    @PostMapping(path = "/v1/jar/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> saveJarRequest(@ModelAttribute SaveRequestModel saveRequestModel) {
        final File uploadDir = new File(JAR_FILES_PATH);
        if (!uploadDir.exists()) {
            logger.error("Upload directory does not exists");
            return ResponseEntity.internalServerError().build();
        }

        File destinationFile = new File(uploadDir, saveRequestModel.fileName());
        try {
            saveRequestModel.jar().transferTo(destinationFile);
        } catch (IOException e) {
            logger.error("Error during uploading", e);
            return ResponseEntity.internalServerError().build();
        }
        logger.info("File " + destinationFile.getName() + " correctly saved to " + destinationFile.getPath());
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "v1/jar/run")
    public ResponseEntity<?> runJarTask(@RequestBody RunJarTaskModel runJarTaskModel) {
        try {
            runnerService.runTask(runJarTaskModel, JAR_FILES_PATH);
            return ResponseEntity.ok().build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
