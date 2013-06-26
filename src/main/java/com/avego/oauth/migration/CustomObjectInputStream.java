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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * The CustomObjectInputStream represents an object input stream that resolves
 * classes using a specified class loader when deserializing
 * @version $Id: CustomObjectInputStream.java 66251 2013-05-14 13:56:07Z conor.roche $
 * @author conorroche
 */
public class CustomObjectInputStream extends ObjectInputStream {

	ClassLoader cl;

	/**
	 * This creates a CustomObjectInputStream
	 * @throws IOException
	 * @throws SecurityException
	 */
	public CustomObjectInputStream() throws IOException, SecurityException {
		super();
	}

	/**
	 * This creates a CustomObjectInputStream
	 * @param in The input stream to use
	 * @param cl The class loader to use to resolve classes
	 * @throws IOException
	 */
	public CustomObjectInputStream(InputStream in, ClassLoader cl) throws IOException {
		super(in);
		this.cl = cl;
	}

	/**
	 * {@inheritDoc}
	 * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
	 */
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		return this.cl.loadClass(desc.getName());
	}

}