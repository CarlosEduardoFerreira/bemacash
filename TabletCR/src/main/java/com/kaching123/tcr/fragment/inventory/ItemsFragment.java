package com.kaching123.tcr.fragment.inventory;

import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.kaching123.tcr.adapter.IObjectsAdapter;
import com.kaching123.tcr.adapter.ObjectCursorDragAdapter;
import com.kaching123.tcr.commands.store.inventory.UpdateItemOrderCommand;
import com.kaching123.tcr.commands.store.inventory.UpdateItemOrderCommand.BaseUpdateItemOrderCommandCallback;
import com.kaching123.tcr.fragment.catalog.BaseItemsPickFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ItemExtView2.ItemTable;
import com.kaching123.tcr.util.DrawableUtil;
import com.kaching123.tcr.util.UnitUtil;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;
import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;
import static com.kaching123.tcr.model.ContentValuesUtil._castToReal;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * Created by vkompaniets on 27.11.13.
 */
@EFragment(R.layout.inventory_items_fragment)
public class ItemsFragment extends BaseItemsPickFragment {

    public static final String LOAD_ALL_CATEGORIES = "load_all_categories";
    private static final int ITEMS_LOADER_ID = 0;

    @ViewById
    protected DragSortListView list;

    private String textFilter;

    private boolean useOnlyNearTheEnd;

    private boolean sortByName;

    private boolean draggable;

    private String departmentGuid;

    public boolean composer;
    public boolean composition;
    public boolean reference;
    public boolean forSale;
    public boolean hasModifiers;
    public boolean serial;
    public boolean child;

