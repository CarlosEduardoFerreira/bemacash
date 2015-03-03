package com.kaching123.tcr.fragment.reports.sub;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;

/**
 * Created by gdubina on 03/02/14.
 */
@EFragment(R.layout.reports_returned_items_list_fragment)
public class ReturnedItemsFragment extends SalesByItemsFragment{

    public static ReturnedItemsFragment instance(long startTime, long endTime, long resisterId) {
        return ReturnedItemsFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).build();
    }

    @Override
    protected boolean isSale() {
        return false;
    }

    /*
    @ViewById(android.R.id.list)
    protected ListView listView;

    @FragmentArg
    protected long startTime;

    @FragmentArg
    protected long endTime;

    @FragmentArg
    protected long resisterId;

    private ItemsAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter = new ItemsAdapter(getActivity()));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ReturnedItemsReportQuery.query(getActivity(), startTime, endTime, resisterId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.changeCursor(null);
    }

    @Override
    public void updateData(long startTime, long endTime, long resisterId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.resisterId = resisterId;
        getLoaderManager().restartLoader(0, null, this);
    }


    private class ItemsAdapter extends ResourceCursorAdapter {

        public ItemsAdapter(Context context) {
            super(context, R.layout.reports_returned_items_item_view, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            v.setTag(new UiHolder(
                    (TextView) v.findViewById(R.id.description),
                    (TextView) v.findViewById(R.id.qty),
                    (TextView) v.findViewById(R.id.price),
                    (TextView) v.findViewById(R.id.total)
            ));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            BigDecimal qty = _decimal(cursor.getString(1));
            BigDecimal price = _decimal(cursor.getString(2));

            UiHolder holder = (UiHolder) view.getTag();

            holder.description.setText(cursor.getString(0));
            showInteger(holder.qty, qty);
            showPrice(holder.price, price);
            showPrice(holder.total, CalculationUtil.getSubTotal(qty, price));
        }
    }

    private static class UiHolder {
        private TextView description;
        private TextView price;
        private TextView qty;
        private TextView total;

        private UiHolder(TextView description, TextView price, TextView qty, TextView total) {
            this.description = description;
            this.price = price;
            this.qty = qty;
            this.total = total;
        }
    }
*/
}
