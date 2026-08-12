package com.aqConnecta.repository.specs;

import com.aqConnecta.model.Vaga;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class VagaSpecs {
    public static Specification<Vaga> possuiTitulo(String titulo) {
        return (root, query, cb) -> Strings.isEmpty(titulo) ? null :
            cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%");
    }

    public static Specification<Vaga> possuiCompetencia(UUID idCompetencia) {
        return (root, query, cb) -> idCompetencia == null ? null :
            cb.equal(root.join("competencias").get("id"), idCompetencia);
    }

    public static Specification<Vaga> isIniciante(Boolean iniciante) {
        return (root, query, cb) -> iniciante == null ? null :
            cb.equal(root.get("isIniciante"), iniciante);
    }

    public static Specification<Vaga> naoDeletada() {
        return (root, query, cb) -> cb.isNull(root.get("deletadoEm"));
    }

    public static Specification<Vaga> dentroDoPrazo() {
        return (root, query, cb) -> {
            var now = Instant.now();
            return cb.or(
                cb.isNull(root.get("dataLimiteCandidatura")),
                cb.greaterThan(root.get("dataLimiteCandidatura"), now)
            );
        };
    }
}