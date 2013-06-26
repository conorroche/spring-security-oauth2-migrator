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

import javax.sql.DataSource;

import org.springframework.security.oauth2.provider.token.JdbcTokenStore;

/**
 * The CustomJdbcTokenStore represents extended jdbc token store
 * purely to expose the extractTokenKey method which can be
 * used to create hashes of tokens
 * @version $Id: CustomJdbcTokenStore.java 66251 2013-05-14 13:56:07Z conor.roche $
 * @author conorroche
 */
public class CustomJdbcTokenStore extends JdbcTokenStore {

	/**
	 * This creates a CustomJdbcTokenStore
	 * @param dataSource
	 */
	public CustomJdbcTokenStore(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.oauth2.provider.token.JdbcTokenStore#extractTokenKey(java.lang.String)
	 */
	@Override
	public String extractTokenKey(String value) {
		return super.extractTokenKey(value);
	}

}