-- simple tablespace of 10mb
create tablespace SPRING_SEC_OAUTH2_DATA
     datafile '/opt/sfw/oracle/SPRING_SEC_OAUTH2_DATA/SPRING_SEC_OAUTH2_DATA.dbf' size 10M
     extent management local autoallocate segment space management auto;

-- create the user schema for spring sec oauth 2
create user SPRING_SEC_OAUTH2 identified by "SPRING_SEC_OAUTH2_PW" default tablespace SPRING_SEC_OAUTH2_DATA;
grant connect to SPRING_SEC_OAUTH2;
alter user SPRING_SEC_OAUTH2 quota unlimited on SPRING_SEC_OAUTH2_DATA;
grant all privileges to SPRING_SEC_OAUTH2;

-- This is the schema structure for v 1.0.0.M6d
create table oauth_client_details (
  client_id VARCHAR(256) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256),
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER
);

create table oauth_access_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256),
  user_name VARCHAR(256),
  client_id VARCHAR(256),
  authentication BLOB,
  refresh_token VARCHAR(256)
);

create table oauth_refresh_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication BLOB
);

create table oauth_code (
  code VARCHAR(256), authentication BLOB
);