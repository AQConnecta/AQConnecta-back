UPDATE TB_USUARIO
    SET USER_URL = LOWER(REPLACE(NOME, ' ', '-'))
    WHERE USER_URL IS NULL;

-- substitui todos os valores USER_URL iguais por uma variação concatenada ao ID do usuário,
-- removendo qualquer possível duplicata para que o campo possa ter restrição de unicidade
UPDATE TB_USUARIO t1
    INNER JOIN (
        SELECT USER_URL, COUNT(*) as total
        FROM TB_USUARIO
        GROUP BY USER_URL
        HAVING COUNT(*) > 1
    ) t2 ON t1.USER_URL = t2.USER_URL
    SET t1.USER_URL = CONCAT(LEFT(t1.USER_URL, 117), '-', LOWER(HEX(t1.ID)));

-- remove a anulabilidade e adiciona a restrição de unicidade no campo `USER_URL`
-- lembrando que anteriormente o campo foi adicionado com `NULL` como padrão, mas não é
-- interessante que este campo seja anulável
ALTER TABLE TB_USUARIO
    MODIFY COLUMN USER_URL VARCHAR(150) NOT NULL,
    ADD CONSTRAINT tb_usuario_user_url_unique_c UNIQUE (USER_URL);

-- esse campo já devia ser único, mas a restrição não fora aplicada no DB até então
ALTER TABLE TB_USUARIO
    ADD CONSTRAINT uc_tb_usuario_email UNIQUE (EMAIL);
