package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by long on 7/7/2016.
 */
@Root(name = "Order")
public class KdsOrder {
    @Element(name = "ID")
    String id;
    /** It must be unique in whole system, it can be a string */
    @Element(name = "PosTerminal")
    long posTerminal;
    @Element(name = "TransType")
    int transType;
    /**
     * 1 Add new order, Append order to the last position
     * 2 Delete this order. If use this value, the KDS just need order ID tag.
     * 3 Modify this order. Just tranfer all changed order tag.
     * 4 Reserved for future use
     * 5 Ask this order kitchen status
     */
    @Element(name = "OrderStatus")
    int orderStatus;
    /**
     * 0 unpaid
     * 1 Paid
     * 2 In Process
     */
    @Element(name = "OrderType")
    String orderType;
    /**
     * ""- Normal order
     * RUSH- Rush Order
     * Fire- fire order
     */
    @Element(name = "ServerName")
    String serverName;
    @Element(name = "Destination")
    String destination;
    @Element(name = "CustomersName", required = false)
    String guestTable;
    @Element(name = "UserInfo", required = false)
    String userInfo;

    public KDSOrderMessage getOrderMessage() {
        return orderMessage;
    }

    public void setOrderMessage(KDSOrderMessage orderMessage) {
        this.orderMessage = orderMessage;
    }

    @Element(name = "OrderMessage", required = false)
    KDSOrderMessage orderMessage;

    public ArrayList<KdsOrderItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<KdsOrderItem> items) {
        this.items = items;
    }

    @ElementList
    ArrayList<KdsOrderItem> items;
    public KdsOrder (){
        super();
    }

    public KdsOrder(String id, int posTerminal, int transType, int orderStatus, String orderType, String serverName, String destination, String guestTable, String userInfo, ArrayList<KdsOrderItem> items) {
        this(id, posTerminal, transType, orderStatus, orderType, serverName, destination, guestTable, userInfo);
        this.items = items;
    }

    public KdsOrder(String id, int posTerminal, int transType, int orderStatus, String orderType, String serverName, String destination, String guestTable, String userInfo) {
        this.id = id;
        this.posTerminal = posTerminal;
        this.transType = transType;
        this.orderStatus = orderStatus;
        this.orderType = orderType;
        this.serverName = serverName;
        this.destination = destination;
        this.guestTable = guestTable;
        this.userInfo = userInfo;
    }

    public KdsOrder(String id, long posTerminal, int transType, int orderStatus, String orderType, String serverName, String destination) {
        this.id = id;
        this.posTerminal = posTerminal;
        this.transType = transType;
        this.orderStatus = orderStatus;
        this.orderType = orderType;
        this.serverName = serverName;
        this.destination = destination;
    }

    public KdsOrder(String id) {
        this.id = id;
        this.transType = 2;
    }

}
