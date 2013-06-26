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

/**
 * The OauthAccessTokenRecord represents the data for an oauth refresh token record in the db
 * @version $Id: OauthRefreshTokenRecord.java 66463 2013-05-16 14:29:24Z conor.roche $
 * @author conorroche
 */
public class OauthRefreshTokenRecord extends TokenRecord {

	private byte[] authentication;

	/**
	 * This gets the authentication
	 * @return the authentication
	 */
	public byte[] getAuthentication() {
		return this.authentication;
	}

	/**
	 * This sets the authentication
	 * @param authentication the authentication to set
	 */
	public void setAuthentication(byte[] authentication) {
		this.authentication = authentication;
	}

}
