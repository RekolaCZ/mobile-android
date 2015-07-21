package cz.rekola.app.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * show/keyboard
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {21. 7. 2015}
 **/
public class KeyboardUtils {
    public static final String TAG = KeyboardUtils.class.getName();

    public static void showKeyboard(Context context) {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    public static void showKeyboard(Context context, View focusedView) {
        if (focusedView == null) {
            return;
        }

        focusedView.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(focusedView, InputMethodManager.SHOW_FORCED);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }
}
