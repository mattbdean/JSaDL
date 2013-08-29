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
import net.dean.jsadl.Configurator.Config;
import net.dean.parsers.ini.IniSyntaxException;
import net.dean.util.CollectionUtils;
import net.dean.util.InternetUtils;
import net.dean.util.file.FileUtil;

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
	public static final String CONFIG_FILE_NAME = "config.ini";
	/**
	 * The Config object that reads and parses the {@value #CONFIG_FILE_NAME}
	 * file and gets URLs for JSaDL.
	 */
	private Configurator configurator;

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
		File configFile = new File(configFileString == null || configFileString.isEmpty() ? CONFIG_FILE_NAME
				: configFileString);
		try {
			this.configurator = new Configurator(this);
			configurator.configure(configFile);
		} catch (FileNotFoundException e) {
			exitAbnormally(e, 20);
		} catch (IOException e) {
			exitAbnormally(e, 21);
		} catch (IniSyntaxException e) {
			exitAbnormally(e, 22);
		}

		// Get the lookup type
		LookupType type = LookupType.DOC;
		if (args.contains("--source") || args.contains("-s")) {
			type = LookupType.SOURCE;
		}

		String referenceName = getProperty(args, "--lookup=");

		Reference ref = null;
		try {
			ref = referenceName != null ? configurator.getConfig().getRefFor(referenceName) : getDefaultReference();
		} catch (MalformedURLException e) {
			exitAbnormally(e, 5);
		}

		// If 'java' doesn't exist
		ref = getDefaultReference();

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
			if (!openWithProgram(target, viewer)) {
				exitAbnormally("The program \"" + viewer + "\" could not be found or is not in the PATH.", 30);
			}
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
		String program = null;

		// Try to determine the program to use
		switch (FileUtil.getOS()) {
		case LINUX:
		case SOLARIS:
			// Linux uses xdg-open
			if (FileUtil.isInPath("xdg-open")) {
				program = "xdg-open";
			} else {
				if (new File("/usr/bin/xdg-open").exists()) {
					program = "/usr/bin/xdg-open";
				}
			}
			break;
		case MAC:
			program = "open";
			break;
		case WINDOWS:
			// Windows uses "cmd.exe /c"
			if (FileUtil.isInPath("cmd.exe")) {
				program = "cmd.exe /c";
			} else {
				if (new File("C:\\Windows\\System32\\cmd.exe").exists()) {
					program = "C:\\Windows\\System32\\cmd.exe /c";
				}
			}
			break;
		default:
			// This applies to OperatingSystem.UNKNOWN or any other new OS enums
			// added in the future
			break;
		}

		if (program != null) {
			if (openWithProgram(url, program)) {
				// Everything went smoothly
				exitNormally();
			}

			// Eh, not so much. Try and use Desktop.
		}

		if (!Desktop.isDesktopSupported()) {
			exitAbnormally("Java Desktop is not supported. Please specify a program and try again.", 2);
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
	private boolean openWithProgram(URL url, String program) {
		ProcessBuilder pb = new ProcessBuilder().command(program, url.toExternalForm());
		try {
			Process p = pb.start();
			// Make sure it starts
			int exit = p.waitFor();
			System.out.printf("%s exited with code %s\n", program, exit);
			return true;
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			return false;
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

	private Reference getDefaultReference() {
		Config config = configurator.getConfig();
		// Look for the java reference
		if (config.getIniFile().hasSection("java")) {
			try {
				return config.getRefFor("java");
			} catch (MalformedURLException e) {
				exitAbnormally(e, 5);
			}
		}

		// Look for the first reference (if there are any)
		if (config.getIniFile().getSections().size() > 0) {
			try {
				return config.getRefFor(config.getIniFile().getSections().get(0).getName());
			} catch (MalformedURLException e) {
				exitAbnormally(e, 5);
			}
		}

		String source = null, docs = null;

		// File f;
		// if ((f = new File("docs/")).exists()) {
		// docs = "file://" + f.getAbsolutePath();
		// } else if (((f = new File(System.getProperty("java.home") +
		// "src/")).exists())) {
		//
		// }
		List<File> folders = new ArrayList<>();
		folders.add(new File(System.getProperty("user.dir")));
		folders.add(new File(System.getProperty("java.home")));
		folders.add(new File(System.getenv("JAVA_HOME")));

		File sourceFolder = null;
		for (File f : folders) {
			if ((sourceFolder = new File(f, "src/")).exists()) {
				source = sourceFolder.getAbsolutePath();
				break;
			}
		}

		if (sourceFolder == null) {
			exitAbnormally(
					"Unable to find a source folder for the default Reference. Please add at least one Reference to your "
							+ CONFIG_FILE_NAME + ".", 4);
		}

		File docsFolder = null;
		for (File f : folders) {
			if ((sourceFolder = new File(f, "doc/")).exists()) {
				source = sourceFolder.getAbsolutePath();
				break;
			}
		}
		if (docsFolder == null) {
			// Resort to online
			docs = "http://docs.oracle.com/javase/7/docs/api/";
		}

		try {
			return new Reference(source, docs);
		} catch (MalformedURLException e) {
			exitInternalError(e, "Malformed URL while generating default Reference: " + e.getLocalizedMessage());
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
		arguments.add(new Argument("-s", "--source", "Shows the source code instead of the Javadoc"));
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
