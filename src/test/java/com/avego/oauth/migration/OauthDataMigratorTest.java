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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;
import junit.framework.Assert;

/**
 * The OauthDataMigratorTest represents a test case for the OauthDataMigrator
 * @version $Id$
 * @author conorroche
 */
public class OauthDataMigratorTest extends TestCase {

	private File getTestFile(String name) {
		File file = new File("src/test/resources", name);
		return file;
	}

	private File getTestClassesDir() {
		File dir = new File("target/test-classes/");
		return dir;
	}

	/**
	 * This tests the data migration with the standard
	 * parameters
	 * @throws Exception
	 */
	public void testDefaultDataMigration() throws Exception {

		// set system prop to test case
		System.setProperty(OauthMigrationDaoFactory.MIGRATION_DAO_PROPERTY, InMemTestOauthMigrationDao.class.getName());
		OauthDataMigrator migrator = new OauthDataMigrator(null);

		addTestPrincipalClass(migrator);

		verifyMigrationOfNoTokens(migrator);

		InMemTestOauthMigrationDao dao = (InMemTestOauthMigrationDao) migrator.getDao();
		initTestTokenData(dao);

		Assert.assertEquals(1, dao.countUnmigratedAccessTokens());
		Assert.assertEquals(1, dao.countUnmigratedRefreshTokens());
		Assert.assertEquals(0, dao.countMigratedAccessTokens());
		Assert.assertEquals(0, dao.countMigratedRefreshTokens());

		migrator.migrateData();
		Assert.assertEquals(0, dao.countUnmigratedAccessTokens());
		Assert.assertEquals(0, dao.countUnmigratedRefreshTokens());
		Assert.assertEquals(1, dao.countMigratedAccessTokens());
		Assert.assertEquals(1, dao.countMigratedRefreshTokens());

		// check limit
		Assert.assertEquals(1, dao.getMigratedOauthAccessTokenRecords(1).size());
		Assert.assertEquals(1, dao.getMigratedOauthAccessTokenRecords(2).size());
		Assert.assertEquals(0, dao.getMigratedOauthAccessTokenRecords(0).size());

		Assert.assertEquals(1, dao.getMigratedOauthRefreshTokenRecords(1).size());
		Assert.assertEquals(1, dao.getMigratedOauthRefreshTokenRecords(2).size());
		Assert.assertEquals(0, dao.getMigratedOauthRefreshTokenRecords(0).size());

	}

	/**
	 * This tests that if the remove refresh param is set to true
	 * it clears the refresh tokens
	 * @throws Exception
	 */
	public void testClearRefreshTokensConfig() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put(OauthDataMigrator.SERIALIZE_NEW_TOKEN_VALUES_PARAM, Boolean.FALSE);
		params.put(OauthDataMigrator.REMOVE_REFRESH_TOKENS_PARAM, Boolean.TRUE);
		params.put(OauthMigrationDaoFactory.DEFAULT_MIGRATION_DAO, InMemTestOauthMigrationDao.class.getName());

		OauthDataMigrator migrator = new OauthDataMigrator(params);

		addTestPrincipalClass(migrator);

		verifyMigrationOfNoTokens(migrator);

		InMemTestOauthMigrationDao dao = (InMemTestOauthMigrationDao) migrator.getDao();
		initTestTokenData(dao);

		Assert.assertEquals(1, dao.countUnmigratedAccessTokens());
		Assert.assertEquals(1, dao.countUnmigratedRefreshTokens());
		Assert.assertEquals(0, dao.countMigratedAccessTokens());
		Assert.assertEquals(0, dao.countMigratedRefreshTokens());

		migrator.migrateData();
		Assert.assertEquals(0, dao.countUnmigratedAccessTokens());
		Assert.assertEquals(0, dao.countUnmigratedRefreshTokens());
		Assert.assertEquals(1, dao.countMigratedAccessTokens());
		// this time we expect there to be no migrated refresh tokens as they should have been cleared
		Assert.assertEquals(0, dao.countMigratedRefreshTokens());
		Assert.assertEquals(null, dao.getMigratedOauthAccessTokenRecords(1).get(0).getRefreshToken());

		// check limit
		Assert.assertEquals(1, dao.getMigratedOauthAccessTokenRecords(1).size());
		Assert.assertEquals(1, dao.getMigratedOauthAccessTokenRecords(2).size());
		Assert.assertEquals(0, dao.getMigratedOauthAccessTokenRecords(0).size());

		Assert.assertEquals(0, dao.getMigratedOauthRefreshTokenRecords(1).size());
		Assert.assertEquals(0, dao.getMigratedOauthRefreshTokenRecords(2).size());
		Assert.assertEquals(0, dao.getMigratedOauthRefreshTokenRecords(0).size());

	}

	private void verifyMigrationOfNoTokens(OauthDataMigrator migrator) throws IllegalArgumentException, IOException, ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		OauthMigrationDao dao = migrator.getDao();
		Assert.assertEquals(0, dao.countUnmigratedAccessTokens());
		Assert.assertEquals(0, dao.countUnmigratedRefreshTokens());
		Assert.assertEquals(0, dao.countMigratedAccessTokens());
		Assert.assertEquals(0, dao.countMigratedRefreshTokens());
		migrator.migrateData();
		Assert.assertEquals(0, dao.countUnmigratedAccessTokens());
		Assert.assertEquals(0, dao.countUnmigratedRefreshTokens());
		Assert.assertEquals(0, dao.countMigratedAccessTokens());
		Assert.assertEquals(0, dao.countMigratedRefreshTokens());
	}

	private void addTestPrincipalClass(OauthDataMigrator migrator) throws MalformedURLException {
		// we need to add the principal class used for the test to the deserialization class loader
		URLClassLoader testCl = new URLClassLoader(new URL[] { getTestClassesDir().toURI().toURL() }, migrator.getDeserialisationClassLoader());
		migrator.setDeserialisationClassLoader(testCl);
	}

	private void initTestTokenData(InMemTestOauthMigrationDao dao) throws IOException {
		Map<String, OauthAccessTokenRecord> accessTokens = new HashMap<String, OauthAccessTokenRecord>();
		OauthAccessTokenRecord tokenRecord = new OauthAccessTokenRecord();
		tokenRecord.setTokenId("test-token");
		tokenRecord.setAuthentication(FileUtils.readFileToByteArray(getTestFile("auth2.dat")));
		tokenRecord.setAuthenticationId("test");
		tokenRecord.setClientId("test");
		tokenRecord.setRefreshToken("test");
		tokenRecord.setToken(FileUtils.readFileToByteArray(getTestFile("token1.dat")));
		tokenRecord.setUserName("test");
		accessTokens.put(tokenRecord.getTokenId(), tokenRecord);
		dao.setAccessTokens(accessTokens);

		Map<String, OauthRefreshTokenRecord> refreshTokens = new HashMap<String, OauthRefreshTokenRecord>();
		OauthRefreshTokenRecord refreshTokenRecord = new OauthRefreshTokenRecord();
		refreshTokenRecord.setAuthentication(FileUtils.readFileToByteArray(getTestFile("auth2.dat")));
		refreshTokenRecord.setToken(FileUtils.readFileToByteArray(getTestFile("refreshtoken1.dat")));
		refreshTokenRecord.setTokenId("test-refresh-token");
		refreshTokens.put(refreshTokenRecord.getTokenId(), refreshTokenRecord);
		dao.setRefreshTokens(refreshTokens);
	}

}
