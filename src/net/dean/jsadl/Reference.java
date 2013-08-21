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
	private String sourceBase;

	/**
	 * The base URL of the documentation
	 */
	private String docBase;

	/**
	 * Instantiates a new Reference.
	 * 
	 * @param sourceBase
	 *            The base directory of the source files
	 * @param docBase
	 *            The base URL of the documentation
	 */
	public Reference(String sourceBase, String docBase) {
		if (!sourceBase.startsWith("http://") && !sourceBase.startsWith("https://")) {
			sourceBase = "file://" + sourceBase;
		}
		if (!docBase.startsWith("http://") && !docBase.startsWith("https://")) {
			docBase = "file://" + docBase;
		}
		this.sourceBase = sourceBase;
		this.docBase = docBase;
	}

	/**
	 * Gets the base source directory
	 * 
	 * @return A String representation of the base source directory.
	 */
	public String getSourceBase() {
		return sourceBase;
	}

	/**
	 * Gets the base documentation URL
	 * 
	 * @return A String representation of the base documentation URL.
	 */
	public String getDocBase() {
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
			return new URL(new URL(type == LookupType.SOURCE ? sourceBase : docBase), clazz.replace(".", "/")
					+ ((type == LookupType.SOURCE) ? ".java" : ".html"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
