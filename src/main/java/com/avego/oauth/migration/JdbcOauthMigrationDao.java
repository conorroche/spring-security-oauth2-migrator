/*
 * Copyright 2013 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avego.oauth.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * The JdbcOauthMigrationDao represents a base class for Oauth migration daos that use spring JDBC template for db access
 * @version $Id: JdbcOauthMigrationDao.java 66424 2013-05-16 11:02:39Z conor.roche $
 * @author conorroche
 */
public abstract class JdbcOauthMigrationDao implements OauthMigrationDao {

	/**
	 * This is the param key to use for the username
	 */
	public static final String USER_KEY = "user";
	/**
	 * This is the param key to use for password
	 */
	public static final String PASS_KEY = "pass";
	/**
	 * This is the param key to use for the jdbc url
	 */
	public static final String JDBC_URL_KEY = "url";

	/**
	 * This is the param key to use for the oauth_access_token table name, doubles as the default table name
	 */
	public static final String ACCESS_TOKEN_TABLE = "oauth_access_token";

	/**
	 * This is the param key to use for the oauth_refresh_token table name, doubles as the default table name
	 */
	public static final String REFRESH_TOKEN_TABLE = "oauth_refresh_token";

	private DataSource datasource;
	private Map<String, Object> params;
	private JdbcTemplate jdbcTemplate;
	private CustomJdbcTokenStore tokenStore;

	/**
	 * This creates a JdbcOauthMigrationDao that uses the given params
	 * @param params The params
	 * @throws SQLException If an erorr occurs creating the data source
	 */
	public JdbcOauthMigrationDao(Map<String, Object> params) throws SQLException {
		if (params == null) {
			throw new IllegalArgumentException("The parameters may not be null");
		}
		this.params = params;
		this.datasource = buildDataSource();
		this.jdbcTemplate = new JdbcTemplate(this.datasource);
		this.tokenStore = new CustomJdbcTokenStore(this.datasource);
	}

	/**
	 * This builds a datasource, it should use getUser(), getPass(), getUrl(), or getParams()
	 * for any credentials or other data it needs to build the datasource
	 * @return The datasource
	 */
	protected abstract DataSource buildDataSource() throws SQLException;

	/**
	 * This gets the username parameter for this dao
	 * @return The username parameter or null if there is none
	 */
	protected String getUser() {
		Object val = getParams().get(USER_KEY);
		return val == null ? null : val.toString();
	}

	/**
	 * This gets the password parameter for this dao
	 * @return The password parameter or null if there is none
	 */
	protected String getPass() {
		Object val = getParams().get(PASS_KEY);
		return val == null ? null : val.toString();
	}

	/**
	 * This gets the jdbc url parameter for this dao
	 * @return The jdbc url parameter or null if there is none
	 */
	protected String getJdbcUrl() {
		Object val = getParams().get(JDBC_URL_KEY);
		return val == null ? null : val.toString();
	}

	/**
	 * This gets the access token table name param
	 * @return the access token table name param
	 */
	protected String getAccessTokenTableName() {
		Object val = getParams().get(ACCESS_TOKEN_TABLE);
		return val == null ? ACCESS_TOKEN_TABLE : val.toString();
	}

	/**
	 * This gets the refresh token table name param
	 * @return the refresh token table name param
	 */
	protected String getRefreshTokenTableName() {
		Object val = getParams().get(REFRESH_TOKEN_TABLE);
		return val == null ? REFRESH_TOKEN_TABLE : val.toString();
	}

	/**
	 * This gets the parameters for this dao
	 * @return The map of parameters for this dao
	 */
	protected Map<String, Object> getParams() {
		return this.params;
	}

	/**
	 * This gets the jdbcTemplate
	 * @return the jdbcTemplate
	 */
	protected JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	/**
	 * This gets the datasource
	 * @return the datasource
	 */
	protected DataSource getDataSource() {
		return this.datasource;
	}

