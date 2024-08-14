package com.aqConnecta.controller;

import com.aqConnecta.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documento")
@RequiredArgsConstructor
@Slf4j
public class DocumentoController {

    private final DocumentoService documentService;

    @PostMapping("/upload")
    public void saveImage(@RequestParam("file") MultipartFile file) {
        documentService.upload(file);
    }

}