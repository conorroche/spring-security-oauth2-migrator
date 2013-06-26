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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.xbean.classloader.JarFileClassLoader;

/**
 * The ClassLoaderUtils represents a utility class for class loader functions
 * @version $Id: ClassLoaderUtils.java 66251 2013-05-14 13:56:07Z conor.roche $
 * @author conorroche
 */
public class ClassLoaderUtils {

	/**
	 * This creates a class loader that has jars loaded into it from the given dir
	 * @param libDir The dir with the .jar files
	 * @param parentClasssLoader The parent class loader
	 * @return The class loader
	 */
	public static ClassLoader createClassLoaderWithJars(String libDir, ClassLoader parentClasssLoader) {
		File dependencyDirectory = new File(libDir);
		File[] files = dependencyDirectory.listFiles();
		ArrayList<URL> urls = new ArrayList<URL>();
		for (File file : files) {
			if (file.getName().endsWith(".jar")) {
				try {
					urls.add(file.toURI().toURL());
				} catch (MalformedURLException ex) {
					throw new IllegalStateException("Error creating uri from file: " + file.toURI());
				}
			}
		}
		ClassLoader classLoader = new JarFileClassLoader("Lib CL" + System.currentTimeMillis(), urls.toArray(new URL[urls.size()]), parentClasssLoader);
		return classLoader;
	}

}
