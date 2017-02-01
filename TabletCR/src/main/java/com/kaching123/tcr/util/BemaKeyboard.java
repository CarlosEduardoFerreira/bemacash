package com.kaching123.tcr.util;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;

/**
 * Created by ferre on 27/12/2016.
 */

public class BemaKeyboard {

    public Keyboard mKeyboard;
    public KeyboardView mKeyboardView;
    public EditText editTextKeyboard;

    int tx = 0;

    public BemaKeyboard(View view, EditText editText) {

        editTextKeyboard = editText;

        /* Routine to use the customized keyboard ************************************ */
        // Create the Keyboard
        mKeyboard = new Keyboard(TcrApplication.get(), R.xml.keyboard_numbers_negative);
        // Lookup the KeyboardView
        mKeyboardView = (KeyboardView) view.getRootView().findViewById(R.id.keyboardview);
        // Attach the keyboard to the view
        Log.i("BemaKeyboard", "mKeyboard: " + mKeyboard);
        Log.i("BemaKeyboard", "mKeyboardView: " + mKeyboardView);
        mKeyboardView.setKeyboard(mKeyboard);
        // Do not show the preview balloons
        mKeyboardView.setPreviewEnabled(false);
        // Install the key handler
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        /* ************************************  Routine to use the customized keyboard  */

        //editTextKeyboard.setImeOptions(EditorInfo.IME_ACTION_NEXT); // to test into ItemCommonInformationFragment

        //salesPrice.setFocusable(false);
        //salesPrice.setFocusableInTouchMode(false);
        editTextKeyboard.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Log.i("BemaCarl", "BemaKeyboard.onFocusChange.editTextKeyboard.hasFocus(): " + editTextKeyboard.hasFocus());
                Log.i("BemaCarl", "BemaKeyboard.onFocusChange.v: " + v);
                Log.i("BemaCarl", "BemaKeyboard.onFocusChange.v.getRootView: " + v.getRootView());
                if(editTextKeyboard.hasFocus()) {
                    Log.i("BemaCarl", "BemaKeyboard.onFocusChange.if(editTextKeyboard.hasFocus()): true");
                    openBemaKeyboard();
                    closeSoftKeyboard();
                }else {
                    Log.i("BemaCarl", "BemaKeyboard.onFocusChange.if(editTextKeyboard.hasFocus()): false");
                    closeBemaKeyboard();
                }
            }

        });

        editTextKeyboard.requestFocus();

    }



    public void openBemaKeyboard() {
        mKeyboardView.setVisibility(View.VISIBLE);
        //mKeyboardView.setEnabled(true);
    }

    public void closeBemaKeyboard() {
        mKeyboardView.setVisibility(View.INVISIBLE);
        //mKeyboardView.setEnabled(false);
    }


    public void closeSoftKeyboard(){
        tx = 10;
        forceCloseKeyboard(tx);
    }

    protected void forceCloseKeyboard(int x){
        tx += x;
        editTextKeyboard.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("BemaCarl", "BemaKeyboard.forceCloseKeyboard.catch.tx: " + tx);

                    boolean hid = hideSoftKeyboard();
                    Log.i("BemaCarl", "BemaKeyboard.forceCloseKeyboard.try.hideSoftKeyboard: " + hid);

                    if(hid) {
                        //((InputMethodManager) TcrApplication.get().getSystemService(INPUT_METHOD_SERVICE))
                         //       .hideSoftInputFromWindow(editTextKeyboard.getApplicationWindowToken(), 0);
                        forceCloseKeyboard(50);
                    }
                }catch(Exception e){
                    Log.i("BemaCarl", "BemaKeyboard.forceCloseKeyboard.catch.tx: " + tx);
                    e.printStackTrace();
                    //forceCloseKeyboard(50);
                }
            }
        }, x);
    }


    protected boolean hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager) TcrApplication.get().getSystemService(INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(editTextKeyboard.getWindowToken(), 0);
    }



    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onKey(int primaryCode, int[] keyCodes) {

            int sales_price_start   = editTextKeyboard.getSelectionStart(); //this is to get the the cursor position
            int sales_price_end     = editTextKeyboard.getSelectionEnd();   //this is to get the the edittext end [ -1 array position ]
            int sales_price_length  = editTextKeyboard.length(); // to set the sales_price on last position

            //Log.d("BemaCarl", "BemaKeyboard.onKey.sales_price_start: "     + sales_price_start);
            //Log.d("BemaCarl", "BemaKeyboard.onKey.sales_price_end: "       + sales_price_end);
            //Log.d("BemaCarl", "BemaKeyboard.onKey.sales_price_length: "    + sales_price_length);
            //sales_price.setSelection(0);

            String str_key = KeyEvent.keyCodeToString(primaryCode);
            //Log.d("BemaCarl", "BemaKeyboard.onKey.primaryCode: " + String.valueOf(primaryCode));
            //Log.d("BemaCarl", "BemaKeyboard.onKey.keyCodeToString: " + str_key);

            switch(primaryCode){
                case KeyEvent.KEYCODE_NUMPAD_1 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('1'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_2 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('2'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_3 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('3'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_4 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('4'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_5 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('5'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_6 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('6'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_7 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('7'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_8 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('8'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_9 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('9'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_0 :
                    editTextKeyboard.getText().insert(sales_price_start, String.valueOf('0'));
                    break;
                case KeyEvent.KEYCODE_MINUS :
                    editTextKeyboard.setText(editTextKeyboard.getText().toString().replace("-", ""));
                    editTextKeyboard.getText().insert(0, String.valueOf('-'));
                    break;
                case KeyEvent.KEYCODE_NUMPAD_DOT :
                    int ja_tem_ponto = editTextKeyboard.getText().toString().indexOf('.');
                    if(ja_tem_ponto == -1) {
                        editTextKeyboard.getText().insert(sales_price_start, String.valueOf('.'));
                    }
                    break;
                case KeyEvent.KEYCODE_DEL :
                    //getting the selected Text
                    if(sales_price_start>0 && sales_price_end>0 && sales_price_start <= sales_price_length) {
                        String part1 = editTextKeyboard.getText().toString().substring(0, sales_price_start-1);
                        String part2 = editTextKeyboard.getText().toString().substring(sales_price_end, sales_price_length);
                        Log.d("BemaCarl", "BemaKeyboard.onKey.KEYCODE_DEL.part1: " + part1);
                        Log.d("BemaCarl", "BemaKeyboard.onKey.KEYCODE_DEL.part2: " + part2);

                        //editTextKeyboard.postDelayed(new Runnable() {
                            //@Override
                            //public void run() {
                        editTextKeyboard.setText(part1 + part2);
                        int len = editTextKeyboard.getText().length();
                        Log.d("BemaCarl", "BemaKeyboard.onKey.KEYCODE_DEL.getText: " + editTextKeyboard.getText());
                        Log.d("BemaCarl", "BemaKeyboard.onKey.KEYCODE_DEL.getText.length: " + len);
                        Log.d("BemaCarl", "BemaKeyboard.onKey.KEYCODE_DEL.sales_price_start: "     + sales_price_start);
                        Log.d("BemaCarl", "BemaKeyboard.onKey.KEYCODE_DEL.sales_price_end: "       + sales_price_end);
                        Log.d("BemaCarl", "BemaKeyboard.onKey.KEYCODE_DEL.sales_price_length: "    + sales_price_length);
                        int pos = sales_price_end - 1;
                        if(sales_price_start < sales_price_length) {
                            editTextKeyboard.setSelection(pos - (sales_price_end - sales_price_start));
                        }else{
                            if(pos > len) {
                                editTextKeyboard.setSelection(len);
                            }else{
                                editTextKeyboard.setSelection(pos);
                            }
                        }

                            //}
                        //}, 200);
                    }
                    break;
                case 261 :
                    editTextKeyboard.setImeOptions(IME_ACTION_NEXT);
                    break;
                case KeyEvent.KEYCODE_ENTER :
                    closeBemaKeyboard();
                    editTextKeyboard.setFocusableInTouchMode(false);
                    editTextKeyboard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editTextKeyboard.setFocusableInTouchMode(true);
                            closeSoftKeyboard();
                            openBemaKeyboard();
                        }
                    });
                    break;
            }
        }

        @Override
        public void onPress(int arg0) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onText(CharSequence text) {
            Log.i("BemaKeyboard", "text: " + text);
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeUp() {
        }
    };


}
