package com.kaching123.tcr.util;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by BemaCarl on 27/12/2016.
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
        mKeyboardView.setKeyboard(mKeyboard);
        // Do not show the preview balloons
        mKeyboardView.setPreviewEnabled(false);
        // Install the key handler
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        /* ************************************  Routine to use the customized keyboard  */

        editTextKeyboard.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(editTextKeyboard.hasFocus()) {
                    openBemaKeyboard();
                    closeSoftKeyboard();
                }else {
                    closeBemaKeyboard();
                }
            }

        });

        editTextKeyboard.requestFocus();

    }



    public void openBemaKeyboard() {
        mKeyboardView.setVisibility(View.VISIBLE);
    }

    public void closeBemaKeyboard() {
        mKeyboardView.setVisibility(View.INVISIBLE);
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
                    boolean hid = hideSoftKeyboard();
                    if(hid) {
                        forceCloseKeyboard(50);
                    }
                }catch(Exception e){
                    e.printStackTrace();
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

            int ja_tem_ponto = editTextKeyboard.getText().toString().indexOf('.');

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
                    if(ja_tem_ponto == -1) {
                        if(editTextKeyboard.getText().equals("-")) {
                            editTextKeyboard.setText("-0.");
                        }else {
                            sales_price_start   = editTextKeyboard.getSelectionStart();
                            sales_price_end     = editTextKeyboard.getSelectionEnd();
                            sales_price_length  = editTextKeyboard.length();
                            deleteText(sales_price_start, sales_price_end, sales_price_length, false);
                            editTextKeyboard.getText().insert(sales_price_end, String.valueOf('.'));
                        }
                        editTextKeyboard.setSelection(editTextKeyboard.getText().length());
                    }
                    break;
                case KeyEvent.KEYCODE_DEL :
                    if(sales_price_start>0 && sales_price_end>0 && sales_price_start <= sales_price_length) {
                        deleteText(sales_price_start, sales_price_end, sales_price_length, true);
                    }
                    break;
                case 261 :
                    enterBind();
                    break;
                case KeyEvent.KEYCODE_ENTER :
                    enterBind();
                    break;
            }

            // Two decimals after dot
            if(ja_tem_ponto != -1) {
                String[] salePrice = editTextKeyboard.getText().toString().split("\\.");
                if(salePrice.length > 1) {
                    if (salePrice[1].length() > 2) {
                        String newSalePrice = editTextKeyboard.getText().toString().substring(0, sales_price_length);
                        editTextKeyboard.setText(newSalePrice);
                        editTextKeyboard.setSelection(sales_price_length);
                    }
                }
            }


        }


        private void enterBind(){
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
        }


        private void deleteText(int sales_price_start, int sales_price_end, int sales_price_length, boolean delOne){
            int delOne_start = sales_price_start;
            if(delOne) delOne_start = sales_price_start - 1;
            String part1 = editTextKeyboard.getText().toString().substring(0, delOne_start);
            String part2 = editTextKeyboard.getText().toString().substring(sales_price_end, sales_price_length);
            editTextKeyboard.setText(part1 + part2);
            int len = editTextKeyboard.getText().length();
            int pos = sales_price_end;
            if(sales_price_end > 1) {
                pos = sales_price_end - 1;
            }
            if(sales_price_start < sales_price_length) {
                editTextKeyboard.setSelection(pos - (sales_price_end - sales_price_start));
            }else{
                if(pos > len) {
                    editTextKeyboard.setSelection(len);
                }else{
                    editTextKeyboard.setSelection(pos);
                }
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
