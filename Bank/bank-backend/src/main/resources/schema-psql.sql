  -- schema-psql.sql
  -- DDL commands for PostgreSQL

  DROP TABLE IF EXISTS books;

 CREATE TABLE books (
   id     SERIAL PRIMARY KEY,
   name   VARCHAR,
   born DATE);
