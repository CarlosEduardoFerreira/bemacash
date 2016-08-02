package com.kaching123.tcr.fragment.categories;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.adapter.SpinnerAdapter;
import com.kaching123.tcr.commands.store.inventory.AddCategoryCommand;
import com.kaching123.tcr.commands.store.inventory.AddCategoryCommand.BaseAddCategoryCallback;
import com.kaching123.tcr.commands.store.inventory.EditCategoryCommand;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo.ViewType;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 18.12.13.
 */
@EFragment(R.layout.categories_dialog_fragment)
public class CategoriesDialog extends StyledDialogFragment {

    public static final String DIALOG_NAME = "categories_dialog";

    private static final String EXT = ".png";

    private static final int DEPARTMENT_LOADER_ID = 0;
    private static final int CATEGORY_LOADER_ID = 1;

    @FragmentArg
    protected CategoryModel model;

    @ViewById
    protected Spinner departmentSpinner;
    @ViewById
    protected EditText title;
    @ViewById
    protected ViewGroup imageFrame;
    @ViewById
    protected StickyGridHeadersGridView imageGrid;
    @ViewById
    protected View commissionsContainer;
    @ViewById
    protected CheckedTextView commissionsEligible;
    @ViewById
    protected EditText commissions;

    private ViewType shopType = TcrApplication.get().getShopInfo().viewType;

    private DepartmentSpinnerAdapter adapter;

    private ImageAdapter imageAdapter;

    private BaseAddCategoryCallback callback;

    private CategoryNamesLoader categoryNamesLoader = new CategoryNamesLoader();

    private List<String> categoriesInDepartment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();

        int widthResource = shopType == ViewType.QUICK_SERVICE ? R.dimen.categories_dialog_quick_width : R.dimen.categories_dialog_width;
        int heightResource;
        if (getApp().isCommissionsEnabled()) {
            if (shopType == ViewType.QUICK_SERVICE) {
                heightResource = R.dimen.categories_dialog_quick_commission_height;
            } else {
                heightResource = R.dimen.categories_dialog_commission_height;
            }
        } else {
            if (shopType == ViewType.QUICK_SERVICE) {
                heightResource = R.dimen.categories_dialog_quick_height;
            } else {
                heightResource = R.dimen.categories_dialog_height;
            }
        }

        params.width = getResources().getDimensionPixelOffset(widthResource);
        params.height = getResources().getDimensionPixelOffset(heightResource);

        adapter = new DepartmentSpinnerAdapter(getActivity());
        departmentSpinner.setAdapter(adapter);

