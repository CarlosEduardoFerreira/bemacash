package com.kaching123.tcr.util;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by ferre on 27/12/2016.
 */

public class BemaKeyboard {

    public Keyboard mKeyboard;
    public KeyboardView mKeyboardView;
    public EditText editTextKeyboard;


    public BemaKeyboard(Context context, View view) {

        /* Routine to use the customized keyboard ************************************ */
        // Create the Keyboard
        mKeyboard = new Keyboard(context, R.xml.keyboard_numbers_negative);
        // Lookup the KeyboardView
        mKeyboardView = (KeyboardView) view.findViewById(R.id.keyboardview);
        // Attach the keyboard to the view
        Log.i("BemaKeyboard", "mKeyboard: " + mKeyboard);
        Log.i("BemaKeyboard", "mKeyboardView: " + mKeyboardView);
        mKeyboardView.setKeyboard(mKeyboard);
        // Do not show the preview balloons
        mKeyboardView.setPreviewEnabled(false);
        // Install the key handler
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        /* ************************************  Routine to use the customized keyboard  */

    }


    public void openKeyboard(View v) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
    }

    public void closeKeyboard(View v) {
        v.requestFocus();
        ((InputMethodManager) TcrApplication.get().getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
        mKeyboardView.setVisibility(View.INVISIBLE);
        mKeyboardView.setEnabled(false);
    }

    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onKey(int primaryCode, int[] keyCodes) {

            int sales_price_start   = editTextKeyboard.getSelectionStart(); //this is to get the the cursor position
            int sales_price_end     = editTextKeyboard.getSelectionEnd();   //this is to get the the edittext end [ -1 array position ]
            int sales_price_length  = editTextKeyboard.length(); // to set the sales_price on last position

            Log.i("BemaKeyboard", "sales_price_start: "     + sales_price_start);
            Log.i("BemaKeyboard", "sales_price_end: "       + sales_price_end);
            Log.i("BemaKeyboard", "sales_price_length: "    + sales_price_length);
            //sales_price.setSelection(0);

            String str_key = KeyEvent.keyCodeToString(primaryCode);
            Log.i("BemaKeyboard", "primaryCode: " + String.valueOf(primaryCode));
            Log.i("BemaKeyboard", "keyCodeToString: " + str_key);

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
                    if(sales_price_start>0 && sales_price_start <= sales_price_length) {
                        String part1 = editTextKeyboard.getText().toString().substring(0, sales_price_start-1);
                        String part2 = editTextKeyboard.getText().toString().substring(sales_price_end, sales_price_length);
                        Log.i("BemaKeyboard", "part1: " + part1);
                        Log.i("BemaKeyboard", "part2: " + part2);
                        //replacing the selected text with empty String and setting it to EditText
                        editTextKeyboard.setText(part1 + part2);
                        Log.i("BemaKeyboard", "sales_price_start: "     + sales_price_start);
                        Log.i("BemaKeyboard", "sales_price_end: "       + sales_price_end);
                        Log.i("BemaKeyboard", "sales_price_length: "    + sales_price_length);
                        editTextKeyboard.setSelection(sales_price_end-1);
                    }
                    break;
                case KeyEvent.KEYCODE_ENTER :
                    mKeyboardView.setVisibility(View.INVISIBLE);
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
