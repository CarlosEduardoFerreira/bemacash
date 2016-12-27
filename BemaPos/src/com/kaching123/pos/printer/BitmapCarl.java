package com.kaching123.pos.printer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import java.util.ArrayList;

/**
 * Class Developed by Carlos Eduardo Ferreira - Bematech US Team
 * This class converts a {@link Bitmap}
 * image for printing on the
 * LR2000 Bematech Printer
 */
public final class BitmapCarl {

    private static final String TAG = "BemaCarl";

    private static final String className = "[BitmapCarl] ";

    private static final int DOTS_PER_LINE = 512;
    // Default line is 24 for graphic and fonts
    private static final int GRAPHIC_LINE_HEIGHT = 24;

    private static final int MAXIMUM_BITMAP_HEIGHT = ((DOTS_PER_LINE + GRAPHIC_LINE_HEIGHT - 1) / GRAPHIC_LINE_HEIGHT) * GRAPHIC_LINE_HEIGHT;

    private enum Binary {
        WHITE,
        BLACK
    }

    public static Bitmap resizeBitmap(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap resized = null;

        if (width > DOTS_PER_LINE && height > MAXIMUM_BITMAP_HEIGHT) {
            if (width > height) {
                float rate = (float) width / (float) DOTS_PER_LINE;
                int newHeight = (int) ((float) height / rate);
                resized = getResizedBitmap(bitmap, DOTS_PER_LINE, newHeight);
            } else {
                float rate = (float) height / (float) MAXIMUM_BITMAP_HEIGHT;
                int newWidth = (int) ((float) width / rate);
                resized = getResizedBitmap(bitmap, newWidth, MAXIMUM_BITMAP_HEIGHT);
            }
        } else if (width > DOTS_PER_LINE && height <= MAXIMUM_BITMAP_HEIGHT) {
            float rate = (float) width / (float) DOTS_PER_LINE;
            int newHeight = (int) ((float) height / rate);
            resized = getResizedBitmap(bitmap, DOTS_PER_LINE, newHeight);
        } else if (height > MAXIMUM_BITMAP_HEIGHT && width <= DOTS_PER_LINE) {
            float rate = (float) width / (float) DOTS_PER_LINE;
            int newHeight = (int) ((float) height / rate);
            resized = getResizedBitmap(bitmap, DOTS_PER_LINE, newHeight);
        }

        return resized != null ? resized : bitmap;
    }

     /**
     * Resize the bitmap into the specified height and the specified width
     * @param image
     *          the image to resize
     * @param newHeight
     *          the new image height
     * @param newWidth
     *          the new image width
     * @return
     *          the image resized
     * @exception IllegalArgumentException
     *                  if some parameter is invalid
     */

    public static Bitmap getResizedBitmap(Bitmap image, int newWidth, int newHeight) {
        if (image == null){
            final String methodName = "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "] ";
            IllegalArgumentException e = new IllegalArgumentException("image parameter is null");
            Log.e(TAG, className + methodName, e);
            throw e;
        }
        if (newHeight <= 0){
            final String methodName = "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "] ";
            IllegalArgumentException e = new IllegalArgumentException("newHeight parameter is less than 1");
            Log.e(TAG, className + methodName, e);
            throw e;
        }
        if (newWidth <= 0){
            final String methodName = "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "] ";
            IllegalArgumentException e = new IllegalArgumentException("newWidth parameter is less than 1");
            Log.e(TAG, className + methodName, e);
            throw e;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    /**
     * Converts the bitmap to printing commands. The image will be resized if the width is larger
     * than 608 pixels and the height is larger than 624.
     * @param image
     *          the specified bitmap to decode
     * @return
     *      a {@link BitmapPrintedCarl} object containing the bitmap decoded to printing
     * @exception IllegalArgumentException
     *              thrown if the image parameter is null or empty
     */
    public static BitmapPrintedCarl toPrint(Bitmap image) throws IllegalArgumentException {

        if (image == null){
            final String methodName = "[" + Thread.currentThread().getStackTrace()[2].getMethodName() + "] ";
            IllegalArgumentException e = new IllegalArgumentException("image parameter is null");
            Log.e(TAG, className + methodName, e);
            throw e;
        }

        // resize bitmap if necessary
        Bitmap bitmap = resizeBitmap(image);

        //Bitmap bitmap = image;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        ArrayList<Binary> data = new ArrayList<>(width * height);
        for (int i = 0; i < width * height; i++)
            data.add(null);

        // convert to grayscale
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);

                float a = Color.alpha(pixel) / (float) 0xff;
                float r = Color.red(pixel) / (float) 0xff;
                float g = Color.green(pixel) / (float) 0xff;
                float b = Color.blue(pixel) / (float) 0xff;

                float average = (r + g + b) / 3;
                Binary pixelBinary = average > 0.7f ? Binary.WHITE : Binary.BLACK;

                data.set(y * width + x, pixelBinary);
            }
        }

        // ESC * ! xL xH ... 0A
        // ESC * ! xL xH ... ESC J 0

        int ny = (height + GRAPHIC_LINE_HEIGHT - 1) / GRAPHIC_LINE_HEIGHT;
        int total = ny * (8 + width * 3);

        byte[] ret = new byte[total];
        int position = 0;

        ret[total - 1] = 0x0a;

        for (int i = 0; i < ny; i++) {
            byte b = 0;
            int count = 0;
            ret[position++] = 0x1b;
            ret[position++] = '*';
            ret[position++] = '!';
            ret[position++] = (byte) width;
            ret[position++] = (byte) (width >> 8);
            int l = i * GRAPHIC_LINE_HEIGHT;

            for (int j = 0; j < width; j++) {
                for (int k = l; k < (l + GRAPHIC_LINE_HEIGHT); k++) {
                    int val;
                    if (k < height) {
                        Binary pixel = data.get(k * width + j);
                        val = pixel == Binary.BLACK ? 1 : 0;
                    } else {
                        val = 0;
                    }

                    b |= val;

                    if (++count == 8) {
                        ret[position++] = b;
                        b = 0;
                        count = 0;
                    } else {
                        b <<= 1;
                    }
                }
            } // for j

            if (count != 0) {
                while (++count < 8) {
                    b <<= 1;
                }
                ret[position++] = b;
            }
            ret[position++] = 0x1b;
            ret[position++] = 'J';
            ret[position++] = 0;
        } // for i

        return new BitmapPrintedCarl(ret);
    }
}
