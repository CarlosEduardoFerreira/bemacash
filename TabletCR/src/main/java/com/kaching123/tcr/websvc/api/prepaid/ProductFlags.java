package com.kaching123.tcr.websvc.api.prepaid;

//------------------------------------------------------------------------------
// <wsdl2code-generated>
//    This code was generated by http://www.wsdl2code.com version  2.5
//
// Date Of Creation: 3/3/2015 9:14:18 PM
//    Please dont change this code, regeneration will override your changes
//</wsdl2code-generated>
//
//------------------------------------------------------------------------------
//
//This source code was auto-generated by Wsdl2Code  Version
//
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import java.util.Hashtable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

public class ProductFlags implements KvmSerializable {
    
    public boolean showRates;
    public boolean showTermsAndConditions;
    public boolean showInstructions;
    public boolean showAccessPhones;
    
    public ProductFlags(){}
    
    public ProductFlags(SoapObject soapObject)
    {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("ShowRates"))
        {
            Object obj = soapObject.getProperty("ShowRates");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                showRates = Boolean.parseBoolean(j.toString());
            }else if (obj!= null && obj instanceof Boolean){
                showRates = (Boolean) obj;
            }
        }
        if (soapObject.hasProperty("ShowTermsAndConditions"))
        {
            Object obj = soapObject.getProperty("ShowTermsAndConditions");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                showTermsAndConditions = Boolean.parseBoolean(j.toString());
            }else if (obj!= null && obj instanceof Boolean){
                showTermsAndConditions = (Boolean) obj;
            }
        }
        if (soapObject.hasProperty("ShowInstructions"))
        {
            Object obj = soapObject.getProperty("ShowInstructions");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                showInstructions = Boolean.parseBoolean(j.toString());
            }else if (obj!= null && obj instanceof Boolean){
                showInstructions = (Boolean) obj;
            }
        }
        if (soapObject.hasProperty("ShowAccessPhones"))
        {
            Object obj = soapObject.getProperty("ShowAccessPhones");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                showAccessPhones = Boolean.parseBoolean(j.toString());
            }else if (obj!= null && obj instanceof Boolean){
                showAccessPhones = (Boolean) obj;
            }
        }
    }
    @Override
    public Object getProperty(int arg0) {
        switch(arg0){
            case 0:
                return showRates;
            case 1:
                return showTermsAndConditions;
            case 2:
                return showInstructions;
            case 3:
                return showAccessPhones;
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
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "ShowRates";
                break;
            case 1:
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "ShowTermsAndConditions";
                break;
            case 2:
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "ShowInstructions";
                break;
            case 3:
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "ShowAccessPhones";
                break;
        }
    }
    
    @Override
    public void setProperty(int arg0, Object arg1) {
    }
    
}