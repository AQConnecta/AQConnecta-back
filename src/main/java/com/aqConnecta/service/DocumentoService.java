package com.aqConnecta.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentoService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "application/pdf"
    );

    private static final Map<String, String> EXT_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif",
            "application/pdf", "pdf"
    );

    @Value("${file.storage.path:/var/data/uploads}")
    private String storagePath;

    @Value("${file.public.base-url:}")
    private String publicBaseUrl;

    @PostConstruct
    void init() {
        try {
            Path dir = Paths.get(storagePath).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            log.info("Diretório de uploads pronto: {}", dir);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar diretório de uploads: " + storagePath, e);
        }
    }

    public String upload(MultipartFile file) {
        validate(file);

        String filename = buildSafeFilename(file.getOriginalFilename(), file.getContentType());
        Path basePath = Paths.get(storagePath).toAbsolutePath().normalize();
        Path target = basePath.resolve(filename).normalize();

        if (!target.startsWith(basePath)) {
            throw new IllegalArgumentException("Caminho de arquivo inválido");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Falha ao salvar arquivo {} em {}", filename, target, e);
            throw new RuntimeException("Falha ao salvar arquivo", e);
        }

        return buildPublicUrl(filename);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Arquivo excede o tamanho máximo permitido (10MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido");
        }
    }

    private String buildSafeFilename(String originalFilename, String contentType) {
        String safeName = "arquivo";
        if (originalFilename != null && !originalFilename.isBlank()) {
            String base = originalFilename.replaceAll("[\\\\/]", "_");
            base = base.replaceAll("[^A-Za-z0-9._-]", "_");
            int dot = base.lastIndexOf('.');
            if (dot > 0) {
                base = base.substring(0, dot);
            }
            if (!base.isBlank()) {
                safeName = base;
            }
        }
        String ext = EXT_BY_CONTENT_TYPE.getOrDefault(contentType == null ? "" : contentType.toLowerCase(), "bin");
        return UUID.randomUUID() + "_" + safeName + "." + ext;
    }

    private String buildPublicUrl(String filename) {
        String prefix = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (prefix.isBlank()) {
            return "/files/" + filename;
        }
        return prefix.replaceAll("/+$", "") + "/files/" + filename;
    }

    public String uploadEmSubpasta(MultipartFile file, String subpasta) {
        validate(file);

        String filename = buildSafeFilename(file.getOriginalFilename(), file.getContentType());
        Path basePath = Paths.get(storagePath).toAbsolutePath().normalize();
        Path dir = basePath.resolve(subpasta).normalize();
        if (!dir.startsWith(basePath)) {
            throw new IllegalArgumentException("Subpasta inválida");
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar subpasta de uploads", e);
        }

        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("Caminho de arquivo inválido");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Falha ao salvar arquivo {} em {}", filename, target, e);
            throw new RuntimeException("Falha ao salvar arquivo", e);
        }

        return filename;
    }

    public Path resolverArquivo(String subpasta, String filename) {
        String safe = filename == null ? "" : filename;
        if (safe.contains("..") || safe.contains("/") || safe.contains("\\")) {
            throw new IllegalArgumentException("Nome de arquivo inválido");
        }
        Path baseSubpasta = Paths.get(storagePath).resolve(subpasta).toAbsolutePath().normalize();
        Path file = baseSubpasta.resolve(safe).normalize();
        if (!file.startsWith(baseSubpasta)) {
            throw new IllegalArgumentException("Caminho de arquivo inválido");
        }
        return file;
    }

    public void removerArquivo(String subpasta, String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolverArquivo(subpasta, filename));
        } catch (Exception e) {
            log.warn("Falha ao remover arquivo {}/{}: {}", subpasta, filename, e.getMessage());
        }
    }
}