	/**
	 * This gets the tokenStore
	 * @return the tokenStore
	 */
	protected CustomJdbcTokenStore getTokenStore() {
		return this.tokenStore;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countUnmigratedAccessTokens()
	 */
	public int countUnmigratedAccessTokens() {
		return getJdbcTemplate().queryForInt("select count(token_id) from " + getAccessTokenTableName() + " where token_id like ('%-%')");
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countUnmigratedRefreshTokens()
	 */
	public int countUnmigratedRefreshTokens() {
		return getJdbcTemplate().queryForInt("select count(token_id) from " + getRefreshTokenTableName() + " where token_id like ('%-%')");
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countMigratedAccessTokens()
	 */
	public int countMigratedAccessTokens() {
		return getJdbcTemplate().queryForInt("select count(token_id) from " + getAccessTokenTableName() + " where token_id not like ('%-%')");
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countMigratedRefreshTokens()
	 */
	public int countMigratedRefreshTokens() {
		return getJdbcTemplate().queryForInt("select count(token_id) from " + getRefreshTokenTableName() + " where token_id not like ('%-%')");
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#clearRefreshTokens()
	 */
	public void clearRefreshTokens() {
		getJdbcTemplate().update("delete token from " + getRefreshTokenTableName() + " token");
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getUnmigratedOauthAccessTokenRecords(int)
	 */
	public List<OauthAccessTokenRecord> getUnmigratedOauthAccessTokenRecords(int limit) {
		return getOauthAccessTokenRecords(limit, false);
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getMigratedOauthAccessTokenRecords(int)
	 */
	public List<OauthAccessTokenRecord> getMigratedOauthAccessTokenRecords(int limit) {
		return getOauthAccessTokenRecords(limit, true);
	}

	private List<OauthAccessTokenRecord> getOauthAccessTokenRecords(int limit, boolean migrated) {
		String likeClause = migrated ? "not like ('%-%') " : "like ('%-%') ";
		List<OauthAccessTokenRecord> accessTokens = getJdbcTemplate().query(
				"select token_id, token, authentication_id, user_name, client_id, authentication, refresh_token from " + getAccessTokenTableName()
						+ " where token_id " + likeClause + " order by token_id limit " + limit, new RowMapper<OauthAccessTokenRecord>() {

					public OauthAccessTokenRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
						OauthAccessTokenRecord token = new OauthAccessTokenRecord();
						token.setTokenId(rs.getString(1));
						token.setToken(rs.getBytes(2));
						token.setAuthenticationId(rs.getString(3));
						token.setUserName(rs.getString(4));
						token.setClientId(rs.getString(5));
						token.setAuthentication(rs.getBytes(6));
						token.setRefreshToken(rs.getString(7));
						return token;
					}
				});
		return accessTokens;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getUnmigratedOauthRefreshTokenRecords(int)
	 */
	public List<OauthRefreshTokenRecord> getUnmigratedOauthRefreshTokenRecords(int limit) {
		return getOauthRefreshTokenRecords(limit, false);
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getMigratedOauthRefreshTokenRecords(int)
	 */
	public List<OauthRefreshTokenRecord> getMigratedOauthRefreshTokenRecords(int limit) {
		return getOauthRefreshTokenRecords(limit, true);
	}

	private List<OauthRefreshTokenRecord> getOauthRefreshTokenRecords(int limit, boolean migrated) {
		String likeClause = migrated ? "not like ('%-%') " : "like ('%-%') ";
		List<OauthRefreshTokenRecord> refreshTokens = getJdbcTemplate().query(
				"select token_id, token, authentication from " + getRefreshTokenTableName() + " where token_id " + likeClause + " order by token_id limit "
						+ limit, new RowMapper<OauthRefreshTokenRecord>() {

					public OauthRefreshTokenRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
						OauthRefreshTokenRecord token = new OauthRefreshTokenRecord();
						token.setTokenId(rs.getString(1));
						token.setToken(rs.getBytes(2));
						token.setAuthentication(rs.getBytes(3));
						return token;
					}
				});
		return refreshTokens;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#updateOauthAccessToken(java.lang.String, java.lang.String, java.lang.String, byte[], byte[])
	 */
	public void updateOauthAccessToken(String oldTokenId, String newTokenId, String newRefreshToken, byte[] tokenData, byte[] authData) {
		getJdbcTemplate().update(
				"update " + getAccessTokenTableName() + " set token_id = ?, refresh_token = ?, token = ?, authentication = ? where token_id = ?",
				new Object[] { newTokenId, newRefreshToken, tokenData, authData, oldTokenId },
				new int[] { java.sql.Types.VARCHAR, java.sql.Types.VARCHAR, java.sql.Types.BLOB, java.sql.Types.BLOB, java.sql.Types.VARCHAR });
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#updateOauthRefreshToken(java.lang.String, java.lang.String, byte[], byte[])
	 */
	public void updateOauthRefreshToken(String oldTokenId, String newTokenId, byte[] tokenData, byte[] authData) {
		getJdbcTemplate().update("update " + getRefreshTokenTableName() + " set token_id = ?, token = ?, authentication = ? where token_id = ?",
				new Object[] { newTokenId, tokenData, authData, oldTokenId },
				new int[] { java.sql.Types.VARCHAR, java.sql.Types.BLOB, java.sql.Types.BLOB, java.sql.Types.VARCHAR });
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#generateNewTokenKey(java.lang.String)
	 */
	public String generateNewTokenKey(String tokenId) {
		return this.tokenStore.extractTokenKey(tokenId);
	}

}
