package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * This Class provides the version for {@link CLHelp} and implements {@link picocli.CommandLine.IVersionProvider}
 */
public final class VersionProvider implements CommandLine.IVersionProvider {
	
	/**
	 * Returns the version of the project
	 *
	 * @return project version
	 * @throws IOException if project.properties can not be loaded
	 */
	@NotNull
	@Contract(value = " -> new", pure = true)
	@Override
	public String[] getVersion() throws IOException {
		final Properties properties = new Properties();
		properties.load(Objects.requireNonNull(this.getClass()
												   .getResourceAsStream("/project.properties")));
		return new String[]{"PDF-Zensor",
				"Version: " + properties.getProperty("version"), "Build: " + properties.getProperty("timestamp")};
	}
}

