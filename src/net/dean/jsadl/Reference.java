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
public class Reference {
	private String sourceBase;
	private String docBase;
	
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

	public String getSourceBase() {
		return sourceBase;
	}

	public String getDocBase() {
		return docBase;
	}
	
	public URL getFor(String clazz, LookupType type) {
		try {
			return new URL(new URL(type == LookupType.SOURCE ? sourceBase : docBase), clazz.replace(".", "/") + ((type == LookupType.SOURCE) ? ".java" : ".html"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
