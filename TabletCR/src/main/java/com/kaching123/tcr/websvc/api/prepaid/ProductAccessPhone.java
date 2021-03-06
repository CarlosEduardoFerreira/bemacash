package com.kaching123.tcr.websvc.api.prepaid;

//------------------------------------------------------------------------------
// <wsdl2code-generated>
//    This code was generated by http://www.wsdl2code.com version  2.5
//
// Date Of Creation: 11/6/2014 9:25:05 PM
//    Please dont change this code, regeneration will override your changes
//</wsdl2code-generated>
//
//------------------------------------------------------------------------------
//
//This source code was auto-generated by Wsdl2Code  Version
//

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.io.Serializable;
import java.util.Hashtable;

public class ProductAccessPhone implements KvmSerializable, Serializable {

    public String state;
    public String city;
    public String language;
    public String phoneNumber;

    public ProductAccessPhone() {
    }

    public ProductAccessPhone(SoapObject soapObject) {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("State")) {
            Object obj = soapObject.getProperty("State");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j = (SoapPrimitive) obj;
                state = j.toString();
            } else if (obj != null && obj instanceof String) {
                state = (String) obj;
            }
        }
        if (soapObject.hasProperty("City")) {
            Object obj = soapObject.getProperty("City");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j = (SoapPrimitive) obj;
                city = j.toString();
            } else if (obj != null && obj instanceof String) {
                city = (String) obj;
            }
        }
        if (soapObject.hasProperty("Language")) {
            Object obj = soapObject.getProperty("Language");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j = (SoapPrimitive) obj;
                language = j.toString();
            } else if (obj != null && obj instanceof String) {
                language = (String) obj;
            }
        }
        if (soapObject.hasProperty("PhoneNumber")) {
            Object obj = soapObject.getProperty("PhoneNumber");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j = (SoapPrimitive) obj;
                phoneNumber = j.toString();
            } else if (obj != null && obj instanceof String) {
                phoneNumber = (String) obj;
            }
        }
    }

    @Override
    public Object getProperty(int arg0) {
        switch (arg0) {
            case 0:
                return state;
            case 1:
                return city;
            case 2:
                return language;
            case 3:
                return phoneNumber;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 4;
    }

    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch (index) {
            case 0:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "State";
                break;
            case 1:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "City";
                break;
            case 2:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Language";
                break;
            case 3:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "PhoneNumber";
                break;
        }
    }

    @Override
    public void setProperty(int arg0, Object arg1) {
    }

}
