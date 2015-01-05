package com.kaching123.tcr.util;


/*
 * Copyright (C) 2011, 2012 Random Android Code Snippets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class IOUtil {

    private IOUtil() { }

    public static void closeQuietly(final Closeable closeable) {
        if (null != closeable) {
            try { closeable.close(); } catch (Exception ignore) { }
        }
    }

    public static void closeQuietly(final Closeable...closeables) {
        for (final Closeable closeable : closeables) {
            closeQuietly(closeable);
        }
    }

    public static long copy(final InputStream source, final OutputStream dest) throws IOException {
        return copy(source, dest, 8192);
    }

    public static long copy(final InputStream source, final OutputStream dest, final int bufferSize) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        long nbytes = 0L;
        int nread;
        while ((-1) != (nread = source.read(buffer))) {
            dest.write(buffer, 0, nread);
            nbytes += nread;
        }
        return nbytes;
    }

    public static String readToString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }
}
// EOF
