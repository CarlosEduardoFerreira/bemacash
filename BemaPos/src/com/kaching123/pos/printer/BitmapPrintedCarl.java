/*
 * Copyright 2015 Bematech S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaching123.pos.printer;

/**
 * This class represents a {@link android.graphics.Bitmap} image converted to printer commands.
 * This class can not be instantiated directly. Use {@link BitmapCarl} class to get the
 * {@link BitmapPrintedCarl} object
 */
public final class BitmapPrintedCarl implements BitmapPrintableCarl {
    private byte[] printedBitmap = null;

    BitmapPrintedCarl(byte[] printedBitmap){
        this.printedBitmap = printedBitmap;
    }

    /**
     * Prepare this {@link BitmapPrintedCarl} to be printed
     * @return a new array of bytes ready to be printed

     */
    @Override
    public byte[] toPrint(){
        return printedBitmap;
    }
}
