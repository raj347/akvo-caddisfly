/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by linda on 8/19/15
 */
public class AssetsManager {

    private static AssetsManager assetsManager;
    private final AssetManager manager;

    private AssetsManager() {
        this.manager = CaddisflyApp.getApp().getApplicationContext().getAssets();
    }

    public static AssetsManager getInstance() {
        if (assetsManager == null)
            assetsManager = new AssetsManager();

        return assetsManager;
    }

    public static Drawable getImage(Context context, String imageName) {

        String path = context.getResources().getString(R.string.instruction_images);
        try {

            String localeLanguage = Locale.getDefault().getLanguage();
            String localePath = path + "-" + localeLanguage + "/" + imageName + ".png";
            if (Arrays.asList(context.getResources().getAssets().list("")).contains(localePath)) {
                path += "-" + localeLanguage;
            }

            InputStream ims = context.getAssets().open(path + "/" + imageName.toLowerCase(Locale.US) + ".png");

            return Drawable.createFromStream(ims, null);

        } catch (IOException ignored) {
        }
        return null;
    }

    public String loadJSONFromAsset(String fileName) {
        String json;
        InputStream is = null;
        try {
            if (manager == null)
                return null;

            is = manager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }finally{
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return json;
    }
}
