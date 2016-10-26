package com.davidmiguel.gobees.video.processors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static com.davidmiguel.gobees.TestUtils.assertMatEqual;
import static com.davidmiguel.gobees.TestUtils.assertMatNotEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for Blur, BacakgroundSubtractor, Morphology and ContourFinder classes.
 * OpenCV 3.1.0 native lib must be on PATH environment variable.
 */
public class ProcessorsTest {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Mat source;
    private Mat sourceContours;
    private Mat targetBlur;
    private Mat result;
    private Mat black;

    private Blur blur;
    private BackgroundSubtractor bs;
    private Morphology morf;
    private ContoursFinder cf;

    @Before
    public void setUp() throws Exception {
        // Image to test
        source = new Mat(4, 4, CvType.CV_8U) {
            {
                put(0, 0, 0,   0,   0, 0);
                put(1, 0, 0, 255, 255, 0);
                put(2, 0, 0, 255, 255, 0);
                put(3, 0, 0,   0,   0, 0);
            }
        };
        // Image with a circle with area=314 (simulating a bee)
        sourceContours = new Mat(480,640,CvType.CV_8U,  new Scalar(0));
        Imgproc.circle(sourceContours, new Point(200, 200), 10, new Scalar(255), -1);
        // Expected result for blur
        targetBlur = new Mat(4, 4, CvType.CV_8U) {
            {
                put(0, 0, 100, 110, 110, 100);
                put(1, 0, 110, 120, 120, 110);
                put(2, 0, 110, 120, 120, 110);
                put(3, 0, 100, 110, 110, 100);
            }
        };
        // Black image
        black = new Mat(4, 4, CvType.CV_8U, new Scalar(0));
        // Instaciate classes
        blur = new Blur();
        bs = new BackgroundSubtractor(); // To test default constructor
        bs = new BackgroundSubtractor(50, 0.7);
        morf = new Morphology();
        cf = new ContoursFinder(); // To test default constructor
        cf = new ContoursFinder(16, 600);
    }

    @Test
    public void testBlur() throws Exception {
        result = blur.process(source);
        assertMatEqual(targetBlur, result);
    }

    @Test
    public void testBackgroundSub() throws Exception {
        // Input 50 equal frames
        for (int i = 0; i < 100; i++) {
            result = bs.process(source);
        }
        // Foreground must be all black (no moving elements)
        assertMatEqual(black, result);
        // Modify one pixel
        Mat mod = source.clone();
        mod.put(2, 2, 0);
        result = bs.process(mod);
        assertMatNotEqual(black, result);
    }

    @Test
    public void testMorphology() throws Exception {
        result = morf.process(source);
        assertMatEqual(black, result);
    }

    @Test
    public void testContoursFinder() throws Exception {
        cf.process(sourceContours);
        int num = cf.getNumBees();
        assertEquals(1, num);
        // Add another bee
        Imgproc.circle(sourceContours, new Point(150, 150), 10, new Scalar(255), -1);
        cf.process(sourceContours);
        num = cf.getNumBees();
        assertEquals(2, num);
        // Add an object with area out of rage
        Imgproc.circle(sourceContours, new Point(300, 300), 15, new Scalar(255), -1);
        cf.process(sourceContours);
        num = cf.getNumBees();
        assertEquals(2, num);
    }

    @Test
    public void testNullMat() throws Exception {
        result = blur.process(null);
        assertNull(result);
        result = bs.process(null);
        assertNull(result);
        result = morf.process(null);
        assertNull(result);
    }

    @Test
    public void testeEmptyMat() throws Exception {
        result = blur.process(new Mat());
        assertNull(result);
        result = bs.process(new Mat());
        assertNull(result);
        result = morf.process(new Mat());
        assertNull(result);
    }

    @After
    public void tearDown() throws Exception {
        source.release();
        sourceContours.release();
        black.release();
        targetBlur.release();
        if (result != null) {
            result.release();
        }
    }
}
