package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.utils.Utils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

/** An in-memory representation of a configuration-file. */
final class Config {
	/** A default output-path. Stores the folder censored PDF-files should be written to. */
	@Nullable
	private final File output;
	/** The logging verbosity. Any log-message with a less specific level will not be logged. */
	@Nullable
	private final Level verbose;
	/**
	 * A list of default colors which will be used to assign a color to regular expressions which were not given a color
	 * by the user.
	 */
	@Nullable
	private final Color[] defaultColors;
	
	/** The default constructor creates an empty ConfigurationParser. That is: all values are set to null. */
	private Config() {
		this(null, null, null);
	}
	
	/**
	 * Initializes a new in-memory configuration from the provided values.
	 *
	 * @param output        the file where the censored file should be stored. Null if not specified.
	 * @param verbose       the level of logging verbosity (encoded as a String or int). Null if not specified.
	 * @param defaultColors a string array containing hexadecimal color codes. Null if not specified.
	 * @see #objectToLevel(Object)
	 */
	@JsonCreator()
	private Config(@Nullable @JsonProperty("output") final File output,
				   @Nullable @JsonProperty("verbose") final Object verbose,
				   @Nullable @JsonProperty("defaultColors") final String[] defaultColors) {
		this.output = output;
		this.verbose = objectToLevel(verbose);
		this.defaultColors = hexArrayToColorArray(defaultColors);
	}
	
	/**
	 * Reads the provided json-formatted config-file and stores its data into a new instance of {@link Config}.
	 *
	 * @param config The configuration file that should be parsed. If null the default configuration (everything null)
	 *               will be returned.
	 * @return An object which contains information about the parsed configuration file.
	 * @throws IOException              If the configuration file couldn't be found.
	 * @throws IllegalArgumentException If the passed config is not a file or does not contain a valid JSON string.
	 */
	@Contract("_ -> new")
	@NotNull
	static Config fromFile(@Nullable final File config) throws IOException {
		if (config == null)
			return new Config();
		Validate.isTrue(config.isFile(), "The configuration file is not a valid JSON-file.");
		try {
			return new ObjectMapper().readValue(config, Config.class);
		} catch (@NotNull JsonParseException | JsonMappingException e) {
			throw new IllegalArgumentException("The configuration file does not contain a valid JSON-string.");
		}
	}
	
	/**
	 * Translates the given object into a {@link Level} or null if it was not possible. {@code verbosity} may be a
	 * string or an int. If it is an int it indexes into {@link Logging#VERBOSITY_LEVELS}. If it is too small OFF is
	 * returned, if it is too large ALL is returned. If the given object is neither a string or an int null is
	 * returned.
	 *
	 * @param verbose the string or integer that should be translated into a level.
	 * @return the level corresponding to the provided verbosity or null if it could not be parsed.
	 */
	@Nullable
	@Contract("null -> null")
	private static Level objectToLevel(@Nullable final Object verbose) {
		Level lvl = null;
		if (verbose instanceof String)
			lvl = Level.getLevel(((String) verbose).toUpperCase());
		else if (verbose instanceof Integer)
			lvl = Logging.VERBOSITY_LEVELS[fitToArray(Logging.VERBOSITY_LEVELS, (int) verbose)];
		return lvl;
	}
	
	/**
	 * Converts an array containing hexadecimal representation of colors into a color array or null if the array was
	 * empty or not present.
	 *
	 * @param hex The String array containing the colors in hexadecimal representation.
	 * @return A color array representing the given hexadecimal color codes or null.
	 */
	@Nullable
	@Contract("null -> null")
	private Color[] hexArrayToColorArray(@Nullable final String[] hex) {
		if (hex == null || hex.length == 0)
			return null;
		// filter should never be necessary
		var stream = Arrays.stream(hex).map(Utils::getColorOrNull).filter(Objects::nonNull);
		return stream.toArray(Color[]::new);
	}
	
	/**
	 * Returns the output-path as it was specified in the loaded config.
	 *
	 * @return The output-path as it was specified in the loaded config. Or null if none was specified.
	 */
	@Contract(pure = true)
	@Nullable
	File getOutput() {
		return this.output;
	}
	
	/**
	 * Returns the verbosity as it was specified in the loaded config.
	 *
	 * @return The verbosity as it was specified in the loaded config. Or null if none was specified.
	 */
	@SuppressWarnings("ReturnPrivateMutableField")// may be suppressed as the Level can not be overwritten.
	@Contract(pure = true)
	@Nullable
	Level getVerbosity() {
		return this.verbose;
	}
	
	/**
	 * Returns the default colors as they were specified (in hexadecimal representation) in the loaded config.
	 *
	 * @return A color array containing the default colors as specified in the loaded config.
	 */
	@Contract(pure = true)
	@Nullable
	Color[] getDefaultColors() {
		return this.defaultColors;
	}
}