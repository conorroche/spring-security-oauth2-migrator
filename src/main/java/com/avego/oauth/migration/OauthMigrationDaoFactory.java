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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * The TokenDaoFactory represents a factory for a token DAO
 * It looks for a system property com.avego.oauth.migration.OauthMigrationDao
 * and uses that for the implementation
 * @version $Id: OauthMigrationDaoFactory.java 66251 2013-05-14 13:56:07Z conor.roche $
 * @author conorroche
 */
public class OauthMigrationDaoFactory {

	/**
	 * This is the system property for the migration dao implementation also can be a param
	 * in the map in which case the map param takes precedence
	 */
	public static final String MIGRATION_DAO_PROPERTY = "com.avego.oauth.migration.OauthMigrationDao";
	/**
	 * This is the default implementation of the migration dao
	 */
	public static final String DEFAULT_MIGRATION_DAO = MysqlOauthMigrationDao.class.getName();

	/**
	 * This creates an instance of a token dao
	 * @param params The dao params
	 * @return The token dao instance
	 * @throws DaoCreationException If it failed to instantiate the dao
	 */
	public static OauthMigrationDao newInstance(Map<String, Object> params) throws DaoCreationException {

		OauthMigrationDao dao = null;
		String className = System.getProperty(MIGRATION_DAO_PROPERTY, DEFAULT_MIGRATION_DAO);
		if (params != null && params.containsKey(MIGRATION_DAO_PROPERTY)) {
			String paramClassName = (String) params.get(MIGRATION_DAO_PROPERTY);
			if (paramClassName != null && paramClassName.length() > 0) {
				className = paramClassName;
			}
		}
		Constructor<OauthMigrationDao> constructor = null;
		try {
			@SuppressWarnings("unchecked")
			Class<OauthMigrationDao> clazz = (Class<OauthMigrationDao>) Class.forName(className);

			// look for a constructor that takes the jdbc template
			constructor = ConstructorUtils.getAccessibleConstructor(clazz, Map.class);
			if (constructor == null) {
				// use default no arg constructor
				constructor = ConstructorUtils.getAccessibleConstructor(clazz);
				if (constructor != null) {
					dao = constructor.newInstance();
				}
			} else {
				dao = constructor.newInstance(params);
			}
		} catch (Exception ex) {
			throw new DaoCreationException("Failed to create the dao with the class: " + className + ", msg; " + ex.getMessage(), ex);
		}
		if (constructor == null) {
			throw new DaoCreationException("The class: " + className + " Did not have a no args constructor nor a constructor taking a Map<String, Object>");
		}
		return dao;
	}

}
