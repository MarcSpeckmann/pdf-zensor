package de.uni_hannover.se.pdfzensor;


import de.uni_hannover.se.pdfzensor.censor.PDFCensor;
import de.uni_hannover.se.pdfzensor.config.CLErrorMessageHandler;
import de.uni_hannover.se.pdfzensor.config.CLHelp;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import picocli.CommandLine;

import java.io.IOException;

public class App {
	/** The Logger-instance instances of this class should log their output into. */
	private static final Logger LOGGER = Logging.getLogger();
	
	@SuppressWarnings("squid:S106")// we explicitly want to print to stderr here instead of logging
	public static void main(String... args) {
		try {
			if (!CLHelp.printStandardHelpOptionsIfRequested(args)) {
				final var settings = new Settings(null, args);
				final var censor = new PDFCensor(settings);
				final var processor = new PDFProcessor(censor);
				try (final var doc = PDDocument.load(settings.getInput())) {
					processor.process(doc);
					doc.save(settings.getOutput());
				}
			}
		} catch (CommandLine.ParameterException ex) {
			CLErrorMessageHandler handler = new CLErrorMessageHandler();
			LOGGER.error(ex);
			System.exit(handler.handleParseException(ex, args));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			LOGGER.error(e);
			System.exit(-1);
		}
	}
}
