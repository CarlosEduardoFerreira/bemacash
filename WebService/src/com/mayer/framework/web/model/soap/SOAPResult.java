package com.mayer.framework.web.model.soap;

import com.mayer.framework.web.model.IResponse;

import org.ksoap2.serialization.SoapObject;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public abstract class SOAPResult implements IResponse {

    public abstract SOAPResult updateWith(SoapObject root);
}
