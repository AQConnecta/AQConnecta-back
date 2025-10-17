-- Otimização de índices para cálculo de hot competencies
-- Estes índices melhoram significativamente a performance das queries complexas

-- Índice composto para RL_VAGA_COMPETENCIA com competência e vaga
CREATE INDEX IF NOT EXISTS idx_rl_vaga_competencia_comp_vaga 
ON RL_VAGA_COMPETENCIA (ID_COMPETENCIA, ID_VAGA);

-- Índice para TB_VAGA com data de criação e status de deleção
CREATE INDEX IF NOT EXISTS idx_vaga_criado_deletado 
ON TB_VAGA (CRIADO_EM, DELETADO_EM);

-- Índice composto para TB_VAGA otimizando queries por data
CREATE INDEX IF NOT EXISTS idx_vaga_data_status 
ON TB_VAGA (CRIADO_EM, DELETADO_EM, ID);

-- Índice para TB_COMPETENCIA na descrição (busca por texto)
CREATE INDEX IF NOT EXISTS idx_competencia_descricao 
ON TB_COMPETENCIA (DESCRICAO);

-- Índice para RL_USUARIO_COMPETENCIA
CREATE INDEX IF NOT EXISTS idx_rl_usuario_competencia_comp 
ON RL_USUARIO_COMPETENCIA (ID_COMPETENCIA);

-- Índice para RL_USUARIO_COMPETENCIA por usuário
CREATE INDEX IF NOT EXISTS idx_rl_usuario_competencia_user 
ON RL_USUARIO_COMPETENCIA (ID_USUARIO);

-- View materializada para estatísticas de competências (será atualizada periodicamente)
CREATE OR REPLACE VIEW VW_COMPETENCIA_STATS AS
SELECT 
    TC.ID as competencia_id,
    TC.DESCRICAO as descricao,
    COUNT(DISTINCT TV.ID) as total_vagas,
    COUNT(DISTINCT CASE WHEN TV.CRIADO_EM >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN TV.ID END) as vagas_30_dias,
    COUNT(DISTINCT CASE WHEN TV.CRIADO_EM >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN TV.ID END) as vagas_7_dias,
    MAX(TV.CRIADO_EM) as ultima_vaga,
    MIN(TV.CRIADO_EM) as primeira_vaga,
    COUNT(DISTINCT RUC.ID_USUARIO) as usuarios_com_competencia
FROM TB_COMPETENCIA TC
LEFT JOIN RL_VAGA_COMPETENCIA RVC ON TC.ID = RVC.ID_COMPETENCIA
LEFT JOIN TB_VAGA TV ON RVC.ID_VAGA = TV.ID AND TV.DELETADO_EM IS NULL
LEFT JOIN RL_USUARIO_COMPETENCIA RUC ON TC.ID = RUC.ID_COMPETENCIA
GROUP BY TC.ID, TC.DESCRICAO;

-- Índice na view materializada
CREATE INDEX IF NOT EXISTS idx_vw_competencia_stats_id 
ON VW_COMPETENCIA_STATS (competencia_id);

-- Tabela para cache de hot competencies (será populada por job)
CREATE TABLE IF NOT EXISTS TB_HOT_COMPETENCIES_CACHE (
    ID BINARY(36) NOT NULL PRIMARY KEY,
    COMPETENCIA_ID BINARY(36) NOT NULL,
    TIMEFRAME VARCHAR(20) NOT NULL,
    CATEGORY VARCHAR(20) NOT NULL,
    SCORE DECIMAL(10,4) NOT NULL,
    HOT_LEVEL INT NOT NULL,
    TREND_CATEGORY VARCHAR(20) NOT NULL,
    GROWTH_RATE DECIMAL(10,4) NOT NULL,
    RECENT_COUNT BIGINT NOT NULL,
    TOTAL_COUNT BIGINT NOT NULL,
    LAST_SEEN DATETIME NOT NULL,
    CALCULATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hot_cache_competencia FOREIGN KEY (COMPETENCIA_ID) REFERENCES TB_COMPETENCIA (ID)
);

-- Índices para a tabela de cache
CREATE INDEX IF NOT EXISTS idx_hot_cache_timeframe_category 
ON TB_HOT_COMPETENCIES_CACHE (TIMEFRAME, CATEGORY, CALCULATED_AT);

CREATE INDEX IF NOT EXISTS idx_hot_cache_score 
ON TB_HOT_COMPETENCIES_CACHE (SCORE DESC, CALCULATED_AT);

-- Trigger para invalidar cache quando uma vaga é criada/atualizada/deletada
DELIMITER $$

CREATE TRIGGER IF NOT EXISTS tr_vaga_cache_invalidation 
AFTER INSERT ON TB_VAGA
FOR EACH ROW
BEGIN
    -- Limpar cache quando nova vaga é criada
    DELETE FROM TB_HOT_COMPETENCIES_CACHE 
    WHERE CALCULATED_AT < DATE_SUB(NOW(), INTERVAL 1 HOUR);
