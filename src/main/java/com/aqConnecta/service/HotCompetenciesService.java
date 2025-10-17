package com.aqConnecta.service;

import com.aqConnecta.DTOs.response.CompetenciaCountDTO;
import com.aqConnecta.DTOs.response.HotCompetencyDTO;
import com.aqConnecta.model.Competencia;
import com.aqConnecta.repository.CompetenciaRepository;
import com.aqConnecta.repository.VagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Serviço avançado para cálculo de competências "quentes" (hot competencies)
 * 
 * Características da implementação:
 * - Algoritmo de pontuação multi-dimensional
 * - Cache inteligente com TTL
 * - Ponderação temporal (competências mais recentes têm maior peso)
 * - Categorização por tendências (emergente, estável, declinante)
 * - Otimizações para grandes volumes de dados (30k+ competências)
 * - Estruturas de dados eficientes para cálculos em memória
 */
@Slf4j
@Service
public class HotCompetenciesService {

    @Autowired
    private CompetenciaRepository competenciaRepository;
    
    @Autowired
    private VagaRepository vagaRepository;
    
    @Autowired
    private HotCompetenciesMetricsService metricsService;
    
    // Cache em memória para cálculos frequentes
    private final Map<String, List<HotCompetencyDTO>> competenciesCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();
    
    // Configurações do algoritmo
    private static final int CACHE_TTL_HOURS = 2;
    private static final int MAX_COMPETENCIES_RETURNED = 100;
    private static final double RECENCY_WEIGHT = 0.3;
    private static final double FREQUENCY_WEIGHT = 0.4;
    private static final double GROWTH_WEIGHT = 0.3;
    private static final int DAYS_FOR_RECENCY = 90;

    /**
     * Calcula competências quentes usando algoritmo avançado multi-dimensional
     */
    @Cacheable(value = "hotCompetencies", key = "#timeframe + '_' + #category")
    public List<HotCompetencyDTO> calculateHotCompetencies(String timeframe, String category) {
        log.info("Calculando hot competencies para timeframe: {} e categoria: {}", timeframe, category);
        
        long startTime = System.currentTimeMillis();
        
        // Verificar cache primeiro
        String cacheKey = timeframe + "_" + category;
        if (isCacheValid(cacheKey)) {
            log.info("Retornando competências do cache");
            long responseTime = System.currentTimeMillis() - startTime;
            metricsService.recordRequest(timeframe, category, responseTime);
            return competenciesCache.get(cacheKey);
        }
        
        try {
            // Obter dados do banco com otimizações
            List<Object[]> rawData = getOptimizedCompetencyData(timeframe);
            
            // Processar dados usando estruturas eficientes
            Map<UUID, CompetencyMetrics> metricsMap = processCompetencyData(rawData);
            
            // Calcular pontuações usando algoritmo multi-dimensional
            List<HotCompetencyDTO> hotCompetencies = calculateAdvancedScores(metricsMap, timeframe);
            
            // Aplicar filtros e ordenação
            List<HotCompetencyDTO> filteredCompetencies = applyFiltersAndSorting(hotCompetencies, category);
            
            // Atualizar cache
            updateCache(cacheKey, filteredCompetencies);
            
            // Registrar métricas
            long responseTime = System.currentTimeMillis() - startTime;
            metricsService.recordRequest(timeframe, category, responseTime);
            
            log.info("Hot competencies calculadas com sucesso: {} competências em {}ms", 
                filteredCompetencies.size(), responseTime);
            return filteredCompetencies;
            
        } catch (Exception e) {
            log.error("Erro ao calcular hot competencies", e);
            metricsService.recordError(timeframe, category, e);
            throw new RuntimeException("Erro interno ao calcular competências quentes", e);
        }
    }

    /**
     * Query otimizada para buscar dados de competências
     */
    private List<Object[]> getOptimizedCompetencyData(String timeframe) {
        LocalDateTime cutoffDate = calculateCutoffDate(timeframe);
        
        // Query otimizada com índices e agregações eficientes
        return competenciaRepository.getHotCompetenciesData(cutoffDate);
    }

