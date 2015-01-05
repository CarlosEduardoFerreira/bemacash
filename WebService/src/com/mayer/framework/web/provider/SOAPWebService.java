package com.mayer.framework.web.provider;

import com.mayer.framework.web.Logger.Logger;
import com.mayer.framework.web.model.INetworkExchangeProtocol;
import com.mayer.framework.web.model.IResponse;
import com.mayer.framework.web.model.soap.SOAPRequest;
import com.mayer.framework.web.model.soap.SOAPResult;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SOAPWebService implements INetworkExchangeProtocol<SOAPRequest, SOAPResult> {



    protected SOAPWebService() { }

    @Override
    public void post(SOAPRequest request, SOAPResult result) throws IOException {


        SoapObject soapResponse = soapPost(request.getNamespace(),
                 request.getMethodName(),
                 request.getUrl(),
                 request.getSoapAction(),
                request.properties());
        if (soapResponse != null) {
            result.updateWith(soapResponse);
        }
    }

    @Override
    public IResponse getResponse() {
        return null;
    }

    @Override
    public void load() {

    }

    protected SoapObject soapPost(final String namespace, final String methodName, final String url, final String soapAction, final PropertyInfo... properties) {
        SoapObject request = new SoapObject(namespace,  methodName);

        if (properties != null) {
            for (PropertyInfo property : properties) {
                request.addProperty(property);
            }
        }
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
//        for (Mapping mapping : mappings) {
//            envelope.addMapping(namespace, mapping.name, mapping.clazz);
//        }
        envelope.implicitTypes = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
        androidHttpTransport.debug = true;

        try {
            androidHttpTransport.call(soapAction, envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            Logger.d("Webservice Output " + response.toString());

            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
