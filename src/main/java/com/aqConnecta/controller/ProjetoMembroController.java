package com.aqConnecta.controller;

import com.aqConnecta.DTOs.request.AlterarPapelRequest;
import com.aqConnecta.DTOs.request.ConviteRequest;
import com.aqConnecta.service.ProjetoMembroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projeto")
@RequiredArgsConstructor
@Slf4j
public class ProjetoMembroController {

    private final ProjetoMembroService service;

    @GetMapping("/{idProjeto}/membros")
    public ResponseEntity<Object> listarMembros(
            @PathVariable UUID idProjeto,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listarMembros(idProjeto, authentication.getName(), pageable);
    }

    @GetMapping("/{idProjeto}/membros/buscar")
    public ResponseEntity<Object> buscarUsuarios(@PathVariable UUID idProjeto, @RequestParam("q") String q, Authentication authentication) {
        return service.buscarUsuarios(idProjeto, q, authentication.getName());
    }

    @PostMapping("/{idProjeto}/convites")
    public ResponseEntity<Object> convidar(@PathVariable UUID idProjeto, @Valid @RequestBody ConviteRequest request, Authentication authentication) {
        return service.convidar(idProjeto, request, authentication.getName());
    }

    @GetMapping("/{idProjeto}/convites")
    public ResponseEntity<Object> listarConvitesProjeto(@PathVariable UUID idProjeto, Authentication authentication) {
        return service.listarConvitesProjeto(idProjeto, authentication.getName());
    }

    @PutMapping("/{idProjeto}/membros/{idMembro}/papel")
    public ResponseEntity<Object> alterarPapel(@PathVariable UUID idProjeto, @PathVariable UUID idMembro, @Valid @RequestBody AlterarPapelRequest request, Authentication authentication) {
        return service.alterarPapel(idProjeto, idMembro, request, authentication.getName());
    }

    @DeleteMapping("/{idProjeto}/membros/{idMembro}")
    public ResponseEntity<Object> removerMembro(@PathVariable UUID idProjeto, @PathVariable UUID idMembro, Authentication authentication) {
        return service.removerMembro(idProjeto, idMembro, authentication.getName());
    }

    @PostMapping("/{idProjeto}/membros/{idMembro}/reativar")
    public ResponseEntity<Object> reativarMembro(@PathVariable UUID idProjeto, @PathVariable UUID idMembro, Authentication authentication) {
        return service.reativarMembro(idProjeto, idMembro, authentication.getName());
    }

    @PostMapping("/{idProjeto}/transferir/{idNovoDono}")
    public ResponseEntity<Object> transferir(@PathVariable UUID idProjeto, @PathVariable UUID idNovoDono, Authentication authentication) {
        return service.transferir(idProjeto, idNovoDono, authentication.getName());
    }

    @GetMapping("/convites/meus")
    public ResponseEntity<Object> listarMeusConvites(Authentication authentication) {
        return service.listarMeusConvites(authentication.getName());
    }

    @PostMapping("/convites/{idConvite}/aceitar")
    public ResponseEntity<Object> aceitarConvite(@PathVariable UUID idConvite, Authentication authentication) {
        return service.aceitarConvite(idConvite, authentication.getName());
    }

    @PostMapping("/convites/{idConvite}/recusar")
    public ResponseEntity<Object> recusarConvite(@PathVariable UUID idConvite, Authentication authentication) {
        return service.recusarConvite(idConvite, authentication.getName());
    }
}
