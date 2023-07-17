package com.example.glucometric1.custom_textview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class PoppinsBlackTextView extends AppCompatTextView {
    public PoppinsBlackTextView(@NonNull Context context) {
        super(context);
        setFontsTextView();
    }

    public PoppinsBlackTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFontsTextView();
    }

    public PoppinsBlackTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFontsTextView();
    }

    private void setFontsTextView() {
        Typeface typeface = Utils.getPoppinsBlackTypeface(getContext());
        setTypeface(typeface);
    }
}