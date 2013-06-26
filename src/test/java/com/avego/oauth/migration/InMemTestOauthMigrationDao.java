/*
 * Copyright © 2013 Avego Ltd., All Rights Reserved.
 * For licensing terms please contact Avego LTD.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avego.oauth.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.mockito.Mockito;

/**
 * The InMemTestOauthMigrationDao represents a dao that stores in tokens in memory
 * for testing.
 * @version $Id$
 * @author conorroche
 */
public class InMemTestOauthMigrationDao implements OauthMigrationDao {

	private CustomJdbcTokenStore tokenStore = new CustomJdbcTokenStore(Mockito.mock(DataSource.class));

	private Map<String, OauthAccessTokenRecord> accessTokens;
	private Map<String, OauthRefreshTokenRecord> refreshTokens;

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countUnmigratedAccessTokens()
	 */
	public int countUnmigratedAccessTokens() {
		// note not an efficient way to count could for example store migrated and unmigrated in
		// separate maps and count the keyset but as this is a test case just doing a quick
		// and easy impl
		if (this.accessTokens != null) {
			return countUnmigratedTokens(this.accessTokens.keySet());
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countMigratedAccessTokens()
	 */
	public int countMigratedAccessTokens() {
		if (this.accessTokens != null) {
			return countMigratedTokens(this.accessTokens.keySet());
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countUnmigratedRefreshTokens()
	 */
	public int countUnmigratedRefreshTokens() {
		if (this.refreshTokens != null) {
			return countUnmigratedTokens(this.refreshTokens.keySet());
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#countMigratedRefreshTokens()
	 */
	public int countMigratedRefreshTokens() {
		if (this.refreshTokens != null) {
			return countMigratedTokens(this.refreshTokens.keySet());
		}
		return 0;
	}

	private int countUnmigratedTokens(Collection<String> tokens) {
		int count = 0;
		for (String token : tokens) {
			if (isUnmigrated(token)) {
				count++;
			}
		}
		return count;
	}

	private int countMigratedTokens(Collection<String> tokens) {
		int count = 0;
		for (String token : tokens) {
			if (!isUnmigrated(token)) {
				count++;
			}
		}
		return count;
	}

	private boolean isUnmigrated(String tokenId) {
		return tokenId.indexOf("-") > -1;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#clearRefreshTokens()
	 */
	public void clearRefreshTokens() {
		if (this.refreshTokens != null) {
			this.refreshTokens.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#generateNewTokenKey(java.lang.String)
	 */
	public String generateNewTokenKey(String tokenId) {
		return this.tokenStore.extractTokenKey(tokenId);
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getUnmigratedOauthAccessTokenRecords(int)
	 */
	@SuppressWarnings("unchecked")
	public List<OauthAccessTokenRecord> getUnmigratedOauthAccessTokenRecords(int limit) {
		return (List<OauthAccessTokenRecord>) getOauthTokenRecords(this.accessTokens.values(), limit, false);
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getUnmigratedOauthRefreshTokenRecords(int)
	 */
	@SuppressWarnings("unchecked")
	public List<OauthRefreshTokenRecord> getUnmigratedOauthRefreshTokenRecords(int limit) {
		return (List<OauthRefreshTokenRecord>) getOauthTokenRecords(this.refreshTokens.values(), limit, false);
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getMigratedOauthAccessTokenRecords(int)
	 */
	@SuppressWarnings("unchecked")
	public List<OauthAccessTokenRecord> getMigratedOauthAccessTokenRecords(int limit) {
		return (List<OauthAccessTokenRecord>) getOauthTokenRecords(this.accessTokens.values(), limit, true);
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#getMigratedOauthRefreshTokenRecords(int)
	 */
	@SuppressWarnings("unchecked")
	public List<OauthRefreshTokenRecord> getMigratedOauthRefreshTokenRecords(int limit) {
		return (List<OauthRefreshTokenRecord>) getOauthTokenRecords(this.refreshTokens.values(), limit, true);
	}

	/**
	 * This builds a list of token records from the given collection with up to limit entries in it
	 * @param records The records
	 * @param limit The max no. of records to return in the list
	 * @param migrated Whether to get migrated token records
	 * @return The list of token records from the given records
	 */
	private List<? extends TokenRecord> getOauthTokenRecords(Collection<? extends TokenRecord> records, int limit, boolean migrated) {
		List<TokenRecord> res = Collections.emptyList();
		if (records != null && !records.isEmpty()) {
			res = new ArrayList<TokenRecord>();
			for (TokenRecord record : records) {
				boolean recordUnmigrated = isUnmigrated(record.getTokenId());
				if ((migrated && !recordUnmigrated) || (!migrated && recordUnmigrated)) {
					if (res.size() == limit) {
						break;
					}
					res.add(record);
				}
			}
		}
		return res;
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#updateOauthAccessToken(java.lang.String, java.lang.String, java.lang.String, byte[], byte[])
	 */
	public void updateOauthAccessToken(String oldTokenId, String newTokenId, String newRefreshToken, byte[] tokenData, byte[] authData) {
		OauthAccessTokenRecord record = this.accessTokens.remove(oldTokenId);
		if (record == null) {
			throw new IllegalStateException("No access token record was found with the id: " + oldTokenId + " when trying to update a token with the new id: "
					+ newTokenId);
		}
		record.setTokenId(newTokenId);
		record.setAuthentication(authData);
		record.setRefreshToken(newRefreshToken);
		record.setToken(tokenData);
		this.accessTokens.put(newTokenId, record);
	}

	/**
	 * {@inheritDoc}
	 * @see com.avego.oauth.migration.OauthMigrationDao#updateOauthRefreshToken(java.lang.String, java.lang.String, byte[], byte[])
	 */
	public void updateOauthRefreshToken(String oldTokenId, String newTokenId, byte[] tokenData, byte[] authData) {
		OauthRefreshTokenRecord record = this.refreshTokens.remove(oldTokenId);
		if (record == null) {
			throw new IllegalStateException("No refresh token record was found with the id: " + oldTokenId + " when trying to update a token with the new id: "
					+ newTokenId);
		}
		record.setTokenId(newTokenId);
		record.setAuthentication(authData);
		record.setToken(tokenData);
		this.refreshTokens.put(newTokenId, record);
	}

	/**
	 * This gets the accessTokens
	 * @return the accessTokens
	 */
	public Map<String, OauthAccessTokenRecord> getAccessTokens() {
		return this.accessTokens;
	}

	/**
	 * This sets the accessTokens
	 * @param accessTokens the accessTokens to set
	 */
	public void setAccessTokens(Map<String, OauthAccessTokenRecord> accessTokens) {
		this.accessTokens = accessTokens;
	}

	/**
	 * This gets the refreshTokens
	 * @return the refreshTokens
	 */
	public Map<String, OauthRefreshTokenRecord> getRefreshTokens() {
		return this.refreshTokens;
	}

	/**
	 * This sets the refreshTokens
	 * @param refreshTokens the refreshTokens to set
	 */
	public void setRefreshTokens(Map<String, OauthRefreshTokenRecord> refreshTokens) {
		this.refreshTokens = refreshTokens;
	}

}
