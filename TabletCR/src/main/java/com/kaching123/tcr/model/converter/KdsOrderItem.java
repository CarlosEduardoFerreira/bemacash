package com.kaching123.tcr.model.converter;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by long on 7/7/2016.
 */
@Root(name = "Item")
public class KdsOrderItem {
    private static final String SMALL_AMPERSAND= "&lt;";
    private static final String BIG_AMPERSAND= "&gt;";
    private static final String AND_AMPERSAND= "&amp;";
    private static final String QUOTE_AMPERSAND= "&apos;";
    private static final String DOUBLE_QUOTE_AMPERSAND= "&quot;";
    private static final String PERCENT_AMPERSAND= "&#37;";
    @Element(name = "ID")
    String id;
    @Element(name = "TransType")
    int transType;
    @Element(name = "Name")
    String name;
    @Element(name = "Quantity")
    String quantity;
    @Element(name = "Category", required = false)
    String category;
    @Element(name = "KDSStation")
    String kdsStation;

    public ArrayList<KDSModifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(ArrayList<KDSModifier> modifiers) {
        this.modifiers = modifiers;
    }

    @ElementList(required = false)
    ArrayList<KDSModifier> modifiers;
    public KdsOrderItem (){
        super();
    }

    public KdsOrderItem (String id, int transType, String name, String category, String quantity, String kdsStation){
        this(id, transType, name, quantity, kdsStation);
        this.category = category;
    }
    public KdsOrderItem (String id, int transType, String name, String quantity, String kdsStation){
        this.id = id;
        this.transType = transType;
        this.name = CharacterFilter(name);
        this.quantity = quantity;
        this.kdsStation = kdsStation;
    }

    private String CharacterFilter(String name)
    {
        name.replace("<",SMALL_AMPERSAND);
        name.replace(">",BIG_AMPERSAND);
        name.replace("<",AND_AMPERSAND);
        name.replace("<",QUOTE_AMPERSAND);
        name.replace("<",DOUBLE_QUOTE_AMPERSAND);
        name.replace("<",PERCENT_AMPERSAND);
        return name;
    }
}
