package com.kaching123.tcr.websvc.api.prepaid;

//------------------------------------------------------------------------------
// <wsdl2code-generated>
//    This code was generated by http://www.wsdl2code.com version  2.5
//
// Date Of Creation: 3/4/2014 8:12:58 PM
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

public class AccessPhone implements KvmSerializable, Serializable {
    
    public String state;
    public String city;
    public String phone;
    public String accessLanguage;
    public String accessType;
    public String areaCode;
    
    public AccessPhone(){}
    
    public AccessPhone(SoapObject soapObject)
    {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("State"))
        {
            Object obj = soapObject.getProperty("State");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                state = j.toString();
            }else if (obj!= null && obj instanceof String){
                state = (String) obj;
            }
        }
        if (soapObject.hasProperty("City"))
        {
            Object obj = soapObject.getProperty("City");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                city = j.toString();
            }else if (obj!= null && obj instanceof String){
                city = (String) obj;
            }
        }
        if (soapObject.hasProperty("Phone"))
        {
            Object obj = soapObject.getProperty("Phone");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                phone = j.toString();
            }else if (obj!= null && obj instanceof String){
                phone = (String) obj;
            }
        }
        if (soapObject.hasProperty("AccessLanguage"))
        {
            Object obj = soapObject.getProperty("AccessLanguage");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                accessLanguage = j.toString();
            }else if (obj!= null && obj instanceof String){
                accessLanguage = (String) obj;
            }
        }
        if (soapObject.hasProperty("AccessType"))
        {
            Object obj = soapObject.getProperty("AccessType");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                accessType = j.toString();
            }else if (obj!= null && obj instanceof String){
                accessType = (String) obj;
            }
        }
        if (soapObject.hasProperty("AreaCode"))
        {
            Object obj = soapObject.getProperty("AreaCode");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                areaCode = j.toString();
            }else if (obj!= null && obj instanceof String){
                areaCode = (String) obj;
            }
        }
    }
    @Override
    public Object getProperty(int arg0) {
        switch(arg0){
            case 0:
                return state;
            case 1:
                return city;
            case 2:
                return phone;
            case 3:
                return accessLanguage;
            case 4:
                return accessType;
            case 5:
                return areaCode;
        }
        return null;
    }
    
    @Override
    public int getPropertyCount() {
        return 6;
    }
    
    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch(index){
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
                info.name = "Phone";
                break;
            case 3:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "AccessLanguage";
                break;
            case 4:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "AccessType";
                break;
            case 5:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "AreaCode";
                break;
        }
    }
    
    @Override
    public void setProperty(int arg0, Object arg1) {
    }
    
}