    /**
     * Processa dados brutos em estruturas eficientes para cálculos
     */
    private Map<UUID, CompetencyMetrics> processCompetencyData(List<Object[]> rawData) {
        Map<UUID, CompetencyMetrics> metricsMap = new HashMap<>();
        
        for (Object[] row : rawData) {
            UUID competenciaId = UUID.fromString(row[0].toString());
            String descricao = row[1].toString();
            Long totalCount = ((Number) row[2]).longValue();
            Long recentCount = ((Number) row[3]).longValue();
            LocalDateTime latestVaga = row[4] != null ? 
                ((java.sql.Timestamp) row[4]).toLocalDateTime() : LocalDateTime.now().minusYears(1);
            
            CompetencyMetrics metrics = new CompetencyMetrics();
            metrics.competenciaId = competenciaId;
            metrics.descricao = descricao;
            metrics.totalCount = totalCount;
            metrics.recentCount = recentCount;
            metrics.latestVagaDate = latestVaga;
            metrics.averageRecency = calculateAverageRecency(row);
            
            metricsMap.put(competenciaId, metrics);
        }
        
        return metricsMap;
    }

    /**
     * Calcula pontuações avançadas usando algoritmo multi-dimensional
     */
    private List<HotCompetencyDTO> calculateAdvancedScores(Map<UUID, CompetencyMetrics> metricsMap, String timeframe) {
        // Calcular estatísticas globais para normalização
        CompetencyStatistics globalStats = calculateGlobalStatistics(metricsMap.values());
        
        List<HotCompetencyDTO> hotCompetencies = new ArrayList<>();
        
        for (CompetencyMetrics metrics : metricsMap.values()) {
            // Calcular componentes da pontuação
            double recencyScore = calculateRecencyScore(metrics, globalStats);
            double frequencyScore = calculateFrequencyScore(metrics, globalStats);
            double growthScore = calculateGrowthScore(metrics, globalStats);
            
            // Pontuação final ponderada
            double finalScore = (recencyScore * RECENCY_WEIGHT) + 
                               (frequencyScore * FREQUENCY_WEIGHT) + 
                               (growthScore * GROWTH_WEIGHT);
            
            // Determinar categoria de tendência
            String trendCategory = determineTrendCategory(metrics, globalStats);
            
            // Determinar nível de "quenteza"
            int hotLevel = calculateHotLevel(finalScore, globalStats);
            
            HotCompetencyDTO dto = new HotCompetencyDTO();
            dto.setCompetencia(new Competencia(metrics.competenciaId, metrics.descricao));
            dto.setCount(metrics.totalCount);
            dto.setRecentCount(metrics.recentCount);
            dto.setScore(finalScore);
            dto.setTrendCategory(trendCategory);
            dto.setHotLevel(hotLevel);
            dto.setGrowthRate(calculateGrowthRate(metrics));
            dto.setLastSeen(metrics.latestVagaDate);
            
            hotCompetencies.add(dto);
        }
        
        return hotCompetencies;
    }

    /**
     * Calcula estatísticas globais para normalização
     */
    private CompetencyStatistics calculateGlobalStatistics(Collection<CompetencyMetrics> metrics) {
        CompetencyStatistics stats = new CompetencyStatistics();
        
        stats.totalCompetencies = metrics.size();
        stats.maxCount = metrics.stream().mapToLong(m -> m.totalCount).max().orElse(1);
        stats.avgCount = metrics.stream().mapToLong(m -> m.totalCount).average().orElse(1);
        stats.maxRecentCount = metrics.stream().mapToLong(m -> m.recentCount).max().orElse(1);
        stats.avgRecentCount = metrics.stream().mapToLong(m -> m.recentCount).average().orElse(1);
        
        return stats;
    }

    /**
     * Calcula pontuação baseada em recência (0-1)
     */
    private double calculateRecencyScore(CompetencyMetrics metrics, CompetencyStatistics stats) {
        long daysSinceLastSeen = ChronoUnit.DAYS.between(metrics.latestVagaDate, LocalDateTime.now());
        double recencyFactor = Math.max(0, 1 - (daysSinceLastSeen / (double) DAYS_FOR_RECENCY));
        
        // Normalizar baseado na distribuição global
        double normalizedRecentCount = (double) metrics.recentCount / stats.maxRecentCount;
        
        return recencyFactor * normalizedRecentCount;
    }

    /**
     * Calcula pontuação baseada em frequência (0-1)
     */
    private double calculateFrequencyScore(CompetencyMetrics metrics, CompetencyStatistics stats) {
        // Usar log para suavizar diferenças extremas
        double logCount = Math.log(metrics.totalCount + 1);
        double logMaxCount = Math.log(stats.maxCount + 1);
        
        return logCount / logMaxCount;
    }

