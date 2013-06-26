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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * The MigrateDbOauthTokens represents a utility class to migrate
 * the records in the oauth_access_token and oauth_refresh_token tables
 * It does 2 things:
 * 1) hashes the token id, refresh_token
 * @version $Id: OauthDataMigrator.java 66487 2013-05-16 15:41:43Z conor.roche $
 * @author conorroche
 */
public class OauthDataMigrator {

	private static final Object[] NO_ARGS = null;

	/**
	 * This is the parameter key for whether refresh tokens are removed as part of the migration
	 */
	public static final String REMOVE_REFRESH_TOKENS_PARAM = "remove_refresh_tokens";
	/**
	 * This is the parameter for whether the token stored in the serialized data fields are the original
	 * non hashed value or the hashed value
	 */
	public static final String SERIALIZE_NEW_TOKEN_VALUES_PARAM = "serialize_new_token_values";

	private static final int PAGE_SIZE = Integer.parseInt(System.getProperty("query_page_size", "100"));

	/**
	 * This migrates spring security oauth 2 token data in m6 form to 1.0.5 release form
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
			System.err.println("Usage <db_jdbc_url> <db_user> <db_pw>");
			System.err.println("Or <db_jdbc_url> <db_user> <db_pw>"
					+ " <remove_refresh_tokens> <serialize_new_token_values> <oauth_access_token_table> <oauth_refresh_token_table>");
			System.exit(1);
		}

		Boolean removeRefreshTokens = Boolean.FALSE;
		if (args.length > 3) {
			removeRefreshTokens = Boolean.parseBoolean(args[3]);
		}
		Boolean serializeNewTokenValues = Boolean.FALSE;
		if (args.length > 4) {
			serializeNewTokenValues = Boolean.parseBoolean(args[4]);
		}

		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put(JdbcOauthMigrationDao.JDBC_URL_KEY, args[0]);
		params.put(JdbcOauthMigrationDao.USER_KEY, args[1]);
		params.put(JdbcOauthMigrationDao.PASS_KEY, args[2]);
		params.put(REMOVE_REFRESH_TOKENS_PARAM, removeRefreshTokens);
		params.put(SERIALIZE_NEW_TOKEN_VALUES_PARAM, serializeNewTokenValues);

		if (args.length > 5) {
			params.put(JdbcOauthMigrationDao.ACCESS_TOKEN_TABLE, args[5]);
		}
		if (args.length > 6) {
			params.put(JdbcOauthMigrationDao.REFRESH_TOKEN_TABLE, args[6]);
		}
		OauthDataMigrator migrator = new OauthDataMigrator(params);
		migrator.migrateData();
	}

	private OauthMigrationDao dao;
	private ClassLoader deserialisationClassLoader;
	private boolean removeRefreshTokens;
	private boolean serializeNewTokenValues;

	/**
	 * This creates a OauthDataMigrator that uses the given dao and deserialisationClassLoader
	 * @param dao The dao used to migrate the data
	 * @param deserialisationClassLoader The class loader used to deserialise the oauth data
	 * @param removeRefreshTokens Whether refresh tokens are to be removed, spring sec m6 creates refresh tokens for every access token which is not needed when
	 *            implicit
	 * @param serializeNewTokenValues Whether the serialized authentication and token data should have the token values in them replaced with the new hashed
	 *            values or left unhashed
	 */
	public OauthDataMigrator(OauthMigrationDao dao, ClassLoader deserialisationClassLoader, boolean removeRefreshTokens, boolean serializeNewTokenValues) {
		super();
		this.dao = dao;
		this.deserialisationClassLoader = deserialisationClassLoader;
		this.removeRefreshTokens = removeRefreshTokens;
		this.serializeNewTokenValues = serializeNewTokenValues;
	}