END$$

CREATE TRIGGER IF NOT EXISTS tr_vaga_update_cache_invalidation 
AFTER UPDATE ON TB_VAGA
FOR EACH ROW
BEGIN
    -- Limpar cache quando vaga é atualizada
    DELETE FROM TB_HOT_COMPETENCIES_CACHE 
    WHERE CALCULATED_AT < DATE_SUB(NOW(), INTERVAL 1 HOUR);
END$$

CREATE TRIGGER IF NOT EXISTS tr_vaga_delete_cache_invalidation 
AFTER UPDATE ON TB_VAGA
FOR EACH ROW
BEGIN
    -- Limpar cache quando vaga é deletada (soft delete)
    IF OLD.DELETADO_EM IS NULL AND NEW.DELETADO_EM IS NOT NULL THEN
        DELETE FROM TB_HOT_COMPETENCIES_CACHE 
        WHERE CALCULATED_AT < DATE_SUB(NOW(), INTERVAL 1 HOUR);
    END IF;
END$$

DELIMITER ;

-- Stored procedure para calcular e atualizar cache de hot competencies
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS sp_update_hot_competencies_cache()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_competencia_id BINARY(36);
    DECLARE v_descricao VARCHAR(100);
    DECLARE v_total_count BIGINT;
    DECLARE v_recent_count BIGINT;
    DECLARE v_latest_date DATETIME;
    DECLARE v_score DECIMAL(10,4);
    DECLARE v_hot_level INT;
    DECLARE v_trend_category VARCHAR(20);
    DECLARE v_growth_rate DECIMAL(10,4);
    
    -- Cursor para processar competências
    DECLARE competency_cursor CURSOR FOR 
        SELECT 
            TC.ID,
            TC.DESCRICAO,
            COUNT(DISTINCT TV.ID) as total_count,
            COUNT(DISTINCT CASE WHEN TV.CRIADO_EM >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN TV.ID END) as recent_count,
            MAX(TV.CRIADO_EM) as latest_date
        FROM TB_COMPETENCIA TC
        LEFT JOIN RL_VAGA_COMPETENCIA RVC ON TC.ID = RVC.ID_COMPETENCIA
        LEFT JOIN TB_VAGA TV ON RVC.ID_VAGA = TV.ID AND TV.DELETADO_EM IS NULL
        GROUP BY TC.ID, TC.DESCRICAO
        HAVING COUNT(DISTINCT TV.ID) > 0
        ORDER BY recent_count DESC, total_count DESC
        LIMIT 1000;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- Limpar cache antigo
    DELETE FROM TB_HOT_COMPETENCIES_CACHE 
    WHERE CALCULATED_AT < DATE_SUB(NOW(), INTERVAL 2 HOUR);
    
    -- Abrir cursor
    OPEN competency_cursor;
    
    read_loop: LOOP
        FETCH competency_cursor INTO v_competencia_id, v_descricao, v_total_count, v_recent_count, v_latest_date;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Calcular métricas
        SET v_growth_rate = CASE WHEN v_total_count > 0 THEN v_recent_count / v_total_count ELSE 0 END;
        
        SET v_score = (v_recent_count * 0.3) + (v_total_count * 0.4) + (v_growth_rate * 0.3);
        
        SET v_hot_level = CASE 
            WHEN v_score >= 90 THEN 4
            WHEN v_score >= 70 THEN 3
            WHEN v_score >= 50 THEN 2
            WHEN v_score >= 30 THEN 1
            ELSE 0
        END;
        
        SET v_trend_category = CASE 
            WHEN v_growth_rate > 0.5 THEN 'EMERGENTE'
            WHEN v_growth_rate > 0.2 THEN 'ESTÁVEL'
            ELSE 'DECLINANTE'
        END;
        
        -- Inserir no cache para diferentes timeframes
        INSERT INTO TB_HOT_COMPETENCIES_CACHE 
        (ID, COMPETENCIA_ID, TIMEFRAME, CATEGORY, SCORE, HOT_LEVEL, TREND_CATEGORY, GROWTH_RATE, RECENT_COUNT, TOTAL_COUNT, LAST_SEEN)
        VALUES 
        (UUID(), v_competencia_id, 'MONTH', 'ALL', v_score, v_hot_level, v_trend_category, v_growth_rate, v_recent_count, v_total_count, v_latest_date),
        (UUID(), v_competencia_id, 'MONTH', v_trend_category, v_score, v_hot_level, v_trend_category, v_growth_rate, v_recent_count, v_total_count, v_latest_date);
        
    END LOOP;
    
    CLOSE competency_cursor;
    
END$$

DELIMITER ;

-- Event scheduler para atualizar cache automaticamente (se habilitado)
-- CREATE EVENT IF NOT EXISTS ev_update_hot_competencies
-- ON SCHEDULE EVERY 2 HOUR
-- DO CALL sp_update_hot_competencies_cache();
