package com.example.glucometric1.custom_textview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class PoppinsThinTextView extends AppCompatTextView {
    public PoppinsThinTextView(@NonNull Context context) {
        super(context);
        setFontsTextView();
    }

    public PoppinsThinTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFontsTextView();
    }

    public PoppinsThinTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFontsTextView();
    }

    private void setFontsTextView() {
        Typeface typeface = Utils.getPoppinsThinTypeface(getContext());
        setTypeface(typeface);
    }
}
