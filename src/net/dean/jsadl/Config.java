package net.dean.jsadl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.dean.parsers.ini.IniFile;
import net.dean.parsers.ini.IniFileFactory;
import net.dean.parsers.ini.IniSyntaxException;
import net.dean.parsers.ini.Section;

/*
 * Config.java
 *
 * Part of project JSaDL (net.dean.jsadl)
 *
 * Originally created on Aug 19, 2013 by matthew
 */
public class Config {
	private IniFile iniFile;
	
	public Config() throws FileNotFoundException, IOException, IniSyntaxException {
		this(new File("config.ini"));
	}
	
	public Config(File configIni) throws FileNotFoundException, IOException, IniSyntaxException {
			iniFile = new IniFileFactory().build(configIni);
	}
	
	public Reference getRefFor(String name) {
		if (iniFile.hasSection(name)) {
			Section section = iniFile.getSection(name);
			String src = null, doc = null;
			if (section.hasKey("src")) {
				src = section.get("src");
			}
			if (section.hasKey("doc")) {
				doc = section.get("doc");
			}
			
			return new Reference(src, doc);
		}
		
		return null;
	}
}
