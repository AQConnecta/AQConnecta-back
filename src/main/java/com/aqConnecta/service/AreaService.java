package com.aqConnecta.service;

import com.aqConnecta.DTOs.request.AreaRequest;
import com.aqConnecta.DTOs.response.ResponseHandler;
import com.aqConnecta.model.Area;
import com.aqConnecta.repository.AreaRepository;
import com.aqConnecta.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AreaService {

    private final AreaRepository areaRepository;
    private final ProjetoRepository projetoRepository;

    public ResponseEntity<Object> listar(String search, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Area> areaPage = (search == null || search.isEmpty())
                    ? areaRepository.findAll(pageable)
                    : areaRepository.findByDescricaoContainingIgnoreCase(search, pageable);
            return ResponseHandler.generateResponse("Listagem feita com sucesso!", HttpStatus.OK, areaPage.getContent());
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Houve um erro ao listar as áreas.", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<Object> cadastrar(AreaRequest registro) {
        try {
            if (!areaRepository.findByDescricaoIgnoreCase(registro.getDescricao()).isEmpty()) {
                return ResponseHandler.generateResponse("Essa área já existe!", HttpStatus.CONFLICT, null);
            }
            Area area = Area.builder().descricao(registro.getDescricao()).build();
            area = areaRepository.saveAndFlush(area);
            return ResponseHandler.generateResponse("Área cadastrada com sucesso!", HttpStatus.CREATED, area);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> alterar(UUID idArea, AreaRequest registro) {
        try {
            Optional<Area> area = areaRepository.findById(idArea);
            if (area.isEmpty()) {
                return ResponseHandler.generateResponse("Área não encontrada!", HttpStatus.NOT_FOUND);
            }
            if (!areaRepository.findByDescricaoIgnoreCase(registro.getDescricao()).isEmpty()) {
                return ResponseHandler.generateResponse("Essa área já existe!", HttpStatus.CONFLICT, null);
            }
            Area alterada = area.get();
            alterada.setDescricao(registro.getDescricao());
            areaRepository.save(alterada);
            return ResponseHandler.generateResponse("Área alterada com sucesso!", HttpStatus.OK, alterada);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deletar(UUID idArea) {
        try {
            Optional<Area> area = areaRepository.findById(idArea);
            if (area.isEmpty()) {
                return ResponseHandler.generateResponse("Área não encontrada!", HttpStatus.NOT_FOUND);
            }
            if (projetoRepository.countByAreaId(idArea) > 0) {
                return ResponseHandler.generateResponse("Não é possível excluir uma área em uso por projetos.", HttpStatus.CONFLICT);
            }
            areaRepository.deleteById(idArea);
            return ResponseHandler.generateResponse("Área deletada com sucesso!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.generateResponse(String.format("Error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
