package com.aqConnecta.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço para métricas e monitoramento do sistema de hot competencies
 */
@Slf4j
@Service
public class HotCompetenciesMetricsService {
    
    private final ConcurrentHashMap<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> responseTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lastCalculations = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    @Autowired
    private HotCompetenciesService hotCompetenciesService;
    
    /**
     * Registra uma requisição para hot competencies
     */
    public void recordRequest(String timeframe, String category, long responseTimeMs) {
        String key = timeframe + "_" + category;
        
        requestCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        responseTimes.put(key + "_time", new AtomicLong(responseTimeMs));
        lastCalculations.put(key, LocalDateTime.now());
        totalRequests.incrementAndGet();
        
        log.debug("Requisição registrada: {} em {}ms", key, responseTimeMs);
    }
    
    /**
     * Registra um erro no sistema
     */
    public void recordError(String timeframe, String category, Exception error) {
        String key = timeframe + "_" + category;
        totalErrors.incrementAndGet();
        
        log.error("Erro registrado para {}: {}", key, error.getMessage());
    }
    
    /**
     * Obtém métricas de performance
     */
    public HotCompetenciesMetrics getMetrics() {
        HotCompetenciesMetrics metrics = new HotCompetenciesMetrics();
        
        metrics.totalRequests = totalRequests.get();
        metrics.totalErrors = totalErrors.get();
        metrics.errorRate = metrics.totalRequests > 0 ? 
            (double) metrics.totalErrors / metrics.totalRequests : 0.0;
        
        // Calcular tempo médio de resposta
        long totalResponseTime = responseTimes.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
        metrics.averageResponseTime = responseTimes.size() > 0 ? 
            totalResponseTime / responseTimes.size() : 0;
        
        // Estatísticas por timeframe/categoria
        metrics.requestCountsByKey = new ConcurrentHashMap<>();
        requestCounts.forEach((key, count) -> 
            metrics.requestCountsByKey.put(key, count.get()));
        
        metrics.lastCalculations = new ConcurrentHashMap<>(lastCalculations);
        
        return metrics;
    }
    
    /**
     * Limpa métricas antigas
     */
    public void cleanupOldMetrics() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        lastCalculations.entrySet().removeIf(entry -> 
            entry.getValue().isBefore(cutoff));
        
        log.info("Métricas antigas removidas. Entradas ativas: {}", lastCalculations.size());
    }
    
    /**
     * Classe para encapsular métricas
     */
    public static class HotCompetenciesMetrics {
        public long totalRequests;
        public long totalErrors;
        public double errorRate;
        public long averageResponseTime;
        public ConcurrentHashMap<String, Long> requestCountsByKey;
        public ConcurrentHashMap<String, LocalDateTime> lastCalculations;
        
        @Override
        public String toString() {
            return String.format(
                "HotCompetenciesMetrics{totalRequests=%d, totalErrors=%d, errorRate=%.2f%%, avgResponseTime=%dms}",
                totalRequests, totalErrors, errorRate * 100, averageResponseTime
            );
        }
    }
}
