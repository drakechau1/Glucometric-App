package com.example.glucometric1.custom_textview;

import android.content.Context;
import android.graphics.Typeface;

public class Utils {
    private static Typeface poppinsBlackTypeface, poppinsBoldTypeface,
    poppinsLightTypeface, poppinsMediumTypeface,
    poppinsRegularTypeface, poppinsThinTypeface,
    pacificoTypeface;

    public static Typeface getPoppinsBlackTypeface(Context context) {
        if (poppinsBlackTypeface == null) {
            poppinsBlackTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Poppins-Black.otf");
        }
        return poppinsBlackTypeface;
    }

    public static Typeface getPoppinsBoldTypeface(Context context) {
        if (poppinsBoldTypeface == null) {
            poppinsBoldTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Poppins-SemiBold.otf");
        }
        return poppinsBoldTypeface;
    }

    public static Typeface getPoppinsLightTypeface(Context context) {
        if (poppinsLightTypeface == null) {
            poppinsLightTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Poppins-Light.otf");
        }
        return poppinsLightTypeface;
    }

    public static Typeface getPoppinsMediumTypeface(Context context) {
        if (poppinsMediumTypeface == null) {
            poppinsMediumTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Poppins-Medium.otf");
        }
        return poppinsMediumTypeface;
    }

    public static Typeface getPoppinsRegularTypeface(Context context) {
        if (poppinsRegularTypeface == null) {
            poppinsRegularTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Poppins-Regular.otf");
        }
        return poppinsRegularTypeface;
    }

    public static Typeface getPoppinsThinTypeface(Context context) {
        if (poppinsThinTypeface == null) {
            poppinsThinTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Poppins-Thin.otf");
        }
        return poppinsThinTypeface;
    }

    public static Typeface getPacificoTypeface(Context context) {
        if (pacificoTypeface == null) {
            pacificoTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Pacifico.ttf");
        }
        return pacificoTypeface;
    }
}
