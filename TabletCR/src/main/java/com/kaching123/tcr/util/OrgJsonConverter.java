package com.kaching123.tcr.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/*
     * Copyright (C) 2013 Random Android Code Snippets
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
public class OrgJsonConverter implements Converter {

    private final static String UTF8 = "UTF-8";
    private final static String MIME_TYPE = "application/json; charset=UTF-8";

    @Override
    public Object fromBody(TypedInput body, Type type)
            throws ConversionException {
        final String charset;
        final String mimeType = body.mimeType();
        if (null != mimeType) {
            charset = MimeUtil.parseCharset(mimeType);
        } else {
            charset = UTF8;
        }
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(body.in(), charset);
            final StringBuilder s = new StringBuilder(-1 != body.length() ? (int) body.length() : 1024);
            final char[] buf = new char[1024];
            int len;
            while ((-1) != (len = reader.read(buf))) {
                s.append(buf, 0, len);
            }
            return new JSONTokener(s.toString()).nextValue();
        } catch (IOException e) {
            throw new ConversionException(e);
        } catch (JSONException e) {
            throw new ConversionException(e);
        } finally {
            IOUtil.closeQuietly(reader);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        final TypedOutput out;
        try {
            if (object instanceof JSONObject
                    || object instanceof JSONArray) {
                out = new TypedByteArray(MIME_TYPE, object.toString().getBytes(UTF8));
            } else if (null == object) {
                out = new TypedByteArray(MIME_TYPE, "null".getBytes(UTF8));
            } else {
                throw new IllegalArgumentException(
                        "Expected JSONObject or JSONArray but had "
                                + object.getClass().getName()
                );
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Should not happen, no " + UTF8, e);
        }
        return out;
    }

}