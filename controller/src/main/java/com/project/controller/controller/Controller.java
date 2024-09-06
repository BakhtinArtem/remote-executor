package com.project.controller.controller;

import com.project.controller.entity.Execution;
import com.project.controller.entity.Graph;
import com.project.controller.entity.Node;
import com.project.controller.exception.CycleDetectedException;
import com.project.controller.exception.MultiComponentDetectedException;
import com.project.controller.model.GraphInput;
import com.project.controller.model.RunJarTaskModel;
import com.project.controller.model.SaveRequestModel;
import com.project.controller.service.EdgeService;
import com.project.controller.service.GraphService;
import com.project.controller.service.NodeService;
import com.project.controller.service.RunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    @Value("${JAR_FILES_PATH}")
    private String JAR_FILES_PATH;

    @Autowired
    private RunnerService runnerService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private EdgeService edgeService;

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

    @PostMapping(path = "/v1/jar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
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

//    todo test method: delete method
    @PostMapping(path = "v1/jar/run")
    public ResponseEntity<?> runJarTask(@RequestBody RunJarTaskModel runJarTaskModel) {
//        try {
//            this should be configurable (image, input parameters etc.)
//            runnerService.runTask(runJarTaskModel, JAR_FILES_PATH);
            return ResponseEntity.ok().build();
//        } catch (IOException ex) {
//            return ResponseEntity.internalServerError().build();
//        }
    }

    @QueryMapping
    public Execution executeGraph(@Argument Long graphId) { return graphService.executeGraph(graphId); }

    @QueryMapping
    public Graph graphById(@Argument String id) {
        return graphService.graphById(Long.valueOf(id));
    }

    @MutationMapping
    public Graph createGraph(@Argument GraphInput input) throws CycleDetectedException, MultiComponentDetectedException {
        return graphService.createGraph(input);
    }

    @SchemaMapping
    public List<Node> node(Graph graph) {
        return graphService.getGraphNodes(graph.getId());
    }
}