	/**
	 * This creates a OauthDataMigrator using the give params, it automatically
	 * creates a deserialisation class loader used to deserialize existing oauth data and creates a migration dao using
	 * the given parameters
	 * @param params The parameters used to create the migration dao
	 * @throws FileNotFoundException If the oldlib dir was not found relative to the current dir or in currentdir/target/oldlib
	 * @throws DaoCreationException If the oauth migration dao could not be created from the given parameters
	 */
	public OauthDataMigrator(Map<String, Object> params) throws FileNotFoundException, DaoCreationException {
		this.dao = OauthMigrationDaoFactory.newInstance(params);
		// find where to source the old jars from
		File oldLibDir = new File("oldlib");
		if (!oldLibDir.exists()) {
			oldLibDir = new File("target/oldlib");
		}
		if (!oldLibDir.exists()) {
			throw new FileNotFoundException("Could not find the lib dir either in the current dir or in target dir");
		}

		// the system class loader includes jars on the class path, however this also would include
		// the new versions of the spring classes which we do not want, so we use the
		// Parent of the system class loader which is the ext class loader/ e..g includes bootstrap java classes
		this.deserialisationClassLoader = ClassLoaderUtils.createClassLoaderWithJars(oldLibDir.getAbsolutePath(), ClassLoader.getSystemClassLoader()
				.getParent());
		if (params != null) {
			Boolean val = (Boolean) params.get(REMOVE_REFRESH_TOKENS_PARAM);
			if (val != null) {
				this.removeRefreshTokens = val.booleanValue();
			}
			val = (Boolean) params.get(SERIALIZE_NEW_TOKEN_VALUES_PARAM);
			if (val != null) {
				this.serializeNewTokenValues = val.booleanValue();
			}
		}
	}

	/**
	 * This migrates the oauth data
	 * @throws IOException If an IO error occurs such as when serializing/deserializing data
	 * @throws ClassNotFoundException If a class not found when serializing/deserializing data
	 * @throws InvocationTargetException If an invocation target exception occurs when using reflection to convert to the new objects
	 * @throws IllegalAccessException If an IllegalAccessException exception occurs when using reflection to convert to the new objects
	 * @throws NoSuchMethodException If a NoSuchMethodException exception occurs when using reflection to convert to the new objects
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	public void migrateData() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			IllegalArgumentException, InstantiationException {
		migrateAccessTokens();
		migrateRefreshTokens();
	}

	/**
	 * This gets the dao used to migrate the data
	 * @return the dao used to migrate the data
	 */
	public OauthMigrationDao getDao() {
		return this.dao;
	}

	/**
	 * This sets the dao used to migrate the data
	 * @param dao the dao used to migrate the data
	 */
	public void setDao(OauthMigrationDao dao) {
		this.dao = dao;
	}

	/**
	 * This gets the deserialisationClassLoader used to deserialize old auth and token data
	 * @return the deserialisationClassLoader used to deserialize old auth and token data
	 */
	public ClassLoader getDeserialisationClassLoader() {
		return this.deserialisationClassLoader;
	}

	/**
	 * This sets the deserialisationClassLoader used to deserialize old auth and token data
	 * @param deserialisationClassLoader the deserialisationClassLoader used to deserialize old auth and token data
	 */
	public void setDeserialisationClassLoader(ClassLoader deserialisationClassLoader) {
		this.deserialisationClassLoader = deserialisationClassLoader;
	}

	/**
	 * This gets Whether refresh tokens are to be removed, spring sec m6 creates refresh tokens for every access token which is not needed when
	 * implicit
	 * @return Whether refresh tokens are to be removed, spring sec m6 creates refresh tokens for every access token which is not needed when
	 *         implicit
	 */
	public boolean isRemoveRefreshTokens() {
		return this.removeRefreshTokens;
	}

	/**
	 * This sets Whether refresh tokens are to be removed, spring sec m6 creates refresh tokens for every access token which is not needed when
	 * implicit
	 * @param removeRefreshTokens Whether refresh tokens are to be removed, spring sec m6 creates refresh tokens for every access token which is not needed when
	 *            implicit
	 */
	public void setRemoveRefreshTokens(boolean removeRefreshTokens) {
		this.removeRefreshTokens = removeRefreshTokens;
	}

	/**
	 * This gets Whether the serialized authentication and token data should have the token values in them replaced with the new hashed
	 * values or left unhashed
	 * @return Whether the serialized authentication and token data should have the token values in them replaced with the new hashed
	 *         values or left unhashed
	 */
	public boolean isSerializeNewTokenValues() {
		return this.serializeNewTokenValues;
	}

	/**
	 * This sets Whether the serialized authentication and token data should have the token values in them replaced with the new hashed
	 * values or left unhashed
	 * @param serializeNewTokenValues Whether the serialized authentication and token data should have the token values in them replaced with the new hashed
	 *            values or left unhashed
	 */
	public void setSerializeNewTokenValues(boolean serializeNewTokenValues) {
		this.serializeNewTokenValues = serializeNewTokenValues;
	}

