package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Competencia;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CompetenciaCountDTO {
    @JsonProperty("competencia")
    private Competencia competencia;

    @JsonProperty("count")
    private Long count;
    private Long level;

    public CompetenciaCountDTO(Competencia competencia, Long count) {
        this.competencia = competencia;
        this.count = count;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getLevel() {
        return level;
    }

    public void setLevel(Long level) {
        this.level = level;
    }
}