    @Override
    protected IObjectsAdapter<ItemExModel> createAdapter() {
        Adapter adapter = new Adapter(getActivity(), draggable);
        list.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void setListener(final IItemListener listener) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener == null) {
                    return;
                }
                ItemExModel model = (ItemExModel) parent.getItemAtPosition(position);
                assert model != null;
                listener.onItemSelected(id, model);
            }
        });
    }

    public void setDepartment(String departmentGuid) {
        this.departmentGuid = departmentGuid;
    }

    @Override
    public void setCategory(String categoryGuid) {
        draggable = !LOAD_ALL_CATEGORIES.equals(categoryGuid);
        list.setDragEnabled(draggable);
        ((Adapter) adapter).setDraggable(draggable);
        super.setCategory(categoryGuid);
    }

    @Override
    public Loader<List<ItemExModel>> onCreateLoader(int loaderId, Bundle args) {
        Logger.d("ItemsListFragment onCreateLoader");
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(URI_ITEMS);
        builder.projection(ItemExFunction.PROJECTION);

        builder.orderBy(sortByName && LOAD_ALL_CATEGORIES.equals(categoryGuid)? ItemTable.DESCRIPTION : ItemTable.ORDER_NUM);

        builder.where(ItemTable.IS_DELETED + " = ?", 0);

        if (categoryGuid != null && !LOAD_ALL_CATEGORIES.equals(categoryGuid)) {
            builder.where(ItemTable.CATEGORY_ID + " = ? ", categoryGuid);
        }

        if (!TextUtils.isEmpty(textFilter)) {
            builder.where(ItemTable.DESCRIPTION + " like ? " + " OR " + ItemTable.EAN_CODE + " like ? ", "%" + textFilter + "%", "%" + textFilter + "%");
        }
        if (this.useOnlyNearTheEnd) {
            builder.where(ItemTable.STOCK_TRACKING + " = ? ", "1");
            builder.where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castToReal(ItemTable.MINIMUM_QTY));
        }

        if (serial) {
            builder.where(ItemTable.SERIALIZABLE + " = ? ", "1");
        } else if (composer) {
            builder.where(ShopSchema2.ItemExtView2.HostComposerTable.ID + " IS NOT NULL");
        } else if (composition) {
            builder.where(ShopSchema2.ItemExtView2.ChildComposerTable.ID + " IS NOT NULL");
        } else if (reference) {
            builder.where(ItemTable.ITEM_REF_TYPE + " <> ? ", "0");
        } else if (child) {
            builder.where(ItemTable.REFERENCE_ITEM_ID + " IS NOT NULL OR " + ShopSchema2.ItemExtView2.ItemMatrixTable.PARENT_GUID + " IS NOT NULL");
        } else if (forSale) {
            builder.where(ItemTable.SALABLE + " = ? ", "0");
        }
        Loader<List<ItemExModel>> loader = builder.transform(new ItemExFunction()).build(getActivity());
        return loader;
    }

    public void setTextFilter(String filter) {
        this.textFilter = filter;
        Logger.d("restartLoader from setTextFilter");
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(ITEMS_LOADER_ID, Bundle.EMPTY, this);
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
        restartLoader();
    }


    public void setUseOnlyNearTheEnd(boolean useOnlyNearTheEnd) {
        this.useOnlyNearTheEnd = useOnlyNearTheEnd;
    }

    public void sortByName(boolean sortByName) {
        this.sortByName = sortByName;
        Logger.d("restartLoader from sortByName");
        restartLoader();
    }

    private class Adapter extends ObjectCursorDragAdapter<ItemExModel> implements DragSortListView.DropListener {

        private Drawable pencilDrawable;
        private Drawable pencilTransparent;

        private boolean draggable;

        public Adapter(Context context, boolean draggable) {
            super(context);

            pencilDrawable = context.getResources().getDrawable(R.drawable.pencil);
            DrawableUtil.boundDrawable(pencilDrawable);

            pencilTransparent = context.getResources().getDrawable(R.drawable.square_opacity);
            DrawableUtil.boundDrawable(pencilTransparent);

            this.draggable = draggable;
        }

        public void marks(LinearLayout holder,
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

        @Override
        protected View newView(int position, ViewGroup parent) {
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

        @Override
        protected View bindView(View convertView, int position, ItemExModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (item == null)
                return convertView;

            holder.description.setText(item.description);

            showEanOrProductCode(holder, item.productCode, item.eanCode);

            showPrice(holder.cost, item.cost);
            showPrice(holder.price, item.price);

            holder.price.setCompoundDrawables(null, null, item.priceType == PriceType.OPEN ? pencilDrawable : pencilTransparent, null);

            if (UnitUtil.isPcs(item.priceType)) {
                showInteger(holder.qty, item.availableQty);
                holder.units.setText(null);
            } else {
                showBrandQtyInteger(holder.qty, item.availableQty);
                holder.units.setText(UnitLabelModel.getUnitLabelShortcut(getContext(), item));

            }

            showPrice(holder.totalCost, getSubTotal(item.availableQty, item.cost));

            holder.drag.setVisibility(draggable ? View.VISIBLE : View.INVISIBLE);

            marks(holder.status,
                    item.isAComposer,
                    item.isAComposisiton && !item.isSerializable(),
                    item.refType == ItemRefType.Reference,
                    item.isSalable,
                    item.hasModificators(),
                    item.isSerializable(),
                    !TextUtils.isEmpty(item.referenceItemGuid) || !TextUtils.isEmpty(item.matrixGuid));

            return convertView;
        }

        @Override
        public void drop(int from, int to) {
            updateItemOrder();
            try {
                super.drop(from, to);
                HANDLER.removeCallbacksAndMessages(null);
                HANDLER.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateItemOrder();
                        } catch (Exception bunny) {
                        }
                    }
                }, 3000);

            } catch (CursorIndexOutOfBoundsException pokemon) {
            }
        }

        private void updateItemOrder() {
            String[] guids = new String[getCount()];
            for (int i = 0; i < getCount(); i++) {
                guids[i] = getItem(i).guid;
            }

            UpdateItemOrderCommand.start(getContext(), guids, new BaseUpdateItemOrderCommandCallback() {
                @Override
                protected void onUpdateSuccess() {
                    Logger.d("Update item order succeeded");
                }

                @Override
                protected void onUpdateFailure() {
                    Logger.d("Update item order failed");
                }
            });
        }

        public void setDraggable(boolean draggable) {
            this.draggable = draggable;
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

    private final static Handler HANDLER = new Handler();

}
