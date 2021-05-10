
package com.tourcoo.util;

import android.text.InputFilter;
import android.text.Spanned;

public class EditTextNumberInputFilter implements InputFilter{
    private int min, max;

    public EditTextNumberInputFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public EditTextNumberInputFilter(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String stringInput = dest.toString() + source.toString();
            double value;
            if (stringInput.length() == 1 && stringInput.charAt(0) == '-') {
                value = -1;
            } else {
                value = Double.parseDouble(dest.toString() + source.toString());
            }
            if (isInRange(min, max, value)) {
                return null;
            }
        } catch (NumberFormatException nfe) {
            //Do nothing as the number is not valid
        }
        return "";
    }

    private boolean isInRange(int a, int b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

}
