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

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

/**
 * The OracleOauthMigrationDao represents an oracle jdbc template implementation
 * of the oauth migration dao
 * @version $Id: MysqlOauthMigrationDao.java 66463 2013-05-16 14:29:24Z conor.roche $
 * @author conorroche
 */
public class OracleOauthMigrationDao extends JdbcOauthMigrationDao {

	/**
	 * This creates a MysqlOauthMigrationDao
	 * @param params The dao params
	 * @throws SQLException
	 */
	public OracleOauthMigrationDao(Map<String, Object> params) throws SQLException {
		super(params);
	}

	/**
	 * {@inheritDoc}
	 * @throws SQLException
	 * @see com.avego.oauth.migration.JdbcOauthMigrationDao#buildDataSource()
	 */
	@Override
	protected DataSource buildDataSource() throws SQLException {
		OracleDataSource ds = new OracleDataSource();
		ds.setURL(getJdbcUrl());
		ds.setUser(getUser());
		ds.setPassword(getPass());
		return ds;
	}
}
