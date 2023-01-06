CREATE KEYSPACE IF NOT EXISTS test_authorization 
  WITH REPLICATION = { 
   'class' : 'NetworkTopologyStrategy', 
   'datacenter1' : 1
};

CREATE KEYSPACE IF NOT EXISTS main 
  WITH REPLICATION = { 
   'class' : 'NetworkTopologyStrategy', 
   'datacenter1' : 1
};

CREATE TABLE IF NOT EXISTS test_authorization.login (
	username text PRIMARY KEY,
	password text,
	disabled boolean,
	account_locked boolean
);

CREATE TABLE IF NOT EXISTS main.oauth2 (
	id text PRIMARY KEY,
	registered_client_id text,
	principal_name text,
	authorized_scopes set<text>,
	authorization_grant_type text,
	attributes text,
	access_token_value text,
	access_token_issued_at timestamp,
	access_token_expires_at timestamp,
	access_token_metadata text,
	access_token_type text,
	access_token_scopes set<text>,
	accessTokenType text,
	authorization_code_value text,
	authorization_code_issued_at timestamp,
	authorization_code_expires_at timestamp,
	authorization_code_metadata text,
	refresh_token_value text,
	refresh_token_issued_at timestamp,
	refresh_token_expires_at timestamp,
	refresh_token_metadata text
);

CREATE INDEX IF NOT EXISTS oauth2_authorization_code ON main.oauth2 ( authorization_code_value );

CREATE INDEX IF NOT EXISTS oauth2_refresh_token ON main.oauth2 ( refresh_token_value );
