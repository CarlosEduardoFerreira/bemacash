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

public class Product implements KvmSerializable, Serializable {

    public String code;
    public String name;
    public boolean useFixedDenominations;
    public double minDenomination;
    public double maxDenomination;
    public String carrierName;
    public String countryCode;
    public String countryName;
    public String type;
    public String imageUrl;
    public String dialCountryCode;
    public String termsAndConditions;
    public VectorDouble denominations;
    public VectorProductAccessPhone productAccessPhones;
    public int merchantBuyingFrequency;
    public int zipCodeBuyingFrequency;

    public Product(){}

    public Product(SoapObject soapObject)
    {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("Code"))
        {
            Object obj = soapObject.getProperty("Code");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                code = j.toString();
            }else if (obj!= null && obj instanceof String){
                code = (String) obj;
            }
        }
        if (soapObject.hasProperty("Name"))
        {
            Object obj = soapObject.getProperty("Name");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                name = j.toString();
            }else if (obj!= null && obj instanceof String){
                name = (String) obj;
            }
        }
        if (soapObject.hasProperty("UseFixedDenominations"))
        {
            Object obj = soapObject.getProperty("UseFixedDenominations");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                useFixedDenominations = Boolean.parseBoolean(j.toString());
            }else if (obj!= null && obj instanceof Boolean){
                useFixedDenominations = (Boolean) obj;
            }
        }
        if (soapObject.hasProperty("MinDenomination"))
        {
            Object obj = soapObject.getProperty("MinDenomination");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                minDenomination = Double.parseDouble(j.toString());
            }else if (obj!= null && obj instanceof Number){
                minDenomination = (Double) obj;
            }
        }
        if (soapObject.hasProperty("MaxDenomination"))
        {
            Object obj = soapObject.getProperty("MaxDenomination");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                maxDenomination = Double.parseDouble(j.toString());
            }else if (obj!= null && obj instanceof Number){
                maxDenomination = (Double) obj;
            }
        }
        if (soapObject.hasProperty("CarrierName"))
        {
            Object obj = soapObject.getProperty("CarrierName");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                carrierName = j.toString();
            }else if (obj!= null && obj instanceof String){
                carrierName = (String) obj;
            }
        }
        if (soapObject.hasProperty("CountryCode"))
        {
            Object obj = soapObject.getProperty("CountryCode");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                countryCode = j.toString();
            }else if (obj!= null && obj instanceof String){
                countryCode = (String) obj;
            }
        }
        if (soapObject.hasProperty("CountryName"))
        {
            Object obj = soapObject.getProperty("CountryName");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                countryName = j.toString();
            }else if (obj!= null && obj instanceof String){
                countryName = (String) obj;
            }
        }
        if (soapObject.hasProperty("Type"))
        {
            Object obj = soapObject.getProperty("Type");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                type = j.toString();
            }else if (obj!= null && obj instanceof String){
                type = (String) obj;
            }
        }
        if (soapObject.hasProperty("ImageUrl"))
        {
            Object obj = soapObject.getProperty("ImageUrl");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                imageUrl = j.toString();
            }else if (obj!= null && obj instanceof String){
                imageUrl = (String) obj;
            }
        }
        if (soapObject.hasProperty("DialCountryCode"))
        {
            Object obj = soapObject.getProperty("DialCountryCode");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                dialCountryCode = j.toString();
            }else if (obj!= null && obj instanceof String){
                dialCountryCode = (String) obj;
            }
        }
        if (soapObject.hasProperty("TermsAndConditions"))
        {
            Object obj = soapObject.getProperty("TermsAndConditions");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                termsAndConditions = j.toString();
            }else if (obj!= null && obj instanceof String){
                termsAndConditions = (String) obj;
            }
        }
        if (soapObject.hasProperty("Denominations"))
        {
            SoapObject j = (SoapObject)soapObject.getProperty("Denominations");
            denominations = new VectorDouble(j);
        }
        if (soapObject.hasProperty("AccessPhones"))
        {
            SoapObject j = (SoapObject)soapObject.getProperty("AccessPhones");
            productAccessPhones = new VectorProductAccessPhone(j);
        }
        if (soapObject.hasProperty("MerchantBuyingFrequency"))
        {
            Object obj = soapObject.getProperty("MerchantBuyingFrequency");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                merchantBuyingFrequency = Integer.parseInt(j.toString());
            }else if (obj!= null && obj instanceof Number){
                merchantBuyingFrequency = (Integer) obj;
            }
        }
        if (soapObject.hasProperty("ZipCodeBuyingFrequency"))
        {
            Object obj = soapObject.getProperty("ZipCodeBuyingFrequency");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) obj;
                zipCodeBuyingFrequency = Integer.parseInt(j.toString());
            }else if (obj!= null && obj instanceof Number){
                zipCodeBuyingFrequency = (Integer) obj;
            }
        }
    }
    @Override
    public Object getProperty(int arg0) {
        switch(arg0){
            case 0:
                return code;
            case 1:
                return name;
            case 2:
                return useFixedDenominations;
            case 3:
                return minDenomination;
            case 4:
                return maxDenomination;
            case 5:
                return carrierName;
            case 6:
                return countryCode;
            case 7:
                return countryName;
            case 8:
                return type;
            case 9:
                return imageUrl;
            case 10:
                return dialCountryCode;
            case 11:
                return termsAndConditions;
            case 12:
                return denominations;
            case 13:
                return productAccessPhones;
            case 14:
                return merchantBuyingFrequency;
            case 15:
                return zipCodeBuyingFrequency;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 16;
    }

    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch(index){
            case 0:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Code";
                break;
            case 1:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Name";
                break;
            case 2:
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "UseFixedDenominations";
                break;
            case 3:
                info.type = Double.class;
                info.name = "MinDenomination";
                break;
            case 4:
                info.type = Double.class;
                info.name = "MaxDenomination";
                break;
            case 5:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "CarrierName";
                break;
            case 6:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "CountryCode";
                break;
            case 7:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "CountryName";
                break;
            case 8:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Type";
                break;
            case 9:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "ImageUrl";
                break;
            case 10:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "DialCountryCode";
                break;
            case 11:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "TermsAndConditions";
                break;
            case 12:
                info.type = PropertyInfo.VECTOR_CLASS;
                info.name = "Denominations";
                break;
            case 13:
                info.type = PropertyInfo.VECTOR_CLASS;
                info.name = "AccessPhones";
                break;
            case 14:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "MerchantBuyingFrequency";
                break;
            case 15:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "ZipCodeBuyingFrequency";
                break;
        }
    }

    @Override
    public void setProperty(int arg0, Object arg1) {
    }

}