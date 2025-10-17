package com.aqConnecta.DTOs.response;

import com.aqConnecta.model.Competencia;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO avançado para competências "quentes" com métricas detalhadas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotCompetencyDTO {
    
    @JsonProperty("competencia")
    private Competencia competencia;
    
    @JsonProperty("count")
    private Long count;
    
    @JsonProperty("recentCount")
    private Long recentCount;
    
    @JsonProperty("score")
    private Double score;
    
    @JsonProperty("hotLevel")
    private Integer hotLevel;
    
    @JsonProperty("trendCategory")
    private String trendCategory;
    
    @JsonProperty("growthRate")
    private Double growthRate;
    
    @JsonProperty("lastSeen")
    private LocalDateTime lastSeen;
    
    @JsonProperty("confidence")
    private Double confidence;
    
    @JsonProperty("prediction")
    private String prediction;
    
    // Getters e setters
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
    
    public Long getRecentCount() {
        return recentCount;
    }
    
    public void setRecentCount(Long recentCount) {
        this.recentCount = recentCount;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public Integer getHotLevel() {
        return hotLevel;
    }
    
    public void setHotLevel(Integer hotLevel) {
        this.hotLevel = hotLevel;
    }
    
    public String getTrendCategory() {
        return trendCategory;
    }
    
    public void setTrendCategory(String trendCategory) {
        this.trendCategory = trendCategory;
    }
    
    public Double getGrowthRate() {
        return growthRate;
    }
    
    public void setGrowthRate(Double growthRate) {
        this.growthRate = growthRate;
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public String getPrediction() {
        return prediction;
    }
    
    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }
}
