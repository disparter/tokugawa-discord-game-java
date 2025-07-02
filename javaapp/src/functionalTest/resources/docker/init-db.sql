-- Script de inicialização do banco de dados para testes funcionais
-- Executa durante a criação do container PostgreSQL

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Configurações de performance para testes
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET track_activity_query_size = 2048;
ALTER SYSTEM SET pg_stat_statements.track = 'all';

-- Configurar encoding
SET client_encoding = 'UTF8';

-- Criar usuário adicional para testes se necessário
-- CREATE USER test_reader WITH PASSWORD 'readonly';

-- Configurar timezone
SET timezone = 'UTC';

-- Log da inicialização
SELECT 'Database initialized for functional tests' as status;