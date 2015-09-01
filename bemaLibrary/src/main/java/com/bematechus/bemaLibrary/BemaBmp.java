package com.bematechus.bemaLibrary;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Color;


class BemaBmp {
    int[] pixels;
    int height;
    int width;
    BemaPrinter.DitherMethod dither;
    byte[] imageData;
    static final int maxWidth = 512;
    static int WHITE = 0x00;//0xFFFFFFFF;
    static int BLACK = 0x01;//0x00000000;




    BemaBmp(Bitmap picture, BemaPrinter.DitherMethod dither ) {
        try {
            if (picture.getWidth() > maxWidth) {
                ScaleImage(picture, maxWidth);
            } else {
                height = picture.getHeight();
                width = picture.getWidth();
                pixels = new int[height * width];

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        pixels[PixelIndex(x, y)] = picture.getPixel(x, y);
                        int breaks  = 0;
                        if ( breaks>=0)
                            breaks++;


                    }
                }

            }

            this.dither = dither;
            imageData = null;
        } catch (OutOfMemoryError e) {
            throw e;
        }

    }

    private int pixelBrightness(int red, int green, int blue) {
        int level = (red + green + blue) / 3;
        return level;
    }

    private int PixelIndex(int x, int y) {
        return (y * width) + x;
    }

    public void ScaleImage(Bitmap picture, int newWidth) {
        int w1 = picture.getWidth();
        int h1 = picture.getHeight();
        int newHeight = newWidth * h1;
        newHeight = newHeight / w1;
        Bitmap bm = Bitmap.createScaledBitmap( picture, newWidth, newHeight, false);
        height = bm.getHeight();
        width = bm.getWidth();
        pixels = new int[height * width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[PixelIndex(x, y)] = bm.getPixel(x, y);
            }
        }
    }

    private int GetGreyLevel(int pixel, float intensity) {
		/*
		 * if(Color.alpha(pixel) == 0) { return 255; }
		 */

        float red = Color.red(pixel);
        float green = Color.green(pixel);
        float blue = Color.blue(pixel);
        float parcial = red + green + blue;
        parcial = (float) (parcial / 3.0);
        int gray = (int) (parcial * intensity);
        if (gray > 255) {
            gray = 255;
        }
        return gray;
    }

    int HowBlack(int x, int y )  {

        int pixel = pixels[PixelIndex(x, y)];

        int sum = (Color.red(pixel) << 1  )  ;
        sum += ( Color.green (pixel) << 2);
        sum += (Color.blue(pixel)  );


        return  (sum / 7);

    }

    private void convertFloydDithering ()
    {
        int pixel;
        int nc;	//nearest color
        int [] ed = new int[width];

											/* Errors distributed down, i.e., */
											/* to the next line.              */
        int x, y, h, v;                 /* Working variables              */
        int [] e = new int[5];                           /* Error parts (7/8,1/8,5/8,3/8). */
        int ef;                            /* Error distributed forward.     */



        for (y=0; y < height; y++ ) {          /* input image.                   */

            ef = 0;      /* No forward error for first dot */

            for (x=0; x<width; ++x) {
                pixel = HowBlack(x,y);
                v = pixel + ef + ed[x];  /* Add errors from    */
                if (v < 0) v = 0;                   /* previous pixels    */
                if (v > 255) v = 255;               /* and clip.          */

                pixel = v;

                if ( pixel < 128 ) {
                    pixels[PixelIndex(x, y)] = BLACK;
                    nc = 0;
                }
                else {
                    pixels[PixelIndex(x, y)] = WHITE;
                    nc = 255;
                }

                v = pixel - nc;		   /* V = new error; h = */
                h = v >> 1;                         /* half of v, e[1..4] */
                e[1] = (7 * h) >> 3;                /* will be filled     */
                e[2] = h - e[1];                    /* with the Floyd and */
                h = v - h;                          /* Steinberg weights. */
                e[3] = (5 * h) >> 3;
                e[4] = h - e[3];

                ef = e[1];                       /* Distribute errors. */
                if (x < width-1) ed[x+1] = e[2];
                if (x == 0) ed[x] = e[3]; else ed[x] += e[3];
                if (x > 0) ed[x-1] += e[4];
            }
        } /* next x */


    }
    private void convertBayerDithering ()
    {

        int pattern[][] = {
            {   0,192, 48,240, 12,204, 60,252,  3,195, 51,243, 15,207, 63,255 },
            { 128, 64,176,112,140, 76,188,124,131, 67,179,115,143, 79,191,127 },
            {  32,224, 16,208, 44,236, 28,220, 35,227, 19,211, 47,239, 31,223 },
            { 160, 96,144, 80,172,108,156, 92,163, 99,147, 83,175,111,159, 95 },
            {   8,200, 56,248,  4,196, 52,244, 11,203, 59,251,  7,199, 55,247 },
            { 136, 72,184,120,132, 68,180,116,139, 75,187,123,135, 71,183,119 },
            {  40,232, 24,216, 36,228, 20,212, 43,235, 27,219, 39,231, 23,215 },
            { 168,104,152, 88,164,100,148, 84,171,107,155, 91,167,103,151, 87 },
            {   2,194, 50,242, 14,206, 62,254,  1,193, 49,241, 13,205, 61,253 },
            { 130, 66,178,114,142, 78,190,126,129, 65,177,113,141, 77,189,125 },
            {  34,226, 18,210, 46,238, 30,222, 33,225, 17,209, 45,237, 29,221 },
            { 162, 98,146, 82,174,110,158, 94,161, 97,145, 81,173,109,157, 93 },
            {  10,202, 58,250,  6,198, 54,246,  9,201, 57,249,  5,197, 53,245 },
            { 138, 74,186,122,134, 70,182,118,137, 73,185,121,133, 69,181,117 },
            {  42,234, 26,218, 38,230, 22,214, 41,233, 25,217, 37,229, 21,213 },
            { 170,106,154, 90,166,102,150, 86,169,105,153, 89,165,101,149, 85 }
        };

        int i, j;
        int pixel;
        for ( i = 0; i < height; i++ ) {
            for ( j=0; j < width; j++) {
                pixel = HowBlack(j,i) ;
                if ( pixel >= pattern[j & 15][i & 15] )

                    pixels[PixelIndex(j, i)] = WHITE;
                else
                    pixels[PixelIndex(j, i)] = BLACK;
            }

        }
    }


    private void convertToMonochromeSteinbertDithering(float intensity) {
        int[][] levelmap = new int[width][height];
        for (int y = 0; y < height; y++) {
            if ((y & 1) == 0) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[PixelIndex(x, y)];
                    levelmap[x][y] += 255 - GetGreyLevel(pixel, intensity);
                    if (levelmap[x][y] >= 255) {
                        levelmap[x][y] -= 255;
                        pixels[PixelIndex(x, y)] = BLACK;
                    } else {
                        pixels[PixelIndex(x, y)] = WHITE;
                    }

                    int sixteenthOfQuantError = levelmap[x][y] / 16;

                    if (x < width - 1)
                        levelmap[x + 1][y] += sixteenthOfQuantError * 7;

                    if (y < height - 1) {
                        levelmap[x][y + 1] += sixteenthOfQuantError * 5;

                        if (x > 0)
                            levelmap[x - 1][y + 1] += sixteenthOfQuantError * 3;
                        if (x < width - 1)
                            levelmap[x + 1][y + 1] += sixteenthOfQuantError;
                    }
                }
            } else {
                for (int x = width - 1; x >= 0; x--) {
                    int pixel = pixels[PixelIndex(x, y)];

                    levelmap[x][y] += 255 - GetGreyLevel(pixel, intensity);

                    if (levelmap[x][y] >= 255) {
                        levelmap[x][y] -= 255;
                        pixels[PixelIndex(x, y)] = BLACK;
                    } else {
                        pixels[PixelIndex(x, y)] = WHITE;
                    }

                    int sixteenthOfQuantError = levelmap[x][y] / 16;

                    if (x > 0)
                        levelmap[x - 1][y] += sixteenthOfQuantError * 7;

                    if (y < height - 1) {
                        levelmap[x][y + 1] += sixteenthOfQuantError * 5;

                        if (x < width - 1)
                            levelmap[x + 1][y + 1] += sixteenthOfQuantError * 3;

                        if (x > 0)
                            levelmap[x - 1][y + 1] += sixteenthOfQuantError;
                    }
                }
            }
        }
    }

    public byte[] getRasterImage () {
        if (imageData != null) {
            return imageData;
        }

        // Converts the image to a Monochrome image using a Steinbert Dithering algorithm. This call can be removed but it that will also remove any dithering.

        switch (dither){
            case Steinbert:
            default:
                convertToMonochromeSteinbertDithering((float) 1.5);
                break;
            case Bayer:
                convertBayerDithering();
                break;
            case Floyd:
                convertFloydDithering();
                break;
        }


        final int yBlockCount = (height + 23) / 24;
        final int BlockSize = 24;
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        int commandHeaderSize = 5; // 1b 2a 21 n1 n2 .....
        byte []verticalMove = new byte[] { 0x1b, 0x4a, 0x00};
        //byte []verticalMove = new byte[] { };
        int lineSize = 0;

        for (int y = 0; y < yBlockCount; y++) {
            byte[] constructedBytes = new byte[commandHeaderSize + width * 3+verticalMove.length];
            lineSize = 0;
            for (int x = 0; x < width; x++) {

                for (int i = 0; i < 3; i++) {
                    int pixely = y * BlockSize + i * 8;
                    try {

                        if (PixelIndex(x,pixely) < pixels.length) {
                            constructedBytes[commandHeaderSize + lineSize] = 0;

                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+0)] << 7);
                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+1)] << 6);
                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+2)] << 5);
                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+3)] << 4);
                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+4)] << 3);
                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+5)] << 2);
                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+6)] << 1);
                            constructedBytes[commandHeaderSize + lineSize] |=(byte) (pixels[PixelIndex(x,pixely+7)] << 0);
                        }
                        else
                            constructedBytes[commandHeaderSize + lineSize] = (byte) 0x00;


                    }
                    catch (Exception ex )
                    {
                        //Log.d("BMP", ex.getMessage());
                        //constructedBytes[commandHeaderSize + lineSize++] = (byte) 0x00;
                    }
                    lineSize++;

                }
            }
            constructedBytes[0] = 0x1b;
            constructedBytes[1] = 0x2a;
            constructedBytes[2] = 0x21;
            constructedBytes[3] = (byte) (width % 256);
            constructedBytes[4] = (byte) (width / 256);

           System.arraycopy(verticalMove,0, constructedBytes,commandHeaderSize+lineSize,verticalMove.length);

            list.add( constructedBytes);
        }


        int packageSize =lineSize +commandHeaderSize+verticalMove.length;
        byte [] flushCurrentBuffer = new byte [] { 0x1b, 0x4a, 0x00};
        imageData = new byte[list.size() * packageSize+ flushCurrentBuffer.length];

        System.arraycopy(flushCurrentBuffer,0, imageData,0,flushCurrentBuffer.length);
        int offset = flushCurrentBuffer.length;
        for ( byte[] line : list)
        {
            System.arraycopy(line, 0, imageData,offset, packageSize);
            offset += packageSize;
        }


        return imageData;

    }

}