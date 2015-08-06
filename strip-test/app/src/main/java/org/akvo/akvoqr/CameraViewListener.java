package org.akvo.akvoqr;

import android.graphics.Bitmap;

/**
 * Created by linda on 6/26/15.
 */
public interface CameraViewListener {

    void getMessage(int what);

    void sendData(byte[] data, int format, int width, int height);

    void setBitmap(Bitmap bitmap);

}