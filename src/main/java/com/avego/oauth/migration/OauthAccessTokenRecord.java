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
 * The OauthAccessTokenRecord represents the data for an oauth access token record in the db
 * @version $Id: OauthAccessTokenRecord.java 66463 2013-05-16 14:29:24Z conor.roche $
 * @author conorroche
 */
public class OauthAccessTokenRecord extends TokenRecord {

	private String authenticationId;
	private String userName;
	private String clientId;
	private byte[] authentication;
	private String refreshToken;

	/**
	 * This gets the authenticationId
	 * @return the authenticationId
	 */
	public String getAuthenticationId() {
		return this.authenticationId;
	}

	/**
	 * This sets the authenticationId
	 * @param authenticationId the authenticationId to set
	 */
	public void setAuthenticationId(String authenticationId) {
		this.authenticationId = authenticationId;
	}

	/**
	 * This gets the userName
	 * @return the userName
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * This sets the userName
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * This gets the clientId
	 * @return the clientId
	 */
	public String getClientId() {
		return this.clientId;
	}

	/**
	 * This sets the clientId
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

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

	/**
	 * This gets the refreshToken
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return this.refreshToken;
	}

	/**
	 * This sets the refreshToken
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

}
