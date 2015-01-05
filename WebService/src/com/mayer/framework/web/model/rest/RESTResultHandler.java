package com.mayer.framework.web.model.rest;


import com.google.gson.stream.JsonReader;
import com.mayer.framework.web.Logger.Logger;
import com.mayer.framework.web.provider.RESTWebService;
import com.mayer.framework.web.utils.StringUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.params.HttpParams;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to be parent to all handlers
 */
public abstract class RESTResultHandler<T extends RESTResult> extends DefaultHandler {

    public static final boolean D = false;

    protected static final int PUSHSTREAM_BUFFER_SIZE = 4096;


    public enum ContentType {
        JSON, XML
    }

    private static SAXParserFactory saxParserFactory;

    private final T webServiceResult;

    private StringBuilder mBuilder;

    public RESTResultHandler(T webServiceResult) {
        super();
        this.webServiceResult = webServiceResult;
    }

    protected T getWebServiceResult() {
        return webServiceResult;
    }

    protected final void nonParcedItemYield(String tag, String ettr) {
        Logger.d("This attr has not handled, check your handler : %s within %s", ettr, tag);
    }

    /**
     * Save the contents
     */
    @Override
    public final void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        mBuilder.append(ch, start, length);
    }

    /**
     * Starts the document
     */
    @Override
    public final void startDocument() throws SAXException {
        super.startDocument();
        mBuilder = new StringBuilder();
    }

    /**
     * Starts an element
     */
    @Override
    public final void startElement(String uri, String localName, String name,
                                   Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);

        handleElementStart(localName);

        int noOfAttributes = attributes.getLength();

        for (int i = 0; i < noOfAttributes; i++) {
            handleAttribute(localName, attributes.getLocalName(i), attributes.getValue(i));
        }
    }

    @Override
    public final void endElement(String uri, String localName, String name) throws SAXException {
        super.endElement(uri, localName, name);

        handleElementContents(localName, mBuilder);

        mBuilder.setLength(0);

        handleElementEnd(localName);
    }

    /**
     * This gets the response code, and any other meta information about the
     * response.
     *
     * @param response
     */
    protected boolean handleMetaInformation(HttpResponse response) {

        // handle the headers.
        this.handleHeaders(response.getAllHeaders());

        // handle any parameters.
        this.handleParameters(response.getParams());

        // set the status code in the result. (allows the result to validate
        // itself).
        return this.handleResultCode(response.getStatusLine().getStatusCode());
    }

    /**
     * Handle any response parameters returned by the request.
     *
     * @param parameters
     */
    protected void handleParameters(HttpParams parameters) {
    }

    /**
     * Override this to handle the result code. Ensure you call
     * super.handleResultCode() to set the result code of the result object.
     *
     * @param code
     */
    protected boolean handleResultCode(int code) {
        if ( D ) {
            Logger.d("Result code: %s", code);
        }
        return this.webServiceResult.setResultCode(code);
    }

    /**
     * Handle any headers you are interested in, returned by the request.
     *
     * @param headers
     */

    protected void handleHeaders(Header[] headers) {
        if ( D ) {
            for (Header header : headers) {
                Logger.d("Received header %s as %s", header.getName(), header.getValue());
            }
        }
    }

    /**
     * This parses the given http response using the current handler.
     *
     * @param response
     * @result - true if successfully parsed. False if there was an error.
     */
    public boolean parse(HttpResponse response, ContentType type) throws OutOfMemoryError, IOException {
        try {
            if (response == null) {
                // don't try to log this back to the server or you get an
                // infinite loop
                // when offline
                Logger.d("Null response received. Assuming offline");
                return false;
            }

            // set the status code in the result. (allows the result to validate
            // itself).
            // if the result code isn't good, you want to use a different
            // handler for the different xml.
            if (handleMetaInformation(response)) {
                InputStream inputStream = RESTWebService.getEntityStream(response);
                return parse(inputStream, type);
            } else {
                Logger.d("Ignoring response (result code not in valid code list)");
                return false;
            }
        } catch (IOException ex) {
            Logger.e("Parsing of http response failed", ex);
            throw ex;
        } catch (OutOfMemoryError ex) {
            Logger.e("Parsing of http response failed, due to out of memory error.", ex);
            throw ex;
        } catch (Exception ex) {
            Logger.e("Unknown exception while parsing response", ex);
            return false;
        }
    }

    /**
     * This parses the contents of the stream
     *
     * @throws java.io.IOException
     */
    public boolean parse(InputStream is, ContentType type) throws IOException {
        // only handle the response if there is one
        // (sometimes you get a blank response).
        if (is == null) {
            Logger.d("Blank response received. Treating as a valid, empty response.");
            return true;
        } else {
            PushbackInputStream pushbackStream = null;
            try {

                pushbackStream = new PushbackInputStream(is, PUSHSTREAM_BUFFER_SIZE);
                // read the first byte of the stream to check that there is
                // something there,
                // then 'unread' it and pass it on to the parser
                try {
                    final int firstByte = pushbackStream.read();
                    // if the first byte is -1, then the stream is empty
                    if (firstByte == -1) {
                        Logger.d("Empty stream received. Treating as a valid, empty response.");
                        return true;
                    }
                    pushbackStream.unread(firstByte);
                } catch (Exception ex) {
                    Logger.e("Failed to get first byte. stream is probably empty", ex);
                    return true;
                }
                if ( D ) {
                    logISData(pushbackStream);
                }
                preHandle(pushbackStream);

                switch (type) {
                    case XML: {
                        SAXParser parser = getNewParser();
                        parser.parse(pushbackStream, this);
                        break;
                    }
                    case JSON: {
                        JsonReader jsonReader = new JsonReader(new InputStreamReader(pushbackStream, "UTF-8"));
                        handleJSON(jsonReader);
                        break;
                    }
                }

            } catch (ParserConfigurationException ex) {
                return false;
            } catch (SAXException ex) {
                return false;
            } finally {
                if (pushbackStream != null) {
                    try {
                        pushbackStream.close();
                    } catch (Exception ex) {
                        Logger.e("Failed to close pushback stream", ex);
                    }
                }
            }
        }
        return true;
    }

    protected void preHandle(PushbackInputStream pushbackStream) {

    }

    protected final void logISData( PushbackInputStream pushbackStream) {
        Logger.d("Handler is about to read and pushback some data in sake of debugging. This should be removed in live.");
        Logger.d(StringUtils.getString(pushbackStream));
    }


    /**
     * Returns a new parser from the parser factory
     *
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    private static synchronized SAXParser getNewParser() throws SAXException, ParserConfigurationException {
        if (saxParserFactory == null) {
            saxParserFactory = SAXParserFactory.newInstance();
        }
        return saxParserFactory.newSAXParser();
    }

    /********************************************** PARSE CASTS *********************************************/

    /**
     * Parses text as a boolean, allowing '1' or 'true', before falling back on
     * Boolean.parseBoolean
     *
     * @param text
     * @return
     */
    protected static final boolean getBoolean(String text) {
        if (text != null && ("1".equals(text) || "true".equals(text.toLowerCase()))) {
            return true;
        }
        return Boolean.parseBoolean(text);
    }

    /**
     * Parses text as a boolean
     */
    protected static final boolean getBoolean(StringBuilder text) {
        return getBoolean(text.toString());
    }

    /**
     * Parses text as an integer
     */
    protected static final int getInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parses text as an integer
     */
    protected static final int getInt(StringBuilder text) {
        return getInt(text.toString());
    }

    /**
     * Parses text as an integer
     */
    protected static final long getLong(String text) {
        return Long.parseLong(text);
    }

    /**
     * Parses text as an integer
     */
    protected static final long getLong(StringBuilder text) {
        return getLong(text.toString());
    }

    /**
     * Parses text as a float
     */
    protected static final float getFloat(String text) {
        return Float.parseFloat(text);
    }

    /**
     * Parses text as a float
     */
    protected static final float getFloat(StringBuilder text) {
        return getFloat(text.toString());
    }

    /**
     * Return the string from the stringBuilder.
     */
    protected static final String getString(StringBuilder text) {
        if (text != null) {
            return text.toString();
        }
        return null;
    }

    /**
     * Returns the string
     */
    protected static final String getString(String text) {
        return text;
    }

    /**
     * Override to do the element processing.
     */
    protected  void handleElementStart(String nodeName){}

    /**
     * Override to do the element processing.
     */
    protected  void handleElementContents(String nodeName, StringBuilder elementContents){}

    /**
     * Override to do the element processing.
     */
    protected  void handleElementEnd(String nodeName){}

    /**
     * Override this to handle any attributes of the root node.
     */
    protected  void handleAttribute(String nodeName, String attributeName, String attributeValue){}

    /**
     * Override this to handle json
     */
    protected boolean handleJSON(JsonReader json) { return false;}
}
