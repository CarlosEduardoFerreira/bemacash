package com.kaching123.tcr.model.converter;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by long on 7/7/2016.
 */
@Root(name = "Transaction")
public class KdsTransaction {
    public KdsOrder getOrder() {
        return order;
    }

    public void setOrder(KdsOrder order) {
        this.order = order;
    }

    @Element(name = "Order")
    KdsOrder order;

    public KdsTransaction (KdsOrder order){
        this.order = order;
    }
    public KdsTransaction (){
        super();
    }
}
