CREATE TABLE IF NOT EXISTS user
(
    id              BINARY(16)   NOT NULL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL,
    password        VARCHAR(100) NOT NULL,
    instituition    VARCHAR(100),
    cpf             VARCHAR(15)  NOT NULL,
    postalCode      VARCHAR(15)  NOT NULL,
    city            VARCHAR(100) NOT NULL,
    address         VARCHAR(100) NOT NULL,
    houseNumber     VARCHAR(10)  NOT NULL,
    permissionLevel int(11)      NOT NULL
);

CREATE TABLE IF NOT EXISTS curriculum
(
    id     BINARY(16)   NOT NULL,
    userId BINARY(16)   NOT NULL,
    path   VARCHAR(255) NOT NULL
);


CREATE TABLE IF NOT EXISTS post
(
    id          BINARY(16)    NOT NULL,
    userId      BINARY(16)    NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(1000) NOT NULL,
    startDate   DATETIME      NOT NULL,
    endDate     DATETIME      NOT NULL
);

CREATE TABLE IF NOT EXISTS tag
(
    id   BINARY(16)  NOT NULL,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS post_has_user
(
    idPost BINARY(16) NOT NULL,
    idUser BINARY(16) NOT NULL
);


CREATE TABLE IF NOT EXISTS post_has_tag
(
    idPost BINARY(16) NOT NULL,
    idUser BINARY(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS post_has_selected_user
(
    idPost BINARY(16) NOT NULL,
    idUser BINARY(16) NOT NULL
);