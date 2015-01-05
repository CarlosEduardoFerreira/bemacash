package com.mayer.framework.web.model.soap;

import com.mayer.framework.web.model.IRequest;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

/**
 * @author Ivan v. Rikhmayer
 */
public abstract class SOAPRequest implements IRequest, KvmSerializable {

    final String namespace;
    final String methodName;
    final String url;
    final String soapAction;
    final PropertyInfo[] properties;
    final Mapping[] mappings;

    protected SOAPRequest(String namespace, String methodName, String url, String soapAction, Mapping[] mappings, PropertyInfo... properties) {
        this.namespace = namespace;
        this.methodName = methodName;
        this.mappings = mappings;
        this.url = url;
        this.soapAction = soapAction;
        this.properties = properties;
    }

    public Mapping[] getMappings() {
        return mappings;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUrl() {
        return url;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public PropertyInfo[] getProperties() {
        return properties;
    }

    public static class Mapping
    {
        public String name;
        public Class clazz;
    }

    public abstract PropertyInfo[] properties();
}
