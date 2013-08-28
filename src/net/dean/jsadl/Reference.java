package net.dean.jsadl;

import java.net.MalformedURLException;
import java.net.URL;

/*
 * Reference.java
 *
 * Part of project JSaDL (net.dean.jsadl)
 *
 * Originally created on Aug 19, 2013 by matthew
 */
/**
 * This class represents a reference to a JDK documentation and source.
 * 
 * @author matthew
 * 
 */
public class Reference {

	/**
	 * The base directory of the source files
	 */
	private URL sourceBase;

	/**
	 * The base URL of the documentation
	 */
	private URL docBase;

	/**
	 * Instantiates a new Reference.
	 * 
	 * @param sourceBase
	 *            The base directory of the source files
	 * @param docBase
	 *            The base URL of the documentation
	 * @throws MalformedURLException
	 *             If the given source or doc base are invalid URLs
	 */
	public Reference(String sourceBase, String docBase) throws MalformedURLException {
		if (!sourceBase.startsWith("http://") && !sourceBase.startsWith("https://")) {
			this.sourceBase = new URL("file://" + sourceBase);
		}
		
		// TODO: Look for URL protocol instead
		if (!docBase.startsWith("http://") && !docBase.startsWith("https://")) {
			this.docBase = new URL("file://" + docBase);
		} else {
			this.docBase = new URL(docBase);
		}
	}

	/**
	 * Gets the base source directory
	 * 
	 * @return The base source directory.
	 */
	public URL getSourceBase() {
		return sourceBase;
	}

	/**
	 * Gets the base documentation URL
	 * 
	 * @return The base documentation URL.
	 */
	public URL getDocBase() {
		return docBase;
	}

	/**
	 * Gets a URL based on the given class name and lookup type.
	 * 
	 * @param clazz
	 *            The name of the class to get a URL for
	 * @param type
	 *            The LookupType to use to decide the base directory of of the
	 *            URL
	 * @return A URL that represents the given class name and LookupType
	 */
	public URL getFor(String clazz, LookupType type) {
		try {
			
			return new URL((type == LookupType.SOURCE ? sourceBase.toExternalForm() : docBase.toExternalForm()) + clazz.replace(".", "/")
					+ ((type == LookupType.SOURCE) ? ".java" : ".html"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
