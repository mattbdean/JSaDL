package net.dean.jsadl;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.dean.console.Argument;
import net.dean.console.ConsoleApplication;
import net.dean.parsers.ini.IniSyntaxException;
import net.dean.util.CollectionUtils;
import net.dean.util.InternetUtils;

/*
 * JSadl.java
 *
 * Part of project JSaDL (net.dean.jsadl)
 *
 * Originally created on Aug 19, 2013 by matthew
 */

/**
 * The main class of Java Source and Documentation Lookup.
 * 
 * @author Matthew Dean
 * 
 */
public class JSaDL extends ConsoleApplication {
	/**
	 * The Config object that reads and parses the config.ini file and gets URLs
	 * for JSaDL.
	 */
	private Config config;

	/**
	 * A list of arguments passed to the application
	 */
	private List<String> args;

/**
	 * Instantiates a new JSaDL
	 * 
	 * @param arguments A list of Arguments used to satisfy {@link ConsoleApplication#ConsoleApplication(List))
	 * @param args The arguments passed to this application
	 */
	public JSaDL(List<Argument> arguments, List<String> args) {
		super(arguments);
		this.args = args;
	}

	/**
	 * Does the lookup with the given arguments
	 */
	private void doLookup() {
		if (args.size() == 0) {
			exitAbnormally("No Java class was specified", 1);
		}

		if (args.contains("--help")) {
			printUsage();
			exitNormally();
		}
		// Get the config file
		String configFileString = getProperty(args, "--config=");
		File configFile = new File(configFileString == null || configFileString.isEmpty() ? "config.ini" : configFileString);
		try {
			this.config = new Config(configFile);
		} catch (FileNotFoundException e) {
			exitAbnormally(e, 20);
		} catch (IOException e) {
			exitAbnormally(e, 21);
		} catch (IniSyntaxException e) {
			exitAbnormally(e, 22);
		}

		// Get the lookup type
		LookupType type = LookupType.DOC;
		if (args.contains("--src") || args.contains("-s")) {
			type = LookupType.SOURCE;
		}

		String referenceName = getProperty(args, "--lookup=");

		Reference ref = null;
		try {
			ref = config.getRefFor((referenceName != null) ? referenceName : "java");
		} catch (MalformedURLException e) {
			exitAbnormally(e, 30);
		}
		URL target = ref.getFor(args.get(0), type);

		String protocol = target.getProtocol();
		boolean exists = true;
		if (!args.contains("--nocheck")) {
			// Local file

			if (protocol.equals("file")) {
				// If it doesn't exist
				if (!new File(target.getFile()).exists()) {
					exists = false;
				}
			} else if (protocol.equals("http") || protocol.equals("https")) {
				try {
					if (!(InternetUtils.doGetRequest(target.toExternalForm()).getResponseCode() == 200)) {
						exists = false;
					}
				} catch (MalformedURLException e) {
					exitInternalError(e, "Bad URL: " + target.toExternalForm());
				} catch (IOException e) {
					exitAbnormally(
							"An IOException occured when testing the availablity of the document. Use --nocheck to disable this.",
							3);
				}
			}
		}

		if (!exists) {
			int exitCode = 10;
			if (protocol.equals("http") || protocol.equals("https")) {
				exitCode = 11;
			} else if (protocol.equals("file")) {
				exitCode = 12;
			}
			exitAbnormally(args.get(0) + " could not be found.", exitCode);
		}

		// TODO: For methods/fields of online (http/https) references, use
		// #method(). For instance, ${doc}/java/lang/Object#equals(Object)

		String viewer = getProperty(args, "--viewer=");
		if (viewer == null) {
			openWithDefault(target);
		} else {
			openWithProgram(target, viewer);
		}
	}

	/**
	 * Opens the specified URL with the system default viewer/editor.
	 * 
	 * @param url
	 *            The URL to browse to
	 * @see java.awt.Desktop#browse(java.net.URI)
	 */
	private void openWithDefault(URL url) {
		if (!Desktop.isDesktopSupported()) {
			System.err.println("Java Desktop is not supported. Please specify a program and try again.");
			exit(3);
		}

		Desktop d = Desktop.getDesktop();
		try {
			if (url.getProtocol().equals("file")) {
				d.open(new File(url.toURI()));
				return;
			}
			d.browse(url.toURI());
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts a new process with this structure:
	 * 
	 * <blockquote><code>
	 * {@code <viewer> <url>}
	 * </code></blockquote>
	 * 
	 * @param url
	 *            The URL to browse to
	 * @param program
	 *            The program to use
	 */
	private void openWithProgram(URL url, String program) {
		ProcessBuilder pb = new ProcessBuilder(program, new File(url.getFile()).getAbsolutePath());
		try {
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the value of a property from the command line arguments. For
	 * example, if the argument <code>--viewer=vim</code> was passed, then when
	 * this method is called like this:
	 * <code>getProperty(args, "--viewer=")</code>, this method would return
	 * <code>vim</code>.
	 * 
	 * @param args
	 *            The arguments to search
	 * @param propertyName
	 *            The full name and key of the property
	 * @return The value of the given key
	 */
	private String getProperty(List<String> args, String propertyName) {
		// TODO: Add support for properties with quotes

		for (String arg : args) {
			if (arg.startsWith(propertyName)) {
				return arg.substring(propertyName.length());
			}
		}

		return null;
	}

	@Override
	protected String getMaker() {
		return "thatJavaNerd";
	}

	@Override
	protected String getVersion() {
		return "1.0";
	}

	@Override
	protected String getProjectName() {
		return "JSaDL";
	}

	@Override
	protected void printUsage() {
		printSampleIntro();
		System.out.println("You can find the source to this project at https://github.com/thatJavaNerd/JSaDL.git");
		printArguments();
	}

	public static void main(String[] args) {
		List<Argument> arguments = new ArrayList<>();
		arguments.add(new Argument("-s", "--src", "Shows the source code instead of the Javadoc"));
		arguments.add(new Argument("", "--config=<file>", "Uses a different configuration file"));
		arguments.add(new Argument("", "--lookup=<reference>", "Sets the name of the reference to use"));
		arguments.add(new Argument("", "--viewer=<app>", "Uses a program to view the file instead of the system default"));
		arguments.add(new Argument("", "--nocheck",
				"Disables checking for an existing file/200 HTTP response before trying to view the document"));
		JSaDL saddle = new JSaDL(arguments, CollectionUtils.toCollection(args));
		saddle.setSupportSite("https://github.com/thatJavaNerd/JSaDL/issues");
		saddle.doLookup();
	}
}
