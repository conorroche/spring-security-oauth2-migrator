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
 * The DaoCreationException represents an exception creating a dao
 * @version $Id: DaoCreationException.java 66251 2013-05-14 13:56:07Z conor.roche $
 * @author conorroche
 */
public class DaoCreationException extends Exception {

	/**
	 * This is the serial uid
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This creates a DaoCreationException
	 * @param arg0
	 * @param arg1
	 */
	public DaoCreationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * This creates a DaoCreationException
	 * @param arg0
	 */
	public DaoCreationException(String arg0) {
		super(arg0);
	}

}