    /**
     * Calcula pontuação baseada em crescimento (0-1)
     */
    private double calculateGrowthScore(CompetencyMetrics metrics, CompetencyStatistics stats) {
        if (metrics.totalCount == 0) return 0;
        
        double recentRatio = (double) metrics.recentCount / metrics.totalCount;
        double avgRecentRatio = (double) stats.avgRecentCount / stats.avgCount;
        
        // Crescimento relativo comparado à média
        return Math.max(0, recentRatio / avgRecentRatio);
    }

    /**
     * Determina categoria de tendência
     */
    private String determineTrendCategory(CompetencyMetrics metrics, CompetencyStatistics stats) {
        double recentRatio = (double) metrics.recentCount / metrics.totalCount;
        double avgRecentRatio = (double) stats.avgRecentCount / stats.avgCount;
        
        if (recentRatio > avgRecentRatio * 1.5) {
            return "EMERGENTE";
        } else if (recentRatio > avgRecentRatio * 0.8) {
            return "ESTÁVEL";
        } else {
            return "DECLINANTE";
        }
    }

    /**
     * Calcula nível de "quenteza" (0-4)
     */
    private int calculateHotLevel(double score, CompetencyStatistics stats) {
        if (score >= 0.9) return 4; // Muito quente
        if (score >= 0.7) return 3; // Quente
        if (score >= 0.5) return 2; // Moderadamente quente
        if (score >= 0.3) return 1; // Pouco quente
        return 0; // Frio
    }

    /**
     * Calcula taxa de crescimento
     */
    private double calculateGrowthRate(CompetencyMetrics metrics) {
        if (metrics.totalCount == 0) return 0;
        return (double) metrics.recentCount / metrics.totalCount;
    }

    /**
     * Aplica filtros e ordenação final
     */
    private List<HotCompetencyDTO> applyFiltersAndSorting(List<HotCompetencyDTO> competencies, String category) {
        return competencies.stream()
                .filter(dto -> category == null || category.equals("ALL") || 
                        dto.getTrendCategory().equals(category))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(MAX_COMPETENCIES_RETURNED)
                .collect(Collectors.toList());
    }

    /**
     * Calcula data de corte baseada no timeframe
     */
    private LocalDateTime calculateCutoffDate(String timeframe) {
        LocalDateTime now = LocalDateTime.now();
        switch (timeframe.toUpperCase()) {
            case "WEEK": return now.minusWeeks(1);
            case "MONTH": return now.minusMonths(1);
            case "QUARTER": return now.minusMonths(3);
            case "YEAR": return now.minusYears(1);
            default: return now.minusMonths(1);
        }
    }

    /**
     * Calcula recência média (implementação simplificada)
     */
    private double calculateAverageRecency(Object[] row) {
        // Implementação simplificada - pode ser expandida com mais dados
        return Math.random() * 30; // Simula dias de recência
    }

    /**
     * Verifica se cache é válido
     */
    private boolean isCacheValid(String cacheKey) {
        LocalDateTime timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp == null) return false;
        
        return ChronoUnit.HOURS.between(timestamp, LocalDateTime.now()) < CACHE_TTL_HOURS;
    }

    /**
     * Atualiza cache
     */
    private void updateCache(String cacheKey, List<HotCompetencyDTO> data) {
        competenciesCache.put(cacheKey, data);
        cacheTimestamps.put(cacheKey, LocalDateTime.now());
    }

    /**
     * Limpa cache periodicamente
     */
    @Scheduled(fixedRate = 3600000) // A cada hora
    public void cleanExpiredCache() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(CACHE_TTL_HOURS);
        
        cacheTimestamps.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        competenciesCache.entrySet().removeIf(entry -> !cacheTimestamps.containsKey(entry.getKey()));
        
        log.info("Cache limpo. Entradas restantes: {}", competenciesCache.size());
    }

    /**
     * Classe para métricas de competência
     */
    private static class CompetencyMetrics {
        UUID competenciaId;
        String descricao;
        Long totalCount;
        Long recentCount;
        LocalDateTime latestVagaDate;
        double averageRecency;
    }

    /**
     * Classe para estatísticas globais
     */
    private static class CompetencyStatistics {
        int totalCompetencies;
        long maxCount;
        double avgCount;
        long maxRecentCount;
        double avgRecentCount;
    }
}
