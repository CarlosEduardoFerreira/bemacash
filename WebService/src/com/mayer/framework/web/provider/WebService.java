package com.mayer.framework.web.provider;

import com.mayer.framework.web.model.INetworkExchangeProtocol;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public enum  WebService {

    REST(new com.mayer.framework.web.provider.RESTWebService()),
    SOAP(new SOAPWebService());

    private INetworkExchangeProtocol protocol;

    private WebService(INetworkExchangeProtocol protocol) {
        this.protocol = protocol;
    }

    public INetworkExchangeProtocol get() {
        return this.protocol;
    }
}