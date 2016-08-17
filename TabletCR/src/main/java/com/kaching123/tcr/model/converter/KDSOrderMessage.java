package com.kaching123.tcr.model.converter;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;

/**
 * Created by long on 7/12/2016.
 */
public class KDSOrderMessage {
    @Element(name = "Count")
    int count = 1;
//    @ElementList
//    ArrayList<String> message;
    @Element(name = "S0")
    String message;
}
