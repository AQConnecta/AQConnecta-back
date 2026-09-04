package com.aqConnecta.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${file.storage.path:/var/data/uploads}")
    private String storagePath;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String filename) {
        String safe = StringUtils.cleanPath(filename);
        if (safe.contains("..") || safe.contains("/") || safe.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path basePath = Paths.get(storagePath).toAbsolutePath().normalize();
        Path file = basePath.resolve(safe).normalize();

        if (!file.startsWith(basePath) || !Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(file.toUri());
            String mime = probeContentType(file);
            MediaType mediaType = mime != null ? MediaType.parseMediaType(mime) : MediaType.APPLICATION_OCTET_STREAM;
            long contentLength = Files.size(file);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(contentLength)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safe + "\"")
                    .header("X-Content-Type-Options", "nosniff")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.warn("Arquivo com URI inválida: {}", file, e);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Erro lendo arquivo {}", file, e);
            return ResponseEntity.status(500).build();
        }
    }

    private String probeContentType(Path file) {
        try {
            String mime = Files.probeContentType(file);
            if (mime != null) return mime;
        } catch (IOException ignored) {
        }
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".gif"))  return "image/gif";
        if (name.endsWith(".pdf"))  return "application/pdf";
        return null;
    }
}
