# Controle Financeiro - Lab BD

Aplicativo Android de controle financeiro com conexão MySQL via JDBC.

## Pré-requisitos

- MySQL instalado e rodando
- Android Studio com emulador configurado

## Configuração do Banco de Dados

1. Abra o MySQL Workbench
2. Execute o script abaixo para criar o banco, as tabelas e a stored procedure:

```sql
CREATE DATABASE IF NOT EXISTS FinanceiroDB CHARACTER SET utf8 COLLATE utf8_unicode_ci;
USE FinanceiroDB;

CREATE TABLE IF NOT EXISTS metaFinanceira (
    id    INT NOT NULL AUTO_INCREMENT,
    valor DECIMAL(10,2) NOT NULL,
    nome  VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS despesa (
    id    INT NOT NULL AUTO_INCREMENT,
    valor DECIMAL(10,2) NOT NULL,
    data  DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS receita (
    id    INT NOT NULL AUTO_INCREMENT,
    valor DECIMAL(10,2) NOT NULL,
    data  DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reserva (
    id             INT NOT NULL AUTO_INCREMENT,
    valor          DECIMAL(10,2) NOT NULL,
    data           DATE NOT NULL,
    metaFinanceira INT,
    PRIMARY KEY (id),
    FOREIGN KEY (metaFinanceira) REFERENCES metaFinanceira(id) ON DELETE SET NULL
);

DELIMITER $$
CREATE PROCEDURE sp_resumo_financeiro(
    IN  p_mes           INT,
    IN  p_ano           INT,
    OUT p_total_receita DECIMAL(10,2),
    OUT p_total_despesa DECIMAL(10,2),
    OUT p_saldo         DECIMAL(10,2)
)
BEGIN
    SELECT COALESCE(SUM(valor), 0) INTO p_total_receita FROM receita  WHERE MONTH(data) = p_mes AND YEAR(data) = p_ano;
    SELECT COALESCE(SUM(valor), 0) INTO p_total_despesa FROM despesa  WHERE MONTH(data) = p_mes AND YEAR(data) = p_ano;
    SET p_saldo = p_total_receita - p_total_despesa;
END$$
DELIMITER ;
```

3. Crie um usuário para o app:

```sql
GRANT ALL PRIVILEGES ON FinanceiroDB.* TO 'app_user'@'%' IDENTIFIED BY 'senha123';
FLUSH PRIVILEGES;
```

4. Em `DatabaseConnection.java`, ajuste as credenciais:

```java
private static final String HOST     = "10.0.2.2";
private static final String USER     = "app_user";
private static final String PASSWORD = "senha123";
```

## Como Rodar

1. Execute o script SQL no Workbench
2. Abra o projeto no Android Studio
3. Rode no emulador
