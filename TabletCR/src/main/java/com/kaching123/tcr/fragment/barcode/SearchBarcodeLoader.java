package com.kaching123.tcr.fragment.barcode;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Optional;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand.UnitCallback;
import com.kaching123.tcr.commands.wireless.PickItemForUnitCommand;
import com.kaching123.tcr.jdbc.converters.BarcodePrefixJdbcConverter.BarcodePrefixes;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.store.ShopStore.ItemExtView;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by gdubina on 23.01.14.
 */
public class SearchBarcodeLoader implements LoaderCallbacks<Optional<ItemExModel>> {

    private static final Uri URI_ITEMS = ShopProvider.getContentUriGroupBy(ItemExtView.URI_CONTENT, ItemTable.GUID);

    private static final int CODE_LENGTH = 2;

    private FragmentActivity context;
    private int loaderId;

    private String code;
    private String barcode;
    private BigDecimal price;
    private BigDecimal quantity;
    private String orderId;

    public SearchBarcodeLoader(FragmentActivity context, int loaderId, String barcode) {
        this.loaderId = loaderId;
        this.context = context;
        parseBarcode(barcode);
    }

    private void parseBarcode(String barcode) {
        this.barcode = barcode;
        code = barcode;

        if (barcode == null || barcode.length() != TcrApplication.BARCODE_MAX_LEN) {
            Logger.d("parseBarcode(): barcode length invalid; barcode: " + barcode);
            return;
        }

        int code;
        try {
            code = Integer.valueOf(barcode.substring(0, CODE_LENGTH));
        } catch (NumberFormatException e) {
            Logger.d("parseBarcode(): barcode invalid - no code; barcode: " + barcode);
            return;
        }

        BarcodePrefixes barcodePrefixes = ((TcrApplication) context.getApplicationContext()).getBarcodePrefixes();
        int codeLength = 0;
        int numberLength = 0;
        int numberDecimals = 0;
        boolean isPrice = true;
        if (code == barcodePrefixes.code10DItem) {
            codeLength = 10;
        } else if (code == barcodePrefixes.code6DItem4DPrice) {
            codeLength = 6;
            numberLength = 4;
            numberDecimals = 2;
            isPrice = true;
        } else if (code == barcodePrefixes.code5DItem5DPrice) {
            codeLength = 5;
            numberLength = 5;
            numberDecimals = 2;
            isPrice = true;
        } else if (code == barcodePrefixes.code4DItem6DPrice) {
            codeLength = 4;
            numberLength = 6;
            numberDecimals = 2;
            isPrice = true;
        } else if (code == barcodePrefixes.code3DItem7DPrice) {
            codeLength = 3;
            numberLength = 7;
            numberDecimals = 2;
            isPrice = true;
        } else if (code == barcodePrefixes.code6DItem4DWeight3Dec) {
            codeLength = 6;
            numberLength = 4;
            numberDecimals = 3;
            isPrice = false;
        } else if (code == barcodePrefixes.code6DItem4DWeight) {
            codeLength = 6;
            numberLength = 4;
            numberDecimals = 2;
            isPrice = false;
        } else if (code == barcodePrefixes.code5DItem5DWeight3Dec) {
            codeLength = 5;
            numberLength = 5;
            numberDecimals = 3;
            isPrice = false;
        } else if (code == barcodePrefixes.code5DItem5DWeight) {
            codeLength = 5;
            numberLength = 5;
            numberDecimals = 2;
            isPrice = false;
        } else if (code == barcodePrefixes.code5DItem5DWeight0Dec) {
            codeLength = 5;
            numberLength = 5;
            numberDecimals = 0;
            isPrice = false;
        }

        if (codeLength == 0) {
            Logger.d("parseBarcode(): barcode invalid - code unknown; barcode: " + barcode);
            return;
        }

        this.code = barcode.substring(CODE_LENGTH, CODE_LENGTH + codeLength);

        if (numberLength <= 0) {
            return;
        }

        String numberString = barcode.substring(CODE_LENGTH + codeLength, CODE_LENGTH + codeLength + numberLength);
        numberString = insertDecimalDelimiter(numberString, numberDecimals);
        try {
            if (isPrice)
                price = new BigDecimal(numberString);
            else
                quantity = new BigDecimal(numberString);
        } catch (NumberFormatException e) {
            Logger.d("parseBarcode(): barcode invalid - decimal number expected; barcode: " + barcode);
            this.code = barcode;
            return;
        }
    }

    private String insertDecimalDelimiter(String numberString, int numberDecimals) {
        if (numberDecimals <= 0)
            return numberString;

        return numberString.substring(0, numberString.length() - numberDecimals) + '.' + numberString.substring(numberString.length() - numberDecimals, numberString.length());
    }

    @Override
    public Loader<Optional<ItemExModel>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(URI_ITEMS)
                .projection(ItemExFunction.PROJECTION)
                .where(ItemTable.EAN_CODE + " = ? OR " + ItemTable.PRODUCT_CODE + " = ?", code, code)
                .where(ItemTable.IS_DELETED + " = ?", 0)
                .wrap(new ItemExFunction.Wrap())
                .build(context);
    }

    protected void onMultipleSerialCodeFound(final List<Unit> unitList, final ItemExModel model) {
    }

    @Override
    public void onLoadFinished(Loader<Optional<ItemExModel>> loader, final Optional<ItemExModel> itemExModel) {
        context.getSupportLoaderManager().destroyLoader(loader.getId());

        context.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                final ItemExModel model = itemExModel.orNull();
                if (model == null) {
                    CollectUnitsCommand.start(context, null, null, orderId, barcode, null, true, true, new UnitCallback() {
                        @Override
                        protected void handleSuccess(final List<Unit> unitList) {
                            if (unitList.size() == 0) {
                                onPostExecute(model, price, quantity);
                            } else if (unitList.size() > 1) {
                                onMultipleSerialCodeFound(unitList, model);
                            } else {
                                PickItemForUnitCommand.start(context, unitList.get(0), new PickItemForUnitCommand.UnitCallback() {
                                    @Override
                                    protected void handleSuccess(ItemExModel unit) {
                                        onPostExecute(unit, unitList.get(0), price, quantity);
                                        //onPostExecute(unit, price, quantity);
                                    }

                                    @Override
                                    protected void handleError() {
                                        onPostExecute(model, price, quantity);
                                    }
                                });
                                //onPostExecute(unit.get(0), price, quantity);
                            }
                        }

                        @Override
                        protected void handleError() {
                            onPostExecute(model, price, quantity);
                        }
                    });
                } else {
                    if (price != null && quantity == null) {
                        quantity = CalculationUtil.divide(price, model.price);
                    }
                    if (model.priceType == PriceType.UNIT_PRICE) {
                        onPostExecute(model, model.price, quantity);
                    } else {
                        onPostExecute(model, price, quantity);
                    }
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Optional<ItemExModel>> itemExModelLoader) {

    }

    protected void onPostExecute(ItemExModel itemExModel, BigDecimal price, BigDecimal quantity) {

    }

    protected void onPostExecute(ItemExModel item, Unit unit, BigDecimal price, BigDecimal quantity) {

    }

    protected void onPreExecute() {

    }

    public SearchBarcodeLoader setOrderIdForUnit(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public void execute() {
        onPreExecute();
        context.getSupportLoaderManager().restartLoader(loaderId, null, this);
    }
}
