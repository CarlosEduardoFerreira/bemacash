package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.store.ShopStore.WirelessTable;
import com.kaching123.tcr.websvc.api.prepaid.Product;
import com.kaching123.tcr.websvc.api.prepaid.ProductAccessPhone;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class WirelessItem implements IValueModel, Serializable {

    public String code;
    public String name;
    public boolean useFixedDenominations;
    public BigDecimal minDenomination;
    public BigDecimal maxDenomination;
    public String carrierName;
    public String countryCode;
    public String countryName;
    public WirelessType type;
    public BigDecimal[] denominations;
    public String iconUrl;
    public String dialCountryCode;
    public String TermsAndConditions;
    public ProductAccessPhone[] productAccessPhones;
    public int merchantBuyingFrequency;
    public int zipCodeBuyingFrequency;
    public double feeAmount;

    public WirelessItem() {
    }

    public WirelessItem(String code, String name, boolean useFixedDenominations,
                        BigDecimal minDenomination, BigDecimal maxDenomination, String carrierName,
                        String countryCode, String countryName, String type, BigDecimal[] denominations, String urlIcon, String dialCountryCode, String TermsAndConditions, ProductAccessPhone[] productAccessPhones, int merchantBuyingFrequency, int zipCodeBuyingFrequency, double feeAmount) {
        this.code = code;
        this.name = name;
        this.useFixedDenominations = useFixedDenominations;
        this.minDenomination = minDenomination;
        this.maxDenomination = maxDenomination;
        this.carrierName = carrierName;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.type = WirelessType.fromString(type);
        this.denominations = denominations;
        this.iconUrl = urlIcon;
        this.dialCountryCode = dialCountryCode;
        this.TermsAndConditions = TermsAndConditions;
        this.productAccessPhones = productAccessPhones;
        this.merchantBuyingFrequency = merchantBuyingFrequency;
        this.zipCodeBuyingFrequency = zipCodeBuyingFrequency;
        this.feeAmount = feeAmount;
    }

    public WirelessItem(Product product) {
        this(product.code,
                product.name,
                product.useFixedDenominations,
                BigDecimal.valueOf(product.minDenomination),
                BigDecimal.valueOf(product.maxDenomination),
                product.carrierName,
                product.countryCode,
                product.countryName,
                product.type,
                null,
                product.imageUrl,
                product.dialCountryCode,
                product.termsAndConditions,
                null,
                product.merchantBuyingFrequency,
                product.zipCodeBuyingFrequency,
                product.feeAmount);
        if (product.denominations != null) {
            denominations = new BigDecimal[product.denominations.size()];
            int i = 0;
            for (Number value : product.denominations) {
                denominations[i] = BigDecimal.valueOf(value.doubleValue());
                i++;
            }
        }
//        if (product.name.contains("2014"))
//            if (product.productAccessPhones != null)
//                for (ProductAccessPhone accessPhone1 : product.productAccessPhones)
//                    Logger.d("trace 2014:" + accessPhone1.state + accessPhone1.city + accessPhone1.language + accessPhone1.phoneNumber);
        if (product.accessPhones != null) {
            productAccessPhones = new ProductAccessPhone[product.accessPhones.size()];
            int i = 0;
            for (ProductAccessPhone accessPhone : product.accessPhones) {
                productAccessPhones[i] = accessPhone;
                i++;
            }
        }
    }

    public WirelessItem(Cursor c) {

        this(c.getString(c.getColumnIndex(WirelessTable.CODE)),
                c.getString(c.getColumnIndex(WirelessTable.NAME)),
                _bool(c, c.getColumnIndex(WirelessTable.USEFIXEDDENOMINATIONS)),
                _decimal(c, c.getColumnIndex(WirelessTable.MINDENOMINATION)),
                _decimal(c, c.getColumnIndex(WirelessTable.MAXDENOMINATION)),
                c.getString(c.getColumnIndex(WirelessTable.CARRIERNAME)),
                c.getString(c.getColumnIndex(WirelessTable.COUNTRYCODE)),
                c.getString(c.getColumnIndex(WirelessTable.COUNTRYNAME)),
                c.getString(c.getColumnIndex(WirelessTable.TYPE)),
                null,
                c.getString(c.getColumnIndex(WirelessTable.URL)),
                c.getString(c.getColumnIndex(WirelessTable.DIALCOUNTRYCODE)),
                c.getString(c.getColumnIndex(WirelessTable.TERMSANDCONDITIONS)),
                null,
                c.getInt(c.getColumnIndex(WirelessTable.MERCHANGTBUYINGFREQUENCY)),
                c.getInt(c.getColumnIndex(WirelessTable.ZIPCODEBUYFREQUENCY)),
                c.getDouble(c.getColumnIndex(WirelessTable.FEEAMOUNT))
        );
        String[] buffer = c.getString(c.getColumnIndex(WirelessTable.DENOMINATIONS)).split(";");
        if (buffer != null) {
            BigDecimal[] items = new BigDecimal[buffer.length];
            int i = 0;
            for (String item : buffer) {
                try {
                    items[i] = new BigDecimal(item);
                } catch (NumberFormatException ignore) {
                    items[i] = BigDecimal.ZERO;
                }
                i++;
            }
            this.denominations = items;

        }

        String[] buffer_pap = c.getString(c.getColumnIndex(WirelessTable.PRODUCTACCESSPHONES)).split(";");
        if (buffer_pap != null) {
            ProductAccessPhone[] accessPhones = new ProductAccessPhone[buffer_pap.length];
            int i = 0;
            for (String item : buffer_pap) {
                try {
                    String[] subitems = item.split("/");
                    accessPhones[i] = new ProductAccessPhone();
                    accessPhones[i].state = subitems[0] != null ? subitems[0] : "";
                    accessPhones[i].city = subitems[1] != null ? subitems[1] : "";
                    accessPhones[i].language = subitems[2] != null ? subitems[2] : "";
                    accessPhones[i].phoneNumber = subitems[3] != null ? subitems[3] : "";
                } catch (Exception e) {
                    accessPhones[i] = new ProductAccessPhone();
                }
                i++;

            }
            this.productAccessPhones = accessPhones;
        }
    }

    @Override
    public String getGuid() {
        return code;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(WirelessTable.CODE, code);
        v.put(WirelessTable.NAME, name);
        v.put(WirelessTable.USEFIXEDDENOMINATIONS, useFixedDenominations);
        v.put(WirelessTable.MINDENOMINATION, _decimal(minDenomination));
        v.put(WirelessTable.MAXDENOMINATION, _decimal(maxDenomination));
        v.put(WirelessTable.CARRIERNAME, carrierName);
        v.put(WirelessTable.COUNTRYCODE, countryCode);
        v.put(WirelessTable.COUNTRYNAME, countryName);
        v.put(WirelessTable.DIALCOUNTRYCODE, dialCountryCode);
        v.put(WirelessTable.TYPE, type.getName());
        v.put(WirelessTable.TERMSANDCONDITIONS, TermsAndConditions);
        v.put(WirelessTable.MERCHANGTBUYINGFREQUENCY, merchantBuyingFrequency);
        v.put(WirelessTable.ZIPCODEBUYFREQUENCY, zipCodeBuyingFrequency);
        v.put(WirelessTable.FEEAMOUNT, feeAmount);
        StringBuilder builder = new StringBuilder();
        if (denominations == null) {
            v.put(WirelessTable.DENOMINATIONS, "");
        } else {
            for (BigDecimal item : denominations) {
                if (builder.length() > 0) {
                    builder.append(";");
                }
                builder.append(UiHelper.valueOf(item));
            }
            v.put(WirelessTable.DENOMINATIONS, builder.toString());
        }
        v.put(WirelessTable.URL, iconUrl);

        StringBuilder sb = new StringBuilder();
        if (productAccessPhones == null) {
            v.put(WirelessTable.PRODUCTACCESSPHONES, "");
        } else {
            for (ProductAccessPhone accessPhone : productAccessPhones) {
                sb.append(accessPhone.state + "/" + accessPhone.city + "/" + accessPhone.language + "/" + accessPhone.phoneNumber);
                sb.append(";");
            }
            v.put(WirelessTable.PRODUCTACCESSPHONES, sb.toString());

            Logger.d("trace accessphone: " + sb.toString());
        }
        return v;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isLongDistance() {
        return type.isLongDistance();
    }

    public boolean isPinless() {
        return type.isPinless();
    }

    public boolean isWirelessInternational() {
        return type.isWirelessInternational();
    }

    public boolean isWireless() {
        return type.isWireless();
    }

    public boolean isPinBased() {
        return type.isPin();
    }

    public ArrayList<WirelessItemDenomination> denominations() {
        ArrayList<WirelessItemDenomination> result = new ArrayList<WirelessItemDenomination>();
        if (denominations != null) {
            for (BigDecimal item : denominations) {
                if (item.compareTo(BigDecimal.ZERO) > 0) {
                    WirelessItemDenomination demon = new WirelessItemDenomination();
                    demon.denomination = item;
                    result.add(demon);
                }
            }

        }
        if (minDenomination != null && maxDenomination != null && BigDecimal.ZERO.compareTo(maxDenomination) < 0) {
            WirelessItemDenomination demon = new WirelessItemDenomination();
            demon.min = minDenomination;
            demon.max = maxDenomination;
            result.add(demon);
        }
        return result;
    }
}