        title.setImeOptions(EditorInfo.IME_ACTION_DONE);
        title.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_DONE == i) {
                    if (doClick()) {
                        dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

        commissionsEligible.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);
        commissionsContainer.setVisibility(getApp().isCommissionsEnabled() ? View.VISIBLE : View.GONE);
        InputFilter[] decimalFilter = new InputFilter[]{new CurrencyFormatInputFilter()};
        commissions.setFilters(decimalFilter);

        departmentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, categoryNamesLoader);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (shopType == ViewType.QUICK_SERVICE) {
            imageFrame.setVisibility(View.VISIBLE);
            imageGridSetup();
        } else {
            imageFrame.setVisibility(View.GONE);
        }

        commissionsEligible.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                commissionsEligible.setChecked(!commissionsEligible.isChecked());
            }
        });

        fillViews();

        getLoaderManager().restartLoader(DEPARTMENT_LOADER_ID, null, new DepartmentsLoader());
    }

    private void fillViews() {
        if (model == null)
            return;

        title.setText(model.title);
        commissionsEligible.setChecked(model.commissionEligible);
        showPrice(commissions, model.commission);
    }

    private void imageGridSetup() {
        imageAdapter = new ImageAdapter(getActivity());
        imageAdapter.setNumColumns(getResources().getInteger(R.integer.category_icon_columns_count));
        imageGrid.setAdapter(imageAdapter);

        loadAndSetCategories();

        imageGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageAdapter.itemClicked(position, imageGrid, true);
            }
        });

        if (model != null) {
            int position = imageAdapter.getPosition(model.image);

            if (position >= 0) {
                imageAdapter.itemClicked(position, imageGrid, false);
            }
        }
    }

    private void loadAndSetCategories() {
        LinkedHashMap<String, LinkedHashSet<String>> categoryGroupsMap = new LinkedHashMap<String, LinkedHashSet<String>>();

        String foodCategoryGroup = getString(R.string.category_group_food);
        String[] foodCategoryImages = getResources().getStringArray(R.array.category_group_food);
        String beverageCategoryGroup = getString(R.string.category_group_beverage);
        String[] beverageCategoryImages = getResources().getStringArray(R.array.category_group_beverage);
        String clothingCategoryGroup = getString(R.string.category_group_clothing);
        String[] clothingCategoryImages = getResources().getStringArray(R.array.category_group_clothing);
        String electronicsCategoryGroup = getString(R.string.category_group_electronics);
        String[] electronicsCategoryImages = getResources().getStringArray(R.array.category_group_electronics);
        String generalCategoryGroup = getString(R.string.category_group_general);
        String[] generalCategoryImages = getResources().getStringArray(R.array.category_group_general);
        String colorsCategoryGroup = getString(R.string.category_group_colors);
        String[] colorsCategoryImages = getResources().getStringArray(R.array.category_group_colors);

        categoryGroupsMap.put(foodCategoryGroup, new LinkedHashSet<>(Arrays.asList(foodCategoryImages)));
        categoryGroupsMap.put(beverageCategoryGroup, new LinkedHashSet<>(Arrays.asList(beverageCategoryImages)));
        categoryGroupsMap.put(clothingCategoryGroup, new LinkedHashSet<>(Arrays.asList(clothingCategoryImages)));
        categoryGroupsMap.put(electronicsCategoryGroup, new LinkedHashSet<>(Arrays.asList(electronicsCategoryImages)));
        categoryGroupsMap.put(generalCategoryGroup, new LinkedHashSet<>(Arrays.asList(generalCategoryImages)));
        categoryGroupsMap.put(colorsCategoryGroup, new LinkedHashSet<>(Arrays.asList(colorsCategoryImages)));

        LinkedHashMap<String, Integer> imageHeadersMap = new LinkedHashMap<String, Integer>();
        ArrayList<String> sortedImages = new ArrayList<String>();
        for (Entry<String, LinkedHashSet<String>> entry : categoryGroupsMap.entrySet()) {
            LinkedHashSet<String> imagesSet = entry.getValue();
            int filteredCount = 0;
            for (String image : imagesSet) {
                //if (imageFiles.contains(image)) {
                filteredCount++;
                sortedImages.add(image);
                //}
            }
            imageHeadersMap.put(entry.getKey(), filteredCount);
        }

        imageAdapter.changeCursor(sortedImages, imageHeadersMap);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.categories_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return model == null ? R.string.categories_dialog_title_create : R.string.categories_dialog_title_edit;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return doClick();
            }
        };
    }

    private void setCallback(BaseAddCategoryCallback callback) {
        this.callback = callback;
    }

    private boolean doClick() {
        if (fieldsValid()) {
            CategoryModel newModel = bindModel(model);
            if (newModel == null) {
                EditCategoryCommand.start(getActivity(), model);
            } else {
                AddCategoryCommand.start(getActivity(), newModel, callback);
            }
            return true;
        }
        return false;
    }

    private CategoryModel bindModel(CategoryModel model) {
        String title = this.title.getText().toString().trim();
        Cursor c = (Cursor) this.departmentSpinner.getSelectedItem();
        if (c == null || c.getString(c.getColumnIndex(DepartmentTable.GUID)) == null) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_empty_department, getString(R.string.category_create_error_msg));
            return null;
        }
        String departmentGuid = c.getString(c.getColumnIndex(DepartmentTable.GUID));
        String image = imageAdapter == null ? null : imageAdapter.getSelectedItem();
        boolean isCommissionEligible = commissionsEligible.isChecked();
        BigDecimal commission = parseBigDecimal(commissions, BigDecimal.ZERO);

        if (model == null) {
            return new CategoryModel(null, departmentGuid, title, image, 0, isCommissionEligible, commission);
        }

        model.title = title;
        model.departmentGuid = departmentGuid;
        model.image = image;
        model.commissionEligible = isCommissionEligible;
        model.commission = commission;
        return null;
    }

    private boolean fieldsValid() {
        final String categoryName = this.title.getText().toString().trim();
        if (TextUtils.isEmpty(categoryName)) {
            Toast.makeText(getActivity(), R.string.categories_dialog_title_empty_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!categoryNameUnique(categoryName)) {
            Toast.makeText(getActivity(), R.string.categories_dialog_title_exists_msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isEmpty(commissions.getText())) {
            if (parseBigDecimal(commissions, BigDecimal.ZERO).compareTo(CalculationUtil.ONE_HUNDRED) == 1) {
                Toast.makeText(getActivity(), R.string.commission_validation_alert_msg, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private boolean categoryNameUnique(String name) {
        if (categoriesInDepartment == null)
            return true;

        for (String n : categoriesInDepartment) {
            if (name.equalsIgnoreCase(n))
                return false;
        }
        return true;
    }

    private class DepartmentsLoader implements LoaderCallbacks<Cursor> {

        private final Uri URI_DEPARTMENTS = ShopProvider.getContentUri(ShopStore.DepartmentTable.URI_CONTENT);

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(URI_DEPARTMENTS)
                    .projection(new String[]{DepartmentTable.ID, DepartmentTable.GUID, DepartmentTable.TITLE})
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            adapter.changeCursor(cursor);
            if (model != null) {
                departmentSpinner.setSelection(adapter.getPosition4Id(model.departmentGuid));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            adapter.changeCursor(null);
        }
    }

    private static class DepartmentSpinnerAdapter extends SpinnerAdapter {

        public DepartmentSpinnerAdapter(Context context) {
            super(context,
                    R.layout.spinner_item_dark,
                    new String[]{DepartmentTable.TITLE},
                    new int[]{android.R.id.text1},
                    R.layout.spinner_dropdown_item);
        }

        @Override
        protected String getIdColumnName() {
            return DepartmentTable.GUID;
        }
    }

    private class CategoryNamesLoader implements LoaderCallbacks<List<String>> {

        private final Uri URI_CATEGORIES = ShopProvider.getContentUri(CategoryTable.URI_CONTENT);

        @Override
        public Loader<List<String>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(URI_CATEGORIES)
                    .projection(CategoryTable.TITLE)
                    .where(CategoryTable.DEPARTMENT_GUID + " = ?", adapter.getGuid(departmentSpinner.getSelectedItemPosition()))
                    .where(CategoryTable.GUID + " != ?", model == null ? "" : model.guid)
                    .transform(new ListConverterFunction<String>() {
                        @Override
                        public String apply(Cursor cursor) {
                            return cursor.getString(0);
                        }
                    })
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
            categoriesInDepartment = data;
        }


        @Override
        public void onLoaderReset(Loader<List<String>> loader) {
            categoriesInDepartment = null;
        }
    }

    private static class ImageAdapter extends ObjectsArrayAdapter<String> implements StickyGridHeadersBaseAdapter {

        private LinkedHashMap<String, Integer> imageHeadersMap;

        private int selected = -1;

        private int numColumns;

        public ImageAdapter(Context context) {
            super(context);
        }

        public void setNumColumns(int numColumns) {
            this.numColumns = numColumns;
        }

        private int translatePosition(int position) {
            int viewPosition = position;
            int adapterPosition = 0;
            for (Integer count : imageHeadersMap.values()) {

                viewPosition += numColumns;

                adapterPosition += count;

                if (position < adapterPosition)
                    break;
                viewPosition += unFilledSpacesInHeaderGroup(count);
            }

            return viewPosition;
        }

        private int unFilledSpacesInHeaderGroup(int count) {
            int remainder = count % numColumns;
            return remainder == 0 ? 0 : numColumns - remainder;
        }

        public void changeCursor(List<String> itemsList, LinkedHashMap<String, Integer> imageHeadersMap) {
            this.imageHeadersMap = imageHeadersMap;
            super.changeCursor(itemsList);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_image_view, parent, false);
            assert convertView != null;

            ViewHolder holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.parent = (ViewGroup) convertView.findViewById(R.id.parent);

            convertView.setTag(holder);

            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, String item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            String i = getItem(position);

            if (i == null)
                return convertView;

            Integer level = getLevel(item);
            if (level != null){
                holder.image.setImageDrawable(null);
                holder.image.getBackground().setLevel(level);
            }else{
                int drawableResourceId = getContext().getResources().getIdentifier(i, "drawable", getContext().getPackageName());
                holder.image.setImageResource(drawableResourceId == 0 ? R.drawable.categories_placeholder : drawableResourceId);
                holder.image.getBackground().setLevel(0);
            }

            if (selected == position) {
                convertView.setActivated(true);
            } else {
                convertView.setActivated(false);
            }

            return convertView;
        }

        public void itemClicked(int position, StickyGridHeadersGridView parent, boolean smoothScroll) {
            if (smoothScroll)
                parent.smoothScrollToPosition(translatePosition(position));
            else
                parent.setSelection(translatePosition(position));

            selected = position;
            notifyDataSetChanged();
        }

        public String getSelectedItem() {
            return selected == -1 ? null : getItem(selected);
        }

        private String getHeader(int position) {
            if (imageHeadersMap == null)
                return null;

            int i = 0;
            for (String header : imageHeadersMap.keySet()) {
                if (i == position)
                    return header;
                i++;
            }
            return null;
        }

        @Override
        public int getCountForHeader(int position) {
            if (imageHeadersMap == null)
                return 0;

            int i = 0;
            for (Integer count : imageHeadersMap.values()) {
                if (i == position)
                    return count;
                i++;
            }
            return 0;
        }

        @Override
        public int getNumHeaders() {
            if (imageHeadersMap == null)
                return 0;
            return imageHeadersMap.size();
        }

        @Override
        public View getHeaderView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = newHeaderView(position, viewGroup);
            }
            bindHeaderView(position, view);
            return view;
        }

        private View newHeaderView(int position, ViewGroup viewGroup) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.category_group_header, viewGroup, false);
            HeaderViewHolder holder = new HeaderViewHolder();
            holder.label = (TextView) view.findViewById(R.id.label);
            view.setTag(holder);
            return view;
        }


        private void bindHeaderView(int position, View view) {
            HeaderViewHolder holder = (HeaderViewHolder) view.getTag();
            String header = getHeader(position);
            holder.label.setText(header);
        }

        private class ViewHolder {
            ImageView image;
            ViewGroup parent;
        }

        private class HeaderViewHolder {
            TextView label;
        }

        private static Integer getLevel(String str){
            try{
                return Integer.parseInt(str);
            } catch(NumberFormatException nfe){
                return null;
            }
        }
    }

    public static void show(FragmentActivity activity, CategoryModel model, BaseAddCategoryCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, CategoriesDialog_.builder().model(model).build()).setCallback(callback);
    }

}
