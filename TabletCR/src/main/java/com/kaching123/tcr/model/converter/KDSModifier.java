package com.kaching123.tcr.model.converter;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by long on 7/12/2016.
 */
@Root(name = "Condiment")
public class KDSModifier {
    @Element(name = "ID")
    String id;
    @Element(name = "TransType")
    int transType;
    @Element(name = "Name")
    String name;
    public KDSModifier(String id, int transType, String name){
        this.id = id;
        this.transType = transType;
        this.name = name;
    }
}
