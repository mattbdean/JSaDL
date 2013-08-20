package net.dean.jsadl;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.dean.console.Argument;
import net.dean.console.ConsoleApplication;
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
	private Config config;
	private List<String> args;

	//@formatter:off
	/*
	 * Default values:
	 *  - Lookup Javadoc
	 *  - Use system editor/viewer
	 *  - Use config.ini file
	 *  - Use the reference called "java"
	 * 
	 * Examples:
	 * 
	 * Look up Javadoc of java.util.List:
	 * java -jar <jarname> java.util.List
	 * 
	 * Look up source of java.util.List:
	 * java -jar <jarname> java.util.List (-s | --src)
	 * 
	 * Look up Javadoc of java.util.List with gvim:
	 * java -jar <jarname> java.util.List --viewer=gvim
	 * 
	 * Look up source of java.util.List in a defined Reference in config.ini named alt_jdk
	 * java -jar <jarname> java.util.List --lookup=alt_jdk
	 *
	 * Command line options:
	 * --src, -s
	 * --config=other_config_file.ini
	 * --lookup=reference (default is "java")
	 * --viewer=app
	 * --nocheck
	 * 
	 * Exit codes:
	 * 0 - No Java class given to look up
	 * 3 - IOException when checking for a 200 response code on a document
	 * 
	 */
	//@formatter:on
	public JSaDL(List<Argument> arguments, List<String> args) {
		super(arguments);
		this.args = args;
		
		doLookup();
	}
	
	private void doLookup() {
		if (args.size() == 0) {
			exitAbnormally("No Java class was specified", 1);
		}

		if (args.contains("--help")) {
			printUsage();
			exitNormally();
		}
		// Get the config file
		String configFile = getProperty(args, "--config=");
		this.config = new Config(new File(configFile == null ? "config.ini" : configFile));

		// Get the lookup type
		LookupType type = LookupType.DOC;
		if (args.contains("--src") || args.contains("-s")) {
			type = LookupType.SOURCE;
		}

		String referenceName = getProperty(args, "--lookup=");

		Reference ref = config.getRefFor((referenceName != null) ? referenceName : "java");
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

	private void openWithDefault(URL url) {
		if (!Desktop.isDesktopSupported()) {
			System.err.println("Java Desktop is not supported. Please specify a program and try again.");
			exit(3);
		}

		Desktop d = Desktop.getDesktop();
		try {
			d.browse(url.toURI());
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

	private void openWithProgram(URL url, String program) {
		ProcessBuilder pb = new ProcessBuilder(program, new File(url.getFile()).getAbsolutePath());
		try {
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getProperty(List<String> args, String propertyName) {
		// TODO: Add support for properties with quotes

		for (String arg : args) {
			if (arg.startsWith(propertyName)) {
				return arg.substring(propertyName.length());
			}
		}

		return null;
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
}
