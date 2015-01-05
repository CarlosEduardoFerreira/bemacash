package com.mayer.framework.web.model.rest;

import com.mayer.framework.web.Logger.Logger;
import com.mayer.framework.web.model.IResponse;
import com.mayer.framework.web.model.rest.RESTResultHandler.ContentType;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;

public abstract class RESTResult<Type> implements IResponse {

    public static final int ERROR_OUT_OF_MEMORY = 0;
    public static final int ERROR_IO_EXCEPTION = 1;
    public static final int ERROR_NO_HANDLER = 2;

    private static SAXParserFactory saxParserFactory;
    private int[] validResultCodes;

    public int getResultCode() {
        return resultCode;
    }

    private int resultCode;

    public String getErrorReason() {
        return errorReason;
    }

    private String errorReason;

    private Type data;

    public Type getData() {
        return data;
    }

    public void setData(Type data) {
        this.data = data;
    }

    /**
     * Create a web service result that only allows HttpStatus.SC_OK as a valid result
     */
    public RESTResult() {
        this(HttpStatus.SC_OK);
    }

    /**
     * Create a web service result that allows any of the provided result codes as a valid result
     *
     * @param validResultCodes
     */
    protected RESTResult(int... validResultCodes) {
        setValidResultCodes(validResultCodes);
    }

    /**
     * Sets the valid result codes (those that don't trigger use of an ErrorResultHandler)
     *
     * @param validResultCodes
     */
    protected void setValidResultCodes(int... validResultCodes) {
        this.validResultCodes = validResultCodes;
    }

    /**
     * Returns whether this is a valid result object.
     * If it returns false, then it isn't!
     * <p/>
     * This uses the list of valid result codes given in the constructor.
     * Override to implement for non-standard results.
     *
     * @return
     */
    public boolean isValid() {
        for (int code : validResultCodes) {
            if (resultCode == code) {
                return true;
            }
        }
        return false;
    }

    public void setErrorReason(String reason) {
        this.errorReason = reason;
    }

    public boolean updateWith(InputStream str, ContentType type) {
        try {

            RESTResultHandler handler = getDefaultHandler();
            if (handler == null) {
                setResultCode(ERROR_NO_HANDLER);
                return false;
            }

            // update yourself with the response object.
            if (!handler.parse(str, type)) {
                return false;
            }

            return true;

        } catch (IOException ex) {
            // this happens when a network error occurs.
            Logger.e("IOException when parsing local file", ex);
            this.setResultCode(ERROR_IO_EXCEPTION);
            this.setErrorReason(ex.getMessage());
            return false;
        }
    }

    /**
     * updates the object with the http response.
     *
     * @param httpResponse
     */
    public boolean updateWith(HttpResponse httpResponse, RESTResultHandler.ContentType type) {
        try {
            RESTResultHandler handler = getDefaultHandler();
            if (handler == null) {
                setResultCode(ERROR_NO_HANDLER);
                return false;
            }

            // update yourself with the response object.
            if (!handler.parse(httpResponse, type)) {
                return false;
            }

            return true;

        } catch (IOException ex) {
            // this happens when a network error occurs.
            Logger.e("IOException when parsing http response", ex);
            this.setResultCode(ERROR_IO_EXCEPTION);
            this.setErrorReason(ex.getMessage());
            return false;
        } catch (OutOfMemoryError ex) {
            // this happens when the device runs out of memory (usually on catalogue item update parsing).
            Logger.e("Out of memory while trying to create result.", ex);
            this.setResultCode(ERROR_OUT_OF_MEMORY);
            this.setErrorReason(ex.getMessage());
            return false;
        }
    }


    public boolean setResultCode(int resultCode) {
        this.resultCode = resultCode;

        return isValid();
    }

    /**
     * Override this with the handler you want to use to populate this
     * object.
     *
     * @return
     */
    protected abstract RESTResultHandler getDefaultHandler();
}