	/**
	 * This migrates the oauth access tokens
	 * @throws IOException If an IO error occurs such as when serializing/deserializing data
	 * @throws ClassNotFoundException If a class not found when serializing/deserializing data
	 * @throws InvocationTargetException If an invocation target exception occurs when using reflection to convert to the new objects
	 * @throws IllegalAccessException If an IllegalAccessException exception occurs when using reflection to convert to the new objects
	 * @throws NoSuchMethodException If a NoSuchMethodException exception occurs when using reflection to convert to the new objects
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	protected void migrateAccessTokens() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			IllegalArgumentException, InstantiationException {

		int numTokens = this.dao.countUnmigratedAccessTokens();
		int pageSize = PAGE_SIZE;

		int numMigrated = 0;
		System.out.println("Starting Migrating " + numTokens + " access token(s) ...");

		while (numTokens > 0) {

			List<OauthAccessTokenRecord> accessTokens = this.dao.getUnmigratedOauthAccessTokenRecords(pageSize);

			for (OauthAccessTokenRecord tokenRecord : accessTokens) {

				String oldTokenId = tokenRecord.getTokenId();
				System.out.println("Migrating token with id: " + oldTokenId + "...");

				String newTokenId = this.dao.generateNewTokenKey(tokenRecord.getTokenId());
				String newRefreshToken = this.dao.generateNewTokenKey(tokenRecord.getRefreshToken());

				if (this.removeRefreshTokens) {
					newRefreshToken = null;
				}

				System.out.println("New token id: " + newTokenId);
				System.out.println("New refresh token id: " + newRefreshToken);

				// deserialize the token, note this is backward compatible
				OAuth2AccessToken accessToken = null;
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(tokenRecord.getToken()));
				try {
					Object obj = ois.readObject();
					accessToken = (OAuth2AccessToken) obj;
				} finally {
					ois.close();
				}

				// replace the token value in the access token..
				if (this.serializeNewTokenValues) {

					Constructor<OAuth2AccessToken> constructor = null;

					// see if it has a set value method
					Method setValueMethod = MethodUtils.getAccessibleMethod(accessToken.getClass(), "setValue", String.class);
					if (setValueMethod != null) {
						Object res = setValueMethod.invoke(accessToken, newTokenId);
						if (res != null && res instanceof OAuth2AccessToken) {
							accessToken = (OAuth2AccessToken) res;
						}
					} else {

						// look for constructors that we can use
						constructor = (Constructor<OAuth2AccessToken>) ConstructorUtils.getAccessibleConstructor(accessToken.getClass(), String.class);
						if (constructor != null) {

							OAuth2AccessToken newAccessToken = constructor.newInstance(newTokenId);

							// we also need to invoke setters for other fields
							MethodUtils.invokeMethod(newAccessToken, "setAdditionalInformation", accessToken.getAdditionalInformation());
							MethodUtils.invokeMethod(newAccessToken, "setExpiration", accessToken.getExpiration());
							MethodUtils.invokeMethod(newAccessToken, "setScope", accessToken.getScope());
							MethodUtils.invokeMethod(newAccessToken, "setTokenType", accessToken.getTokenType());

							accessToken = newAccessToken;

						} else {
							throw new IllegalStateException("The access token with the class: " + accessToken.getClass().getName()
									+ " did not have a set value method nor a constructor taking a string which we need to update its token value");
						}

					}

					// we also need to overwrite the refresh token
					String newRefreshTokenValue = this.dao.generateNewTokenKey(accessToken.getRefreshToken().getValue());
					OAuth2RefreshToken refreshToken = replaceOAuth2RefreshTokenValue(accessToken.getRefreshToken(), newRefreshTokenValue);
					MethodUtils.invokeMethod(accessToken, "setRefreshToken", refreshToken);
				}

				if (this.removeRefreshTokens) {
					MethodUtils.invokeMethod(accessToken, "setRefreshToken", new Object[] { null }, new Class<?>[] { OAuth2RefreshToken.class });
				}

				byte[] tokenData = SerializationUtils.serialize((Serializable) accessToken);

				// deserialise the authenticated, this is NOT backward compatible so we have to read using a diff class loader
				OAuth2Authentication auth = deserializeOAuth2Authentication(tokenRecord.getAuthentication());
				byte[] authData = SerializationUtils.serialize(auth);

				// this does the actual migration of the token
				this.dao.updateOauthAccessToken(oldTokenId, newTokenId, newRefreshToken, tokenData, authData);

				System.out.println("Migrated token with id: " + oldTokenId);
				numMigrated++;
				System.out.println("");
			}
			numTokens = this.dao.countUnmigratedAccessTokens();
		}

		System.out.println("Finished Migrating " + numMigrated + " access token(s).");

	}

	/**
	 * This migrates the oauth refresh tokens
	 * @throws IOException If an IO error occurs such as when serializing/deserializing data
	 * @throws ClassNotFoundException If a class not found when serializing/deserializing data
	 * @throws InvocationTargetException If an invocation target exception occurs when using reflection to convert to the new objects
	 * @throws IllegalAccessException If an IllegalAccessException exception occurs when using reflection to convert to the new objects
	 * @throws NoSuchMethodException If a NoSuchMethodException exception occurs when using reflection to convert to the new objects
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	protected void migrateRefreshTokens() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			IllegalArgumentException, InstantiationException {

		int numTokens = this.dao.countUnmigratedRefreshTokens();

		if (this.removeRefreshTokens) {
			System.out.println("Clearing " + numTokens + " refresh token(s)...");
			this.dao.clearRefreshTokens();
			System.out.println("Finished clearing refresh token(s).");
		} else {

			int pageSize = PAGE_SIZE;

			int numMigrated = 0;
			System.out.println("Starting Migrating " + numTokens + " refresh token(s) ...");

			while (numTokens > 0) {

				List<OauthRefreshTokenRecord> refreshTokens = this.dao.getUnmigratedOauthRefreshTokenRecords(pageSize);

				for (OauthRefreshTokenRecord tokenRecord : refreshTokens) {

					String oldTokenId = tokenRecord.getTokenId();

					System.out.println("Migrating token with id: " + oldTokenId + "...");

					String newTokenId = this.dao.generateNewTokenKey(tokenRecord.getTokenId());

					System.out.println("New token id: " + newTokenId);

					// deserialize the token, note this is backward compatible
					OAuth2RefreshToken refreshToken = null;
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(tokenRecord.getToken()));
					try {
						Object obj = ois.readObject();
						refreshToken = (OAuth2RefreshToken) obj;
					} finally {
						ois.close();
					}

					// replace the token value in the refresh token..
					if (this.serializeNewTokenValues) {
						refreshToken = replaceOAuth2RefreshTokenValue(refreshToken, newTokenId);
					}

					byte[] tokenData = SerializationUtils.serialize((Serializable) refreshToken);

					// deserialise the authenticated, this is NOT backward compatible so we have to read using a diff class loader
					OAuth2Authentication auth = deserializeOAuth2Authentication(tokenRecord.getAuthentication());
					byte[] authData = SerializationUtils.serialize(auth);

					// this does the actual migration of the token
					this.dao.updateOauthRefreshToken(oldTokenId, newTokenId, tokenData, authData);

					System.out.println("Migrated token with id: " + oldTokenId);
					numMigrated++;
					System.out.println("");
				}
				numTokens = this.dao.countUnmigratedRefreshTokens();
			}

			System.out.println("Finished Migrating " + numMigrated + " refresh token(s).");
		}
	}

	/**
	 * This creates a new refresh token with the token id replaced with the given new token id,
	 * it maintains the token expiry assuming the token implements ExpiringOAuth2RefreshToken
	 * @param origToken The original token to create a new equivalent of with the token value replaced
	 * @param newTokenId The new token value
	 * @return The equivalent new refresh token with the value replaced with the new token
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	private OAuth2RefreshToken replaceOAuth2RefreshTokenValue(OAuth2RefreshToken origToken, String newTokenId) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		OAuth2RefreshToken refreshToken = origToken;
		if (origToken != null) {

			// see if it has a set value method
			Method setValueMethod = MethodUtils.getAccessibleMethod(refreshToken.getClass(), "setValue", String.class);

			if (setValueMethod != null) {
				Object res = setValueMethod.invoke(refreshToken, newTokenId);
				if (res != null && res instanceof OAuth2RefreshToken) {
					refreshToken = (OAuth2RefreshToken) res;
				}

			} else {

				Constructor<OAuth2RefreshToken> constructor = null;
				// look for constructors that we can use
				if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
					java.util.Date expiry = ((ExpiringOAuth2RefreshToken) refreshToken).getExpiration();
					constructor = (Constructor<OAuth2RefreshToken>) ConstructorUtils.getAccessibleConstructor(refreshToken.getClass(), java.util.Date.class,
							String.class);
					if (constructor == null) {
						constructor = (Constructor<OAuth2RefreshToken>) ConstructorUtils.getAccessibleConstructor(refreshToken.getClass(), String.class,
								java.util.Date.class);
						if (constructor != null) {
							refreshToken = constructor.newInstance(newTokenId, expiry);
						}
					} else {
						refreshToken = constructor.newInstance(expiry, newTokenId);
					}
				} else {
					constructor = (Constructor<OAuth2RefreshToken>) ConstructorUtils.getAccessibleConstructor(refreshToken.getClass(), String.class);
					if (constructor != null) {
						refreshToken = constructor.newInstance(newTokenId);
					}
				}

				if (constructor == null) {
					throw new IllegalStateException("Could not replace the refresh token value as the refresh token class: " + refreshToken.getClass()
							+ " did not have a setValue method and did not have a matching constructor");
				}
			}
		}
		return refreshToken;
	}

	/**
	 * This takes serialization data of the old authentication object and then
	 * reads it as an object using a separate class loader which has the old version of the spring and spring security classes
	 * We then use reflection to access the compatible fields and then use this data to construct the new class version object
	 * @param oldAuthData The serialized data created with the old version of the classes
	 * @return The deserialized OAuth2Authentication object constructed with the new class versions
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private OAuth2Authentication deserializeOAuth2Authentication(byte[] oldAuthData) throws IOException, ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		OAuth2Authentication auth = null;
		ObjectInputStream ois = new CustomObjectInputStream(new ByteArrayInputStream(oldAuthData), this.deserialisationClassLoader);
		try {
			Object obj = ois.readObject();

			// the instance of this is the old OAuth2Authentication however we cannot cast
			// so have to use reflection to access the fields and data
			// and then construct a new OAuth2Authentication from this

			Object oldAuthorizationRequest = MethodUtils.invokeMethod(obj, "getAuthorizationRequest", NO_ARGS);

			Object authentication = MethodUtils.invokeMethod(obj, "getUserAuthentication", NO_ARGS);
			Object principal = MethodUtils.invokeMethod(authentication, "getPrincipal", NO_ARGS);
			Object credentials = MethodUtils.invokeMethod(authentication, "getCredentials", NO_ARGS);
			Collection<GrantedAuthority> authorities = convertAuthorities((Collection<?>) MethodUtils.invokeMethod(authentication, "getAuthorities", NO_ARGS));

			// now construct the oauth authentication object with the new auth and request
			Authentication authToken = new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
			AuthorizationRequest authReq = convertAuthorizationRequest(oldAuthorizationRequest);
			auth = new OAuth2Authentication(authReq, authToken);

		} finally {
			ois.close();
		}
		return auth;
	}

	@SuppressWarnings("unchecked")
	static AuthorizationRequest convertAuthorizationRequest(Object authorizationRequest) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Map<String, String> authorizationParameters = (Map<String, String>) MethodUtils.invokeMethod(authorizationRequest, "getParameters", NO_ARGS);
		// String clientId = (String) MethodUtils.invokeMethod(authorizationRequest, "getClientId", NO_ARGS);
		// Collection<String> scope = (Collection<String>) MethodUtils.invokeMethod(authorizationRequest, "getScope", NO_ARGS);
		Collection<GrantedAuthority> authorities = convertAuthorities((Collection<?>) MethodUtils.invokeMethod(authorizationRequest, "getAuthorities", NO_ARGS));
		Set<String> resourceIds = (Set<String>) MethodUtils.invokeMethod(authorizationRequest, "getResourceIds", NO_ARGS);
		Boolean approvedObj = (Boolean) MethodUtils.invokeMethod(authorizationRequest, "isApproved", NO_ARGS);
		boolean approved = false;
		if (approvedObj != null) {
			approved = approvedObj;
		}
		String redirectUri = (String) MethodUtils.invokeMethod(authorizationRequest, "getRedirectUri", NO_ARGS);
		DefaultAuthorizationRequest req = new DefaultAuthorizationRequest(authorizationParameters);
		req.setApproved(approved);
		req.setAuthorities(authorities);
		req.setResourceIds(resourceIds);
		req.setRedirectUri(redirectUri);
		return req;
	}

	static Collection<GrantedAuthority> convertAuthorities(Collection<?> authorities) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		List<GrantedAuthority> res = new ArrayList<GrantedAuthority>();
		if (authorities != null) {
			for (Object authority : authorities) {
				String auth = (String) MethodUtils.invokeMethod(authority, "getAuthority", NO_ARGS);
				res.add(new SimpleGrantedAuthority(auth));
			}
		}
		return res;
	}

}
