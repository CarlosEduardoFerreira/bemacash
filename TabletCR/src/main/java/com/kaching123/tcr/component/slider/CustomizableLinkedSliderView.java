package com.kaching123.tcr.component.slider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;

public class CustomizableLinkedSliderView extends View {

    private static final Paint paint = new Paint();

    private final float fontSize = 12f;
    private int leftSelection = 0;
    private int rightSelection = 0;
    private Bitmap backgroundOFF;
    private Bitmap backgroundON;
    private Bitmap clickableView;
    private String minValueText = "";
    private String maxValueText = "";
    private int[] values;
    private Integer min;
    private Integer max;

    private ISliderEvents listener;

    public CustomizableLinkedSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float scale = getContext().getResources().getDisplayMetrics().density;
        paint.setTextSize((fontSize * scale) + 0.5f);

        if (backgroundOFF == null) backgroundOFF = BitmapFactory.decodeResource(getResources(), R.drawable.sliderbackgroundoff);
        if (backgroundON == null) backgroundON = BitmapFactory.decodeResource(getResources(), R.drawable.sliderbackgroundon);
        if (clickableView == null) clickableView = BitmapFactory.decodeResource(getResources(), R.drawable.sliderbutton);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAntiAlias(true);
        paint.setColor(context.getResources().getColor(android.R.color.black));
    }

    public CustomizableLinkedSliderView setListener(ISliderEvents listener) {
        this.listener = listener;
        return this;
    }

    public void setMinValue(int value) {
        min = value;
        hasInitCoords = false;
    }

    public void setMaxValue(int value) {
        max = value;
        hasInitCoords = false;
    }

    public void setValues(int[] values) {
        this.values = values;
        leftSelection = values[0];
        rightSelection = values[values.length - 1];
    }

    public void reset() {
        leftSelection = values[0];
        rightSelection = values[values.length - 1];
        initGUI(getWidth(), getHeight());
        invalidate();
    }

    public int getMinValue() {
        return values[0];
    }

    public int getMaxValue() {
        return values[values.length - 1];
    }

    public int getLeftSelection() {
        return leftSelection;
    }

    public int getRightSelection() {
        return rightSelection;
    }

    public void setMinMaxValueText(String min, String max) {
        minValueText = min;
        maxValueText = max;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        return 310;
    }

    private int measureHeight(int measureSpec) {
        return (int) (paint.descent() * 20);
    }

    private RectF backgroundRect = new RectF();
    private RectF leftButtonRect = new RectF();
    private RectF rightButtonRect = new RectF();

    private float buttonWidth, buttonHeight;
    private float textY;

    private void initGUI(int width, int height) {
        float padding = (float) height / 7;

        float textHeight = padding * 3;

        backgroundRect.left = 0;
        backgroundRect.right = width - ((padding * 15) / 4);
        backgroundRect.top = padding;
        backgroundRect.bottom = height - textHeight;

        buttonHeight = height - textHeight + padding;

        textY = buttonHeight + (padding * 3) / 2;

        float buttonRatio = (float) clickableView.getWidth() / (float) clickableView.getHeight();
        buttonWidth = buttonHeight * buttonRatio;

        setButtonRect(leftButtonRect, backgroundRect.left, buttonWidth, buttonHeight);
        setButtonRect(rightButtonRect, backgroundRect.right - buttonWidth, buttonWidth, buttonHeight);

        float pixelrange = (backgroundRect.right - buttonWidth) - backgroundRect.left;

        if (min != null) {
            int index = -1;

            for (int i = 0; i < values.length; i++) {

                if (values[i] == min) {
                    leftSelection = min;
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                float pixels = (((float) index) / values.length) * pixelrange;
                setButtonRect(leftButtonRect, pixels, buttonWidth, buttonHeight);
            }
        }

        if (max != null) {
            int index = -1;

            for (int i = 0; i < values.length; i++) {

                if (values[i] == max) {
                    rightSelection = max;
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                float pixels = (((float) index) / values.length) * pixelrange;
                setButtonRect(rightButtonRect, pixels, buttonWidth, buttonHeight);
            }
        }
    }

    private void setButtonRect(RectF rect, float x, float buttonwidth, float buttonheight) {
        rect.left = x;
        rect.right = rect.left + buttonwidth;
        rect.top = 0;
        rect.bottom = buttonheight;
    }

    private boolean hasInitCoords = false;

    public void onDraw(Canvas canvas) {

        if (!hasInitCoords) {
            initGUI(getWidth(), getHeight());
            hasInitCoords = true;
        }

        canvas.drawBitmap(backgroundOFF, null, backgroundRect, paint);

        canvas.save();
        canvas.clipRect(leftButtonRect.centerX(), 0, rightButtonRect.centerX(), getHeight());
        canvas.drawBitmap(backgroundON, null, backgroundRect, paint);
        canvas.restore();

        canvas.drawBitmap(clickableView, null, leftButtonRect, paint);
        canvas.drawBitmap(clickableView, null, rightButtonRect, paint);
    }

    public void setLeftValue() {
        float minx = backgroundRect.left;
        float maxx = backgroundRect.right - buttonWidth;

        float pixelrange = maxx - minx;
        float ratio = (leftButtonRect.left - backgroundRect.left) / pixelrange;

        int index = (int) (values.length * ratio);

        leftSelection = values[index];

    }

    public void setRightValue() {
        float minx = backgroundRect.left;
        float maxx = backgroundRect.right - buttonWidth;

        float pixelrange = maxx - minx;
        float ratio = (rightButtonRect.left - backgroundRect.left) / pixelrange;

        int index = (int) (values.length * ratio);

        if (index > values.length - 1) {
            index = values.length - 1;
        }

        rightSelection = values[index];
    }

    private boolean dragging;
    private boolean draggingLeft = false;

    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            if (leftButtonRect.contains(x, y)) {
                dragging = true;
                draggingLeft = true;
            } else if (rightButtonRect.contains(x, y)) {
                dragging = true;
                draggingLeft = false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            dragging = false;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

            if (dragging) {
                float x = event.getX();

                if (x < backgroundRect.left + buttonWidth / 2) {
                    x = backgroundRect.left + buttonWidth / 2;
                }

                if (x > backgroundRect.right - buttonWidth / 2) {
                    x = backgroundRect.right - buttonWidth / 2;
                }
                if (draggingLeft) {
                    if (x + buttonWidth / 2 > rightButtonRect.left) {
                        x = rightButtonRect.left - buttonWidth / 2;
                    }
                    setButtonRect(leftButtonRect, x - buttonWidth / 2, buttonWidth, buttonHeight);
                    setLeftValue();
                    listener.onLeftChanged(leftSelection);
                } else {
                    if (x - buttonWidth / 2 < leftButtonRect.right) {
                        x = leftButtonRect.right + buttonWidth / 2;
                    }
                    Logger.d("set R4 as " + x);
                    setButtonRect(rightButtonRect, x - buttonWidth / 2, buttonWidth, buttonHeight);
                    setRightValue();
                    listener.onRightChanged(rightSelection);
                }
                invalidate();
            }
        }
        return true;
    }

    public CustomizableLinkedSliderView setLeftSelection(int i) {
        leftSelection = i;
        float x = (float)leftSelection / (float)values.length * (backgroundRect.width()) + buttonWidth / 2;
        if (x + buttonWidth / 2 > rightButtonRect.left) {
            x = rightButtonRect.left - buttonWidth / 2;
        }
        setButtonRect(leftButtonRect, x - buttonWidth / 2, buttonWidth, buttonHeight);

        invalidate();
        return this;
    }

    public CustomizableLinkedSliderView setRightSelection(int i) {
        rightSelection = i;
        float x = (float)rightSelection / (float)values.length * (backgroundRect.width())- buttonWidth / 2;
        if (x - buttonWidth / 2 < leftButtonRect.right) {
            x = leftButtonRect.right + buttonWidth / 2;
        }
        setButtonRect(rightButtonRect, x - buttonWidth / 2, buttonWidth, buttonHeight);

        invalidate();
        return this;
    }

    public static interface ISliderEvents {

        public void onLeftChanged(int to);

        public void onRightChanged(int to);
    }
}