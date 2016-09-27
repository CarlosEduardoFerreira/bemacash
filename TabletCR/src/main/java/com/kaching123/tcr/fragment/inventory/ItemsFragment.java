package com.kaching123.tcr.fragment.inventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.store.inventory.BatchUpdateItemOrderCommand;
import com.kaching123.tcr.fragment.catalog.BaseItemsPickFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.util.DrawableUtil;
import com.kaching123.tcr.util.UnitUtil;
import com.mobeta.android.dslv.DragSortCursorAdapter;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showBrandQty;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.model.ContentValuesUtil._castAsReal;
import static com.kaching123.tcr.model.ContentValuesUtil._lower;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * Created by vkompaniets on 27.11.13.
 */
@EFragment(R.layout.inventory_items_fragment)
public class ItemsFragment extends BaseItemsPickFragment {

    public static final String LOAD_ALL_CATEGORIES = "load_all_categories";

    @ViewById
    protected DragSortListView list;

    private String textFilter;

    private boolean useOnlyNearTheEnd;

    public boolean composer;
    public boolean composition;
    public boolean reference;
    public boolean forSale;
    public boolean hasModifiers;
    public boolean serial;
    public boolean child;

    private Adapter2 adapter2;

    private ItemCursorLoader loader = new ItemCursorLoader();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter2 = new Adapter2(getActivity());
        list.setAdapter(adapter2);
    }

    @Override
    protected ObjectsCursorAdapter<ItemExModel> createAdapter() {
        return null;
    }

    @Override
    protected void restartItemsLoader() {
        getLoaderManager().restartLoader(0, null, loader);
    }

    @Override
    public void setListener(final IItemListener listener) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener == null) {
                    return;
                }
                ItemExFunction func = new ItemExFunction();
                ItemExModel model = func.apply((Cursor) adapter2.getItem(position));
                assert model != null;
                listener.onItemSelected(id, model);
            }
        });
    }

    @Override
    public Loader<List<ItemExModel>> onCreateLoader(int loaderId, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<ItemExModel>> loader, List<ItemExModel> list) {

    }


    public void setTextFilter(String filter) {
        this.textFilter = filter;
        Logger.d("restartLoader from setTextFilter");
        restartItemsLoader();
    }

    public void setFilter(boolean composer,
                          boolean composition,
                          boolean reference,
                          boolean forSale,
                          boolean hasModifiers,
                          boolean serial,
                          boolean child) {
        this.composer = composer;
        this.composition = composition;
        this.reference = reference;
        this.forSale = forSale;
        this.hasModifiers = hasModifiers;
        this.serial = serial;
        this.child = child;
        Logger.d("restartLoader from setFilter");
        restartItemsLoader();
    }


    public void setUseOnlyNearTheEnd(boolean useOnlyNearTheEnd) {
        this.useOnlyNearTheEnd = useOnlyNearTheEnd;
    }

    public void sortOrderChanged() {
        restartItemsLoader();
    }

    private class ItemCursorLoader implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Logger.d("ItemsListFragment onCreateLoader");
            boolean draggable = true;
            CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(URI_ITEMS);
            builder.projection(ItemExFunction.PROJECTION);

            boolean isABCSort = ((SuperBaseActivity) getActivity()).getApp().isEnableABCOrder();
            builder.orderBy(isABCSort ? _lower(ItemTable.DESCRIPTION) : ItemTable.ORDER_NUM);
            draggable &= !isABCSort;

            builder.where(ItemTable.IS_DELETED + " = ?", 0);

            if (categoryGuid != null && !LOAD_ALL_CATEGORIES.equals(categoryGuid)) {
                builder.where(ItemTable.CATEGORY_ID + " = ? ", categoryGuid);
            }else{
                draggable = false;
            }

            if (!TextUtils.isEmpty(textFilter)) {
                builder.where(ItemTable.PRODUCT_CODE + " like? " + " OR " + ItemTable.DESCRIPTION + " like ? " + " OR " + ItemTable.EAN_CODE + " like ? ", "%" + textFilter + "%", "%" + textFilter + "%");
                draggable = false;
            }
            if (useOnlyNearTheEnd) {
                builder.where(ItemTable.STOCK_TRACKING + " = ? ", "1");
                builder.where(_castAsReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castAsReal(ItemTable.MINIMUM_QTY));
                draggable = false;
            }

            if (serial) {
                builder.where(ItemTable.SERIALIZABLE + " = ? ", "1");
                draggable = false;
            } else if (composer) {
                builder.where(ShopSchema2.ItemExtView2.HostComposerTable.ID + " IS NOT NULL");
                draggable = false;
            } else if (composition) {
                builder.where(ShopSchema2.ItemExtView2.ChildComposerTable.ID + " IS NOT NULL");
                draggable = false;
            } else if (reference) {
                builder.where(ItemTable.ITEM_REF_TYPE + " <> ? ", "0");
                draggable = false;
            } else if (child) {
                builder.where(ItemTable.REFERENCE_ITEM_ID + " IS NOT NULL OR " + ShopSchema2.ItemExtView2.ItemMatrixTable.PARENT_GUID + " IS NOT NULL");
                draggable = false;
            } else if (forSale) {
                builder.where(ItemTable.SALABLE + " = ? ", "0");
                draggable = false;
            }

            adapter2.setDraggable(draggable);
            list.setDragEnabled(draggable);

            return builder.build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            adapter2.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter2.changeCursor(null);
        }
    }

    private class Adapter2 extends DragSortCursorAdapter {

        private ItemExFunction function;
        private Drawable pencilDrawable;
        private Drawable pencilTransparent;
        private boolean draggable;

        public Adapter2(Context context) {
            super(context, null, false);

            this.function = new ItemExFunction();

            pencilDrawable = context.getResources().getDrawable(R.drawable.pencil);
            DrawableUtil.boundDrawable(pencilDrawable);

            pencilTransparent = context.getResources().getDrawable(R.drawable.square_opacity);
            DrawableUtil.boundDrawable(pencilTransparent);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.inventory_item_view, parent, false);

            ViewHolder holder = new ViewHolder();

            holder.status = (LinearLayout) convertView.findViewById(R.id.status);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.ean = (TextView) convertView.findViewById(R.id.ean);
            holder.cost = (TextView) convertView.findViewById(R.id.cost);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.qty = (TextView) convertView.findViewById(R.id.qty);
            holder.units = (TextView) convertView.findViewById(R.id.units);
            holder.totalCost = (TextView) convertView.findViewById(R.id.total_cost);
            holder.drag = (ImageView) convertView.findViewById(R.id.drag);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        public void bindView(View convertView, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            ItemExModel item = function.apply(cursor);

            if (item == null)
                return;

            holder.description.setText(item.description);

            showEanOrProductCode(holder, item.productCode, item.eanCode);

            showPrice(holder.cost, item.cost);
            showPrice(holder.price, item.price);

            holder.price.setCompoundDrawables(null, null, item.priceType == PriceType.OPEN ? pencilDrawable : pencilTransparent, null);

            if (item.refType == ItemRefType.Simple) {
                if (UnitUtil.isNotUnitPriceType(item.priceType)) {
                    showBrandQtyInteger(holder.qty, item.availableQty);
                    holder.units.setText(null);
                    showPrice(holder.totalCost, getSubTotal(item.availableQty.setScale(0, BigDecimal.ROUND_FLOOR), item.cost));
                } else {
                    showBrandQty(holder.qty, item.availableQty);
                    holder.units.setText(item.shortCut);
                    showPrice(holder.totalCost, getSubTotal(item.availableQty, item.cost));
                }
            } else {
                holder.qty.setText("");
                holder.totalCost.setText("");
            }

            if (item.refType == ItemRefType.Simple) {
                if (UnitUtil.isNotUnitPriceType(item.priceType)) {
                    showBrandQtyInteger(holder.qty, item.availableQty);
                    holder.units.setText(null);
                    showPrice(holder.totalCost, getSubTotal(item.availableQty.setScale(0, BigDecimal.ROUND_FLOOR), item.cost));
                } else {
                    showBrandQty(holder.qty, item.availableQty);
                    holder.units.setText(item.shortCut);
                    showPrice(holder.totalCost, getSubTotal(item.availableQty, item.cost));
                }
            } else {
                holder.qty.setText("");
                holder.totalCost.setText("");
            }

            holder.drag.setVisibility(draggable ? View.VISIBLE : View.INVISIBLE);

            marks(holder.status,
                    item.isAComposer,
                    item.isAComposisiton && !item.isSerializable(),
                    item.refType == ItemRefType.Reference,
                    item.isSalable,
                    item.hasModificators(),
                    item.isSerializable(),
                    !TextUtils.isEmpty(item.referenceItemGuid) || !TextUtils.isEmpty(item.matrixGuid));
        }

        @Override
        public void drop(final int from, final int to) {
            if (from == to)
                return;
            super.drop(from, to);

            int count = getCount();
            String[] guids = new String[count];
            for (int i = 0; i < count; i++) {
                Cursor c = (Cursor) getItem(i);
                String guid = c.getString(c.getColumnIndex(ItemTable.GUID));
                guids[i] = guid;
            }
            BatchUpdateItemOrderCommand.start(getContext(), guids);
        }

        public void setDraggable(boolean draggable) {
            this.draggable = draggable;
        }

        void marks(LinearLayout holder,
                   boolean composer,
                   boolean composition,
                   boolean reference,
                   boolean forSale,
                   boolean hasModifiers,
                   boolean serial,
                   boolean child) {
            // Now the layout parameters, these are a little tricky at first
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            params.rightMargin = 5;
            holder.removeAllViews();

            if (composer) {
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageResource(R.drawable.ic_extension_black_18dp);
                // Let's get the root layout and add our ImageView
                holder.addView(image, 0, params);
            }
            if (composition) {
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageResource(R.drawable.ic_dashboard_black_18dp);
                // Let's get the root layout and add our ImageView
                holder.addView(image, 0, params);
            }
            if (reference) {
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageResource(R.drawable.tree_structure_24);
                // Let's get the root layout and add our ImageView
                holder.addView(image, 0, params);
            }
            if (child) {
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageResource(R.drawable.broken_link_24);
                // Let's get the root layout and add our ImageView
                holder.addView(image, 0, params);
            }
            if (!forSale) {
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageResource(R.drawable.ic_money_off_black_18dp);
                // Let's get the root layout and add our ImageView
                holder.addView(image, 0, params);
            }
            if (hasModifiers) {
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageResource(R.drawable.pie_chart_24);
                // Let's get the root layout and add our ImageView
                holder.addView(image, 0, params);
            }
            if (serial) {
                ImageView image = new ImageView(getContext());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageResource(R.drawable.barcode_24);
                // Let's get the root layout and add our ImageView
                holder.addView(image, 0, params);
            }
        }

        private void showEanOrProductCode(ViewHolder holder, String productCode, String eanCode) {
            if (TextUtils.isEmpty(eanCode)) {
                if (TextUtils.isEmpty(productCode)) {
                    holder.ean.setVisibility(View.GONE);
                } else {
                    holder.ean.setVisibility(View.VISIBLE);
                    holder.ean.setText(productCode);
                }
            } else {
                holder.ean.setVisibility(View.VISIBLE);
                holder.ean.setText(eanCode);
            }
        }
    }

    private class ViewHolder {
        LinearLayout status;
        TextView description;
        TextView ean;
        TextView cost;
        TextView price;
        TextView qty;
        TextView units;
        TextView totalCost;
        ImageView drag;
    }
}
