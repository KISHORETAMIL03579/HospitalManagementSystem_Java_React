package com.careflow.hospitalmanagement.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Hidden
@RestController
public class SwaggerAssetsController {

    @GetMapping("/swagger-ui/{filename:.+}")
    public ResponseEntity<byte[]> getSwaggerAsset(@PathVariable String filename) throws IOException {
        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        ClassPathResource resource = new ClassPathResource("static/swagger-ui/" + filename);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (filename.endsWith(".css")) {
            mediaType = MediaType.valueOf("text/css");
        } else if (filename.endsWith(".js")) {
            mediaType = MediaType.valueOf("application/javascript");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource.getInputStream().readAllBytes());
    }
}
