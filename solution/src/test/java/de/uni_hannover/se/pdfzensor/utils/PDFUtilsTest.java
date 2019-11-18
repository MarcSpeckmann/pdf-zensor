package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.TestUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static org.junit.jupiter.api.Assertions.*;

class PDFUtilsTest {
	
	@BeforeEach
	void setUp() {
	}
	
	@AfterEach
	void tearDown() {
	}
	
	@Test
	void fitToArray() {
		TestUtility.assertIsUtilityClass(PDFUtils.class);
		assertEquals(PDFUtils.fitToArray(VERBOSITY_LEVELS, 0), 0);
		assertEquals(PDFUtils.fitToArray(VERBOSITY_LEVELS, -1), 0);
		assertEquals(PDFUtils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length + 1), VERBOSITY_LEVELS.length);
		assertEquals(PDFUtils.fitToArray(VERBOSITY_LEVELS, VERBOSITY_LEVELS.length), VERBOSITY_LEVELS.length);
		assertEquals(PDFUtils.fitToArray(VERBOSITY_LEVELS, 3), 3);
		//TODO: Add some more test with autogenerated arrays
		assertThrows(IllegalArgumentException.class, () -> PDFUtils.fitToArray(null, 1));
		assertThrows(IllegalArgumentException.class, () -> PDFUtils.fitToArray(null, -1));
		assertThrows(IllegalArgumentException.class, () -> PDFUtils.fitToArray(null, 0));
	}
}