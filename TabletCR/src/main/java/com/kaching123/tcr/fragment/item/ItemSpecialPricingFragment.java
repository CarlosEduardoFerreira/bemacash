package com.kaching123.tcr.fragment.item;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.component.BrandTextWatcher;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.model.TBPModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.TBPRegisterView2.TbpTable;
import com.kaching123.tcr.store.ShopSchema2.TBPRegisterView2.TbpXRegisterTable;
import com.kaching123.tcr.store.ShopStore.TBPRegisterView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_special_pricing_fragment)
public class ItemSpecialPricingFragment extends ItemBaseFragment {

    @ViewById protected ListView list;

    private TBPAdapter adapter;

    private boolean tbpOverlaps;

    @Override
    protected void newItem(){}

    @Override
    protected void setViews() {
        adapter = new TBPAdapter(getActivity());
        list.setAdapter(adapter);

        getLoaderManager().restartLoader(0, null, new TBPLoader());
    }

    @Override
    protected void setModel() {

    }

    @Override
    public void collectData() {
        for (int i = 0; i < adapter.getCount(); i++){
            TBPWrapper item = adapter.getItem(i);
            getModel().setPrice(item.isChecked ? item.price : null, item.model.priceLevel);
        }
    }

    @Override
    public boolean validateData() {
        List<TBPModel> checkedModels = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++){
            TBPWrapper item = adapter.getItem(i);
            if (item.isChecked && item.price != null)
                checkedModels.add(item.model);
        }

        int size = checkedModels.size();
        for (int i = 0; i < size; i++){
            for (int j = i + 1; j < size; j++){
                if (checkedModels.get(i).isOverlapping(checkedModels.get(j))){
                    Toast.makeText(getActivity(), R.string.special_price_overlapping_msg, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }

    private class TBPLoader implements LoaderCallbacks<List<TBPWrapper>>{

        @Override
        public Loader<List<TBPWrapper>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(TBPRegisterView.URI_CONTENT))
                    .where(TbpXRegisterTable.REGISTER_ID + " = ?", getApp().getRegisterId())
                    .where(TbpTable.IS_ACTIVE + " = ?", 1)
                    .orderBy(TbpTable.PRICE_LEVEL)
                    .transform(new Function<Cursor, List<TBPWrapper>>() {
                        @Override
                        public List<TBPWrapper> apply(Cursor input) {
                            ArrayList<TBPWrapper> output = new ArrayList<>(input.getCount());
                            while (input.moveToNext()){
                                TBPModel tbp = TBPModel.fromView(input);
                                BigDecimal price = getModel().getPrice(tbp.priceLevel);
                                boolean isChecked = price != null;
                                output.add(new TBPWrapper(tbp, price, isChecked));
                            }
                            return output;
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<TBPWrapper>> loader, List<TBPWrapper> data) {
            adapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<List<TBPWrapper>> loader) {
            adapter.changeCursor(null);
        }
    }

    private class TBPAdapter extends ObjectsCursorAdapter<TBPWrapper>{

        public TBPAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.tbp_item_view, parent, false);
            ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, final TBPWrapper item) {
            final ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.description.setText(item.model.description);
            holder.cb.setChecked(item.isChecked);
            holder.price.setEnabled(item.isChecked);
            showPrice(holder.price, item.price);
            holder.position = position;

            return convertView;
        }
    }

    private class ViewHolder {
        TextView description;
        EditText price;
        CheckBox cb;
        int position = -1;

        ViewHolder(View v){
            this.description = (TextView) v.findViewById(R.id.description);
            this.price = (EditText) v.findViewById(R.id.price);
            this.cb = (CheckBox) v.findViewById(R.id.cb);

            InputFilter[] currencyFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
            price.setFilters(currencyFilter);
            price.addTextChangedListener(new BrandTextWatcher(price, true){
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    super.onTextChanged(s, start, before, count);
                    if (position == -1)
                        return;

                    if (price.isEnabled())
                        adapter.getItem(position).price = parseBigDecimal(s.toString(), null);
                }
            });

            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (position == -1)
                        return;

                    TBPWrapper item = adapter.getItem(position);
                    item.isChecked = isChecked;
                    price.setEnabled(isChecked);
                    showPrice(price, isChecked ? item.price : null);
                }
            });
        }
    }

    private class TBPWrapper {
        TBPModel model;
        BigDecimal price;
        boolean isChecked;

        public TBPWrapper(TBPModel model, BigDecimal price, boolean isChecked) {
            this.model = model;
            this.price = price;
            this.isChecked = isChecked;
        }
    }

}
