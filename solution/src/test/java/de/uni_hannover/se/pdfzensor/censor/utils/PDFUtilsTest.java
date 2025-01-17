package de.uni_hannover.se.pdfzensor.censor.utils;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestUtility.checkRectanglesEqual;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PDFUtils#transformTextPosition(TextPosition)} and {@link PDFUtils#pdRectToRect2D(PDRectangle)}
 */
class PDFUtilsTest {
	
	/**
	 * A Hash Map containing bundled data as TextPosition objects and the corresponding expected output-rectangle
	 */
	private static final Map<Rectangle2D, TextPositionValue> TEXT_POSITION = new HashMap<>();
	/**
	 * A Hash Map containing an input-rectangle from type PDRectangle and the corresponding expected output-rectangle
	 * from type Rectangle2D
	 */
	private static final Map<Rectangle2D, PDRectangle> DIMENSIONS = new HashMap<>();
	private static TextPositionValue tpValue1 = new TextPositionValue(79.19946f, 800.769f, 10.9091f, 10, 7.51637f,
																	  8.333466f, 3.0545478f, "D", new int[]{68},
																	  PDType1Font.TIMES_ROMAN);
	private static TextPositionValue tpValue2 = new TextPositionValue(23.1547f, 44.32212f, 11.95f, 11, 7.51637f,
																	  8.333466f, 3.0545478f, "DE", new int[]{68, 69},
																	  PDType1Font.TIMES_ROMAN);
	
	static {
		// test with one char in TextPosition
		TEXT_POSITION.put(new Rectangle2D.Float(tpValue1.endX, tpValue1.endY, 7.876370270042557f, 9.796371887116607f),
						  tpValue1);
		// test with two chars in TextPosition
		TEXT_POSITION.put(new Rectangle2D.Float(tpValue2.endX, tpValue2.endY, 15.929350502353941f, 10.731100338419985f),
						  tpValue2);
	}
	
	static {
		// populates the DIMENSIONS-map with key-value pairs of tuples of input and expected output
		DIMENSIONS.put(new Rectangle2D.Float(0f, 0f, 0f, 0f), new PDRectangle(0f, 0f, 0f, 0f));
		DIMENSIONS.put(new Rectangle2D.Float(-1f, -2f, -3f, -4f), new PDRectangle(-1f, -2f, -3f, -4f));
		DIMENSIONS.put(new Rectangle2D.Float(1f, 2f, 3f, 4f), new PDRectangle(1f, 2f, 3f, 4f));
		DIMENSIONS.put(new Rectangle2D.Float(1.5f, 2.5f, 3.5f, 4.5f), new PDRectangle(1.5f, 2.5f, 3.5f, 4.5f));
	}
	
	/**
	 * Provides a set of arguments for {@link #transformTextPositionTest(TextPositionValue, Rectangle2D)} generated from
	 * {@link #TEXT_POSITION}.
	 *
	 * @return An argument stream containing {@link TextPositionValue}s and {@link Rectangle2D.Float}s.
	 */
	private static Stream<Arguments> textPositionProvider() {
		return TEXT_POSITION.entrySet().stream().map(e -> Arguments.of(e.getValue(), e.getKey()));
	}
	
	/**
	 * Provides a set of arguments for {@link #pdRectToRect2DTest(PDRectangle, Rectangle2D)} generated from {@link
	 * #DIMENSIONS}.
	 *
	 * @return An argument stream containing {@link PDRectangle}s and {@link Rectangle2D.Float}s.
	 */
	private static Stream<Arguments> dimensionsProvider() {
		return DIMENSIONS.entrySet().stream().map(e -> Arguments.of(e.getValue(), e.getKey()));
	}
	
	/**
	 * Tests for {@link PDFUtils#transformTextPosition(TextPosition)} function.
	 *
	 * @param input    The input {@link TextPositionValue}.
	 * @param expected The expected {@link Rectangle2D} after the transformation.
	 */
	@ParameterizedTest(name = "Run {index}: TextPosition: {0}")
	@MethodSource("textPositionProvider")
	void transformTextPositionTest(@NotNull TextPositionValue input, @NotNull Rectangle2D expected) {
		// pageRotation = 0 is a standard value
		// pageWidth = 595,276f and pageHeight=841,89f are height and width of a DIN-A4-PDF, but is irrelevant for this test
		TextPosition tp = new TextPosition(0, 595.276f, 841.89f,
										   new Matrix(input.fontSize, 0f, 0f, input.fontSize, input.endX, input.endY),
										   input.endX, input.endY, input.maxHeight, input.individualWidth,
										   input.spaceWidth, input.unicode, input.charCodes, input.font, input.fontSize,
										   input.fontSizeInPt);
		try {
			assertTrue(checkRectanglesEqual(expected, PDFUtils.transformTextPosition(tp), 1e-3));
		} catch (IOException e) {
			fail("IOException: font of TextPosition object couldn't be loaded correctly");
		}
	}
	
	/**
	 * tests for {@link PDFUtils#pdRectToRect2D(PDRectangle)} function
	 *
	 * @param input    The input {@link PDRectangle}.
	 * @param expected The expected {@link Rectangle2D} after the conversion.
	 */
	@ParameterizedTest(name = "Run {index}: Dimensions: {0}")
	@MethodSource("dimensionsProvider")
	void pdRectToRect2DTest(@NotNull PDRectangle input, @NotNull Rectangle2D expected) {
		assertEquals(expected, PDFUtils.pdRectToRect2D(input));
	}
	
	/**
	 * This test function tests the constructor and some null input tests for the functions {@link
	 * PDFUtils#transformTextPosition(TextPosition)} and {@link PDFUtils#pdRectToRect2D(PDRectangle)}
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void nullInputTest() {
		TestUtility.assertIsUtilityClass(PDFUtils.class);
		assertThrows(NullPointerException.class,
					 () -> PDFUtils.pdRectToRect2D(null)); // ignore SonarLint because we wan't that bad input
		assertThrows(NullPointerException.class,
					 () -> PDFUtils.transformTextPosition(null)); // ignore SonarLint because we wan't that bad input
		
		TextPosition tp = new TextPosition(0, 0f, 0f, new Matrix(0f, 0f, 0f, 0f, 0f, 0f), 0f, 0f, 0f, 0f, 0f, "",
										   new int[]{}, null, 0f, 0);
		assertThrows(NullPointerException.class, () -> PDFUtils.transformTextPosition(tp));
	}
	
	/**
	 * New data structure TextPositionValue to bundle the values for transformTextPosition
	 */
	static class TextPositionValue {
		float endX;
		float endY;
		float fontSize;
		int fontSizeInPt;
		float maxHeight;
		float individualWidth;
		float spaceWidth;
		String unicode;
		int[] charCodes;
		PDFont font;
		
		TextPositionValue(float endX, float endY, float fontSize, int fontSizeInPt, float maxHeight,
						  float individualWidth, float spaceWidth, String unicode, int[] charCodes, PDFont font) {
			this.endX = endX;
			this.endY = endY;
			this.fontSize = fontSize;
			this.fontSizeInPt = fontSizeInPt;
			this.maxHeight = maxHeight;
			this.individualWidth = individualWidth;
			this.spaceWidth = spaceWidth;
			this.unicode = unicode;
			this.charCodes = charCodes;
			this.font = font;
		}
	}
}