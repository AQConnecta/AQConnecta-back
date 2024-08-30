ALTER TABLE `TB_USUARIO`
    ADD COLUMN `USER_URL` VARCHAR(150) DEFAULT NULL;


UPDATE `TB_USUARIO`
    SET `USER_URL` = LOWER(REPLACE(`NOME`, ' ', '-'));