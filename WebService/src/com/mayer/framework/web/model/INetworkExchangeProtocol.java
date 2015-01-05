package com.mayer.framework.web.model;

import java.io.IOException;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public interface INetworkExchangeProtocol<TypeRequest extends IRequest, TypeResult> {

    void post(TypeRequest request, TypeResult result) throws IOException;

    IResponse getResponse();


    void load();
}
