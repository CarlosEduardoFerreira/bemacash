package com.mayer.framework.web.model.rest;

import com.mayer.framework.web.model.IRequest;
import com.mayer.framework.web.model.WebParameter;

import org.apache.http.Header;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class RESTRequest implements IRequest {

    public String path;
    public String body;
    public Header[] headers;
    public WebParameter[] parameters;
}
