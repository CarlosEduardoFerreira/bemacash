package com.kaching123.tcr.fragment.prepaid.LongDistance;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;

import java.util.ArrayList;

/**
 * Created by teli.yin on 10/29/2014.
 */
@EFragment
public class PrepaidLongDistanceCountryCharacterGridFragment extends PrepaidLongDistanceBaseBodyFragment {

    private ArrayList<String> country_characters;

    private int selectCharacterPosition;
    @ViewById
    protected GridView countryCharacterGrid;

    private CountryCharacterFragmentCallback callback;

    private ArrayList<String> inisAvailable;

    private GridAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.prepaid_long_distance_country_character_grid_fragment, container, false);
        countryCharacterGrid = (GridView) view.findViewById(R.id.country_character_grid);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    public void init() {
        adapter = new GridAdapter();
        inisAvailable = new ArrayList<String>();
        TypedArray ar = getActivity().getResources().obtainTypedArray(R.array.coutry_ini_items);
        int len = ar.length();
        country_characters = new ArrayList<String>();
        for (int i = 0; i < len; i++)
            country_characters.add(ar.getString(i));
        ar.recycle();
        countryCharacterGrid.setAdapter(adapter);
    }

    public void setCallback(CountryCharacterFragmentCallback callback) {
        this.callback = callback;
    }

    @Click
    void submit() {

    }


    private class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return country_characters.size();
        }

        @Override
        public Object getItem(int position) {
            return country_characters.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            MyViewHolder mViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.country_character, parent, false);
                mViewHolder = new MyViewHolder();
                mViewHolder.countryCharacter = (TextView) convertView.findViewById(R.id.character);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (MyViewHolder) convertView.getTag();
            }

            mViewHolder.countryCharacter.setText(country_characters.get(position));
            if (selectCharacterPosition != 0 && selectCharacterPosition == position + 1) {
                selectedCharacterText(mViewHolder);
            } else {
                initCharacterText(mViewHolder);
            }

            if ((isCharacterAvailable(country_characters.get(position)))) {
                mViewHolder.countryCharacter.setEnabled(true);
            } else {
                mViewHolder.countryCharacter.setEnabled(false);
                mViewHolder.countryCharacter.setTextColor(getResources().getColor(R.color.light_gray));
            }

            mViewHolder.countryCharacter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectCharacterPosition = position + 1;
                    notifyDataSetChanged();
                    String countryIni = country_characters.get(position);
                    callback.selectCountryInit(countryIni);
                }
            });
            return convertView;
        }
    }

    private void initCharacterText(MyViewHolder mViewHolder) {
        mViewHolder.countryCharacter.setBackgroundResource(Color.TRANSPARENT);
        mViewHolder.countryCharacter.setTextColor(getResources().getColor(R.color.text_grey));
        mViewHolder.countryCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
    }

    private void selectedCharacterText(MyViewHolder mViewHolder) {
        mViewHolder.countryCharacter.setBackgroundResource(R.drawable.country_character);
        mViewHolder.countryCharacter.setTextColor(getResources().getColor(R.color.prepaid_dialog_white));
        mViewHolder.countryCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
    }

    private boolean isCharacterAvailable(String countryCharacter) {
        if (inisAvailable.size() == 0)
            return false;
        for (int i = 0; i < inisAvailable.size(); i++) {
            if (countryCharacter.equalsIgnoreCase(inisAvailable.get(i))) {
                return true;
            }
        }
        return false;

    }

    public void setCountryInis(String[] inis) {
        for (String s : inis)
            inisAvailable.add(s);

        adapter.notifyDataSetChanged();
    }

    class MyViewHolder {
        TextView countryCharacter;
    }

    public interface CountryCharacterFragmentCallback {
        void selectCountryInit(String countryIni);
    }

    public void clearSelectedCharacter() {
        selectCharacterPosition = 0;
        adapter.notifyDataSetChanged();
    }
}
