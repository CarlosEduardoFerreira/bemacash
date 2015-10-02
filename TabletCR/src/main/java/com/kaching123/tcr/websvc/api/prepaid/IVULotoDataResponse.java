package com.kaching123.tcr.websvc.api.prepaid;

//------------------------------------------------------------------------------
// <wsdl2code-generated>
//    This code was generated by http://www.wsdl2code.com version  2.6
//
// Date Of Creation: 6/25/2015 9:58:58 PM
//    Please dont change this code, regeneration will override your changes
//</wsdl2code-generated>
//
//------------------------------------------------------------------------------
//
//This source code was auto-generated by Wsdl2Code  Version
//
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

public class IVULotoDataResponse implements KvmSerializable , Serializable {
    
    public long resultId;
    public String resultDescription;
    public IVULotoResponseData iVULotoData;
    
    public IVULotoDataResponse(){}
    
    public IVULotoDataResponse(SoapObject soapObject)
    {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("ResultId"))
        {
            Object obj = soapObject.getProperty("ResultId");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                resultId = Integer.parseInt(j.toString());
            }else if (obj!= null && obj instanceof Number){
                resultId = (Integer) obj;
            }
        }
        if (soapObject.hasProperty("ResultDescription"))
        {
            Object obj = soapObject.getProperty("ResultDescription");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                resultDescription = j.toString();
            }else if (obj!= null && obj instanceof String){
                resultDescription = (String) obj;
            }
        }
        if (soapObject.hasProperty("IVULotoData"))
        {
            SoapObject j = (SoapObject)soapObject.getProperty("IVULotoData");
            iVULotoData =  new IVULotoResponseData (j);
            
        }
    }
    @Override
    public Object getProperty(int arg0) {
        switch(arg0){
            case 0:
                return resultId;
            case 1:
                return resultDescription;
            case 2:
                return iVULotoData;
        }
        return null;
    }
    
    @Override
    public int getPropertyCount() {
        return 3;
    }
    
    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch(index){
            case 0:
                info.type = Long.class;
                info.name = "ResultId";
                break;
            case 1:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "ResultDescription";
                break;
            case 2:
                info.type = IVULotoResponseData.class;
                info.name = "IVULotoData";
                break;
        }
    }
    
    
    @Override
    public void setProperty(int arg0, Object arg1) {
    }
    
}