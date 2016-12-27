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
 * Defines an interface for classes that can be printed
 */
public interface BitmapPrintableCarl {
    /**
     * Prepare the specified object to be printed
     * @return
     *          a new byte array containing the data ready to print
     * @throws Exception
     *              if some exception occurs
     */
    byte[] toPrint() throws Exception;
}
