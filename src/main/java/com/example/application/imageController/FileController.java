package com.example.application.imageController;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/files")
public class FileController {

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws IOException {
        Path path = Paths.get("src/FILES/songs").resolve(filename).normalize();

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());

        // Ustalenie dynamicznego typu MIME
        String mimeType = Files.probeContentType(path);
        MediaType contentType = (mimeType != null) ?
                MediaType.parseMediaType(mimeType) :
                MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }
}
