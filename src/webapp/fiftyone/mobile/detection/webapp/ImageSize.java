/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.webapp;

/**
 *
 * @author tom
 */
public class ImageSize {

    private int width;
    private int height;
    private final int maxWidth;
    private final int maxHeight;
    private final int factor;

    public ImageSize(final int width, final int height, final int maxWidth, final int maxHeight, final int factor) {
        this.width = width;
        this.height = height;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.factor = factor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resolveSize() {
        // Check that no dimensions are above the specifed max.
        if (height > maxHeight || width > maxWidth) {
            if (height > width) {
                resolveWidth();
                resolveHeight();
            } else {
                resolveHeight();
                resolveWidth();
            }
        }

        // Level size with factor.
        height = factor * (int) Math.floor((double) height / factor);
        width = factor * (int) Math.floor((double) width / factor);
    }

    /// <summary>
    /// Adjust the height of the image so that it is not larger than the
    /// maximum allowed height.
    /// </summary>
    private void resolveHeight() {
        if (height > maxHeight) {
            double ratio = (double) maxHeight / (double) height;
            height = maxHeight;
            width = (int) ((double) height * ratio);
        }
    }

    /// <summary>
    /// Adjust the width of the image so that it is not larger than the
    /// maximum allowed width.
    /// </summary>
    private void resolveWidth() {
        if (width > maxWidth) {
            double ratio = (double) maxWidth / (double) width;
            width = maxWidth;
            height = (int) ((double) height * ratio);
        }
    }
}
