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
/**
 * This class reads and parses the data from a given file and, when requested,
 * return a {@link Reference} from that file.
 * 
 * @author matthew
 * 
 */
public class Config {
	/**
	 * The IniFile that stores all the
	 */
	private IniFile iniFile;

	/**
	 * Instantiates a new Config
	 * 
	 * @throws FileNotFoundException
	 *             If the file does not exist
	 * @throws IOException
	 *             If there was a problem reading the file
	 * @throws IniSyntaxException
	 *             If there was a syntax error in the file
	 */
	public Config() throws FileNotFoundException, IOException, IniSyntaxException {
		this(new File("config.ini"));
	}

	/**
	 * Instantiates a new Config with the given file.
	 * 
	 * @param configIni
	 *            The file to parse
	 * @throws FileNotFoundException
	 *             If the file does not exist
	 * @throws IOException
	 *             If there was a problem reading the file
	 * @throws IniSyntaxException
	 *             If there was a syntax error in the file
	 */
	public Config(File configIni) throws FileNotFoundException, IOException, IniSyntaxException {
		iniFile = new IniFileFactory().build(configIni);
	}

	/**
	 * Constructs a {@link Reference} from the given name.
	 * 
	 * @param name
	 *            The name of the reference to get
	 * @return A new {@link Reference} based on the name, or null if the
	 *         reference does not exist.
	 */
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
