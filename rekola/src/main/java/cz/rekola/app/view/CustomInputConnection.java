package cz.rekola.app.view;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

/**
 * n Android 4.2 (maybe in earlier versions as well) the backspace is not sent
 * as a sendKeyEvent(..., KeyEvent.KEYCODE_DEL) by the standard soft keyboard.
 * Instead, it is sent as deleteSurroundingText(1, 0).
 *
 * http://stackoverflow.com/questions/14560344/android-backspace-in-webview-baseinputconnection/14561345#14561345
 **/
public class CustomInputConnection extends BaseInputConnection {


    public CustomInputConnection(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {

        // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
        if (beforeLength == 1 && afterLength == 0) {
            // backspace
            return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }

        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}