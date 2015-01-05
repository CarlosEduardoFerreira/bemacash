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

public class ProductListResponse implements KvmSerializable, Serializable {
    
    public long resultId;
    public String resultDescription;
    public String productListVersion;
    public VectorProduct products;

    public ProductListResponse(){}
    
    public ProductListResponse(SoapObject soapObject)
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
        if (soapObject.hasProperty("ProductListVersion"))
        {
            Object obj = soapObject.getProperty("ProductListVersion");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                productListVersion = j.toString();
            }else if (obj!= null && obj instanceof String){
                productListVersion = (String) obj;
            }
        }
        if (soapObject.hasProperty("Products"))
        {
            SoapObject j = (SoapObject)soapObject.getProperty("Products");
            products = new VectorProduct(j);
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
                return productListVersion;
            case 3:
                return products;
        }
        return null;
    }
    
    @Override
    public int getPropertyCount() {
        return 4;
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
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "ProductListVersion";
                break;
            case 3:
                info.type = PropertyInfo.VECTOR_CLASS;
                info.name = "Products";
                break;
        }
    }
    
    @Override
    public void setProperty(int arg0, Object arg1) {
    }
    
}
