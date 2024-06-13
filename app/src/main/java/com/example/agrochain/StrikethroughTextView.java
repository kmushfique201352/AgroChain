package com.example.agrochain;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class StrikethroughTextView extends AppCompatTextView {

    public StrikethroughTextView(Context context) {
        super(context);
        init(null);
    }

    public StrikethroughTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public StrikethroughTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.StrikethroughTextView);
            boolean strikethrough = a.getBoolean(R.styleable.StrikethroughTextView_strikethrough, false);
            if (strikethrough) {
                setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            a.recycle();
        }
    }
}
