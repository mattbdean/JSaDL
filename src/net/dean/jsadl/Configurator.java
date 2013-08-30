package net.dean.jsadl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.dean.console.InputFilter;
import net.dean.parsers.ini.IniElement;
import net.dean.parsers.ini.IniFile;
import net.dean.parsers.ini.IniFileFactory;
import net.dean.parsers.ini.IniFileTransformer;
import net.dean.parsers.ini.IniSyntaxException;
import net.dean.parsers.ini.Section;
import net.dean.util.InternetUtils;

/*
 * Configurator.java
 *
 * Part of project JSaDL (net.dean.jsadl)
 *
 * Originally created on Aug 28, 2013 by matthew
 */
public class Configurator {
	public static final String DEFAULT_SRC = System.getProperty("java.home") + "/src";
	public static final String DEFAULT_DOC = "http://docs.oracle.com/javase/7/docs/api/";

	private JSaDL saddle;
	private Config config;

	public Configurator(JSaDL saddle) {
		this.saddle = saddle;
		// This will remain null until configure() is called.
		this.config = null;
	}

	public void configure(File iniFile) throws IOException, IniSyntaxException {
		//@formatter:off
		/*
		 * PROCESS PSEUDOCODE:
		 * if config file exists
		 *     try to create an IniFile object
		 *     if it has at least one valid reference (contains a src and doc key and they have values)
		 *         job is done
		 *     else if it has either a missing src or doc key
		 *         get a src and/or doc values
		 *             if those values don't exist or does not return a 200 code
		 *                 warn the user
		 *                 ask if they really want to add these sources
		 *         save them to the config file
		 *         job is done
		 *     else (there is no valid reference)
		 *         get the src and doc keys
		 *         if those keys and values don't exist or does not return a 200 code
		 *                 warn the user
		 *                 ask if they really want to add these sources
		 *         save them to the config file
		 *         job is done
		 * else (the config file does not exist)
		 *     get the src and doc keys
		 *         if those keys and values don't exist or does not return a 200 code
		 *                 warn the user
		 *                 ask if they really want to add these sources
		 *         save them to the config file
		 *         job is done
		 */
		//@formatter:on
		if (iniFile.exists()) {
			IniFile ini = new IniFileFactory().build(iniFile);

			// If the ini file has more than one section
			if (ini.getSections().size() > 0) {
				// Try to find a valid section
				for (Section s : ini.getSections()) {
					if (s.get("src") != null && s.get("doc") != null) {
						// Section has a valid src and doc key/value pair
						this.config = new Config(iniFile);
						return;
					}
				}

				// At this point none of the sections represent valid
				// References.
				// We have to make a new one.
				getAndExportRef(iniFile);
				// Just in case
				return;
			}
		} else {
			// Config file does not exist, ask for values for doc and source,
			// and also name of default reference
			getAndExportRef(iniFile);
		}
	}

	private void getAndExportRef(File iniFile) {
		// Here we make a new Reference from user input.
		String doc = getLocation("documentation", DEFAULT_SRC);
		String src = getLocation("source", DEFAULT_DOC);

		System.out.printf("Please choose a name for your Reference. Press enter for [%s]\n", "java");
		String ref = saddle.getInput(new InputFilter() {

			@Override
			public boolean accept(String input) {
				return true;
			}
		}, "java");
		
		// Save the values to the Ini file
		exportValues(src, doc, ref, iniFile);
		try {
			// Instantiate the Config
			config = new Config(iniFile);
			return;
		} catch (IOException | IniSyntaxException e) {
			e.printStackTrace();
		}
	}

	private void exportValues(String src, String doc, String refName, File f) {
		IniFile ini = null;
		if (f.exists()) {
			try {
				ini = new IniFileFactory().build(f);
			} catch (IniSyntaxException | IOException e) {
				// TODO: More specific handling. Maybe an IniSyntaxException
				// causes this method to just create a new file
				e.printStackTrace();
			}
		} else {
			ini = new IniFileFactory().newIniFile();
		}

		Section s = null;
		if (ini.hasSection(refName)) {
			s = ini.getSection(refName);
			// Remove the section so we can add it again
			// after the values have been corrected
			ini.getSections().remove(s);
		} else {
			s = new Section(refName);
		}

		if (!s.hasKey("src") && src != null) {
			s.put(new IniElement("src", src));
		}
		if (!s.hasKey("doc") && doc != null) {
			s.put(new IniElement("doc", doc));
		}
		ini.getSections().add(s);

		IniFileTransformer.recommended().export(ini, f);
	}

	private String getLocation(String name, String defaultValue) {
		System.out.printf("Please choose a %s location for your reference. Press enter for [%s]\n", name, defaultValue);
		return saddle.getInput(new InputFilter() {

			@Override
			public boolean accept(String input) {
				try {
					URL url = null;
					if (input.startsWith("http://") || input.startsWith("https://")) {
						url = new URL(input);
					} else {
						url = new URL("file://" + input);
					}

					// Checking for files
					if (url.getProtocol().equals("file")) {
						// If the directory does not exist or if it isn't a
						// directory at all
						if (!new File(url.getFile()).exists() || !new File(url.getFile()).isDirectory()) {
							// Prompt the user if they really want to use it
							return (saddle.getYesNoInput(String.format(
									"%s doesn't seem to exist. Do you still want to use it? (y/n)", url.getFile())));
						} else {
							// The directory exists, add it
							return true;
						}
					} else {
						// HTTP or HTTPS
						if (InternetUtils.doGetRequest(url.toExternalForm()).getResponseCode() != 200) {
							// If the GET request doesn't return a 200, ask the
							// user if they really want to use it
							return (saddle.getYesNoInput(String.format(
									"%s doesn't appear to be online.  Do you still want to use it? (y/n)",
									url.toExternalForm())));
						} else {
							// Returned 200, all is well
							return true;
						}
					}
				} catch (MalformedURLException e) {
					// TODO: Better method than this
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}

			}
		}, defaultValue);
	}

	public Config getConfig() {
		return config;
	}

	/**
	 * This class reads and parses the data from a given file and, when
	 * requested, return a {@link Reference} from that file.
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
			this(new File(JSaDL.CONFIG_FILE_NAME));
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
		 * @throws MalformedURLException
		 *             If the given source or doc base are invalid URLs
		 */
		public Reference getRefFor(String name) throws MalformedURLException {
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

		/**
		 * Gets the IniFile associated with this Config.
		 * 
		 * @return The IniFile
		 */
		public IniFile getIniFile() {
			return iniFile;
		}
	}
}
