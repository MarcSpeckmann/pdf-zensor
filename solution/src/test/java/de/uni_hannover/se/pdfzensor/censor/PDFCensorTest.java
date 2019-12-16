package de.uni_hannover.se.pdfzensor.censor;

import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFHandler;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.PDFCensorBoundingBoxProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

class PDFCensorTest implements PDFHandler {
	/** Acts as a super instance. */
	private PDFCensor properCensor;
	
	/**
	 * The number of processed TextPositions. Does <b>not</b> equal the size of the bounds-color pair list, since
	 * elements' bounds might get combined.
	 */
	private int element;
	
	/** The number of total elements the bounds-pair list should contain after all combinations. */
	private int finalExpectedElements;
	
	/** The bounds of the individual elements of the PDF-file (not combined). */
	private Rectangle2D.Double[] elements;
	
	/**
	 * Returns the list of bounds-color pairs of the instance.
	 *
	 * @param fromInstance The instance from which the list should be retrieved.
	 * @return The bounds-color pair list of the given instance.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static List<ImmutablePair<Rectangle2D, Color>> getBoundingBoxes(@NotNull PDFCensor fromInstance) {
		try {
			var boundingBoxesField = PDFCensor.class.getDeclaredField("boundingBoxes");
			boundingBoxesField.setAccessible(true);
			return (List<ImmutablePair<Rectangle2D, Color>>) boundingBoxesField.get(fromInstance);
		} catch (Exception e) {
			Assertions.fail("Could not retrieve the bounds-color pair list.", e);
		}
		return null;
	}
	
	/** Checks for invalid settings argument. */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testInvalidSettings() {
		Assertions.assertThrows(NullPointerException.class, () -> new PDFCensor(null));
	}
	
	/**
	 * Checks if the elements in the PDF-file equal the given elements and are added to the bounds-color-list
	 * correctly.
	 *
	 * @param input                 The input PDF-file to check.
	 * @param elements              The elements in the input PDF-file.
	 * @param finalExpectedElements The expected length of the bounds-pair list at the end of a page after all
	 *                              combinations have been applied.
	 * @throws IOException If the document could not be loaded.
	 */
	@ParameterizedTest(name = "Run {index}: pdf: {0}, elements: {1}, finalExpectedElements {2}")
	@ArgumentsSource(PDFCensorBoundingBoxProvider.class)
	void testPDFCensor(@NotNull String input, @NotNull Rectangle2D.Double[] elements,
					   int finalExpectedElements) throws IOException {
		var dummySettings = new Settings(null, input);
		this.properCensor = new PDFCensor(dummySettings);
		this.elements = elements;
		this.element = 0;
		this.finalExpectedElements = finalExpectedElements;
		
		final var dummyProcessor = new PDFProcessor(this);
		try (final var doc = PDDocument.load(dummySettings.getInput())) {
			dummyProcessor.process(doc);
		}
	}
	
	/**
	 * Compares the bounds of two rectangles with consideration to a small error margin.
	 *
	 * @param expected The expected rectangle bounds.
	 * @param actual   The actual rectangle bounds.
	 * @return True if the bounds of the rectangles are equal according to the margin, false otherwise.
	 */
	private boolean checkRectanglesEqual(@NotNull Rectangle2D expected, @NotNull Rectangle2D actual) {
		var range = 1 / 1000000.0;
		Objects.requireNonNull(expected);
		Objects.requireNonNull(actual);
		return (range > Math.abs(expected.getX() - actual.getX())) &&
			   (range > Math.abs(expected.getY() - actual.getY())) &&
			   (range > Math.abs(expected.getWidth() - actual.getWidth())) &&
			   (range > Math.abs(expected.getHeight() - actual.getHeight()));
	}
	
	@Override
	public void beginDocument(final PDDocument doc) {
		Objects.requireNonNull(properCensor);
		Assertions.assertNull(getBoundingBoxes(properCensor));
		
		properCensor.beginDocument(doc);
		
		var listAfter = getBoundingBoxes(properCensor);
		Assertions.assertNotNull(listAfter);
		Assertions.assertTrue(listAfter.isEmpty());
	}
	
	@Override
	public void beginPage(final PDDocument doc, final PDPage page, final int pageNum) {
		Objects.requireNonNull(properCensor);
		
		properCensor.beginPage(doc, page, pageNum);
		
		var listAfter = getBoundingBoxes(properCensor);
		Assertions.assertNotNull(listAfter);
		Assertions.assertTrue(listAfter.isEmpty());
	}
	
	@Override
	public void endPage(final PDDocument doc, final PDPage page, final int pageNum) {
		Objects.requireNonNull(properCensor);
		Assertions.assertNotNull(getBoundingBoxes(properCensor));
		// Checks if the expected number of elements have been combined
		// (requires colors to be added because differently colored elements should not be combined)
		
		Assertions.assertEquals(finalExpectedElements, Objects.requireNonNull(getBoundingBoxes(properCensor)).size());
		
		properCensor.endPage(doc, page, pageNum);
		try {
			Assertions.assertTrue(page.getAnnotations().isEmpty());
		} catch (IOException e) {
			Assertions.fail(e.getMessage());
		}
		
	}
	
	@Override
	public void endDocument(final PDDocument doc) {
		Objects.requireNonNull(properCensor);
		Assertions.assertNotNull(getBoundingBoxes(properCensor));
		
		properCensor.endDocument(doc);
		
		Assertions.assertNull(getBoundingBoxes(properCensor));
	}
	
	@Override
	public boolean shouldCensorText(final TextPosition pos) {
		Objects.requireNonNull(properCensor);
		Assertions.assertTrue(element < elements.length, "Not all elements were listed for comparison.");
		
		var listBefore = Objects.requireNonNull(getBoundingBoxes(properCensor));
		var sizeBefore = listBefore.size();
		var oldLast = (sizeBefore > 0) ? listBefore.get(sizeBefore - 1) : null;
		
		var actual = properCensor.shouldCensorText(pos);
		
		var listAfter = Objects.requireNonNull(getBoundingBoxes(properCensor));
		var sizeAfter = listAfter.size();
		Assertions.assertTrue(sizeAfter > 0);
		var newLast = listAfter.get(sizeAfter - 1);
		
		// Colors differ, expect element to be added instead of combined.
		if (oldLast != null && !oldLast.getRight().equals(newLast.getRight()))
			Assertions.assertEquals(sizeBefore + 1, sizeAfter);
		
		var expBounds = elements[element];
		if (sizeBefore == sizeAfter) // element was extended
			expBounds = (Rectangle2D.Double) elements[element].createUnion(oldLast.getLeft());
		Assertions.assertTrue(checkRectanglesEqual(expBounds, newLast.getLeft()));
		
		element++;
		return actual;
	}
}