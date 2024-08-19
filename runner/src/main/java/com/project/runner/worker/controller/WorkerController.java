package com.project.runner.worker.controller;

import com.project.runner.util.CustomLoader;
import com.project.runner.worker.model.ExecuteRequestModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@RestController
@RequestMapping()
public class WorkerController {

    private static final String UPLOAD_DIR = "/jarDir";

    @PostMapping("/v1/code/execution")
    public ResponseEntity<String> executeCode() {
        return ResponseEntity.badRequest().body("Method is not implemented");
    }

//    same as in postman -> load file to cloud and then choose from dropdown, so we may use on jar multiple times
    @PostMapping(path = "/v1/jar/execution", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> executeJar(@ModelAttribute ExecuteRequestModel executeRequestModel) {
        try {
//            file saving service
            final File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                return ResponseEntity.internalServerError().body("Upload directory does not exists");
            }

//            todo: use for code execution
            File destinationFile = new File(uploadDir, executeRequestModel.fileName());
            executeRequestModel.jar().transferTo(destinationFile);
//            executing jar
            URLClassLoader urlClassLoader = new CustomLoader(new URL[]{destinationFile.toURI().toURL()});
            Class<?> clazz = urlClassLoader.loadClass(executeRequestModel.mainClassName());
            Object main = clazz.getDeclaredConstructor().newInstance();
//            support only this signature
            Method test = clazz.getMethod(executeRequestModel.mainMethodName(), String[].class);
            String[] mainMethodArgs = executeRequestModel.args() == null ? new String[]{} : executeRequestModel.args();
            test.invoke(main, (Object) mainMethodArgs);

            return ResponseEntity.ok().body("File uploaded successfully");
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
