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

import java.util.List;

/**
 * The OauthMigrationDao represents a DAO for migrating oauth data
 * from pre 1.0 e.g. M6 to 1.0
 * @version $Id: OauthMigrationDao.java 66463 2013-05-16 14:29:24Z conor.roche $
 * @author conorroche
 */
public interface OauthMigrationDao {

	/**
	 * This gets the number of unmigrated access tokens in the db
	 * @return The number of unmigrated access tokens
	 */
	public int countUnmigratedAccessTokens();

	/**
	 * This gets the number of migrated access tokens in the db
	 * @return The number of migrated access tokens
	 */
	public int countMigratedAccessTokens();

	/**
	 * This gets the number of unmigrated refresh tokens in the db
	 * @return The number of unmigrated refresh tokens
	 */
	public int countUnmigratedRefreshTokens();

	/**
	 * This gets the number of migrated refresh tokens in the db
	 * @return The number of migrated refresh tokens
	 */
	public int countMigratedRefreshTokens();

	/**
	 * This removes all refresh tokens in the db
	 */
	public void clearRefreshTokens();

	/**
	 * This generates a new token key from the existing token id, in old spring security oauth2
	 * the tokens were stored in the db unhashed, in v 1.00+ they are stored hashed
	 * @param tokenId The original token id
	 * @return The new token key (e.g. hashed)
	 */
	public String generateNewTokenKey(String tokenId);

	/**
	 * This gets a given number of unmigrated oauth access token records from the db
	 * @param limit The max number of records to get
	 * @return The list of oauth access token records
	 */
	public List<OauthAccessTokenRecord> getUnmigratedOauthAccessTokenRecords(int limit);

	/**
	 * This gets a given number of unmigrated oauth refresh token records from the db
	 * @param limit The max number of records to get
	 * @return The list of oauth refresh token records
	 */
	public List<OauthRefreshTokenRecord> getUnmigratedOauthRefreshTokenRecords(int limit);

	/**
	 * This gets a given number of migrated oauth access token records from the db
	 * @param limit The max number of records to get
	 * @return The list of oauth access token records
	 */
	public List<OauthAccessTokenRecord> getMigratedOauthAccessTokenRecords(int limit);

	/**
	 * This gets a given number of migrated oauth refresh token records from the db
	 * @param limit The max number of records to get
	 * @return The list of oauth refresh token records
	 */
	public List<OauthRefreshTokenRecord> getMigratedOauthRefreshTokenRecords(int limit);

	/**
	 * This updates an oauth access token in the db
	 * @param oldTokenId The old token id
	 * @param newTokenId The new token id
	 * @param newRefreshToken The new refresh token
	 * @param tokenData The serialized token data
	 * @param authData The serialized authentication data
	 */
	public void updateOauthAccessToken(String oldTokenId, String newTokenId, String newRefreshToken, byte[] tokenData, byte[] authData);

	/**
	 * This updates an oauth refresh token in the db
	 * @param oldTokenId The old token id
	 * @param newTokenId The new token id
	 * @param tokenData The serialized token data
	 * @param authData The serialized authentication data
	 */
	public void updateOauthRefreshToken(String oldTokenId, String newTokenId, byte[] tokenData, byte[] authData);

}
