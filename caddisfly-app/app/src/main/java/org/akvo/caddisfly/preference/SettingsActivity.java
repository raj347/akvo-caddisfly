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

package org.akvo.caddisfly.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.widget.ScrollView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class SettingsActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    private ScrollView mScrollView;
    private int mScrollPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        setupActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext())
                .registerOnSharedPreferenceChangeListener(this);

        if (AppPreferences.isDiagnosticMode()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        }
    }

    private void setupActivity() {

        setTitle(R.string.settings);

        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutContent, new GeneralPreferenceFragment())
                .commit();

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutContent2, new OtherPreferenceFragment())
                .commit();

        if (AppPreferences.isDiagnosticMode()) {
            getFragmentManager().beginTransaction()
                    .add(R.id.layoutContent3, new DiagnosticPreferenceFragment())
                    .commit();
        }

        if (AppPreferences.isDiagnosticMode()) {
            getFragmentManager().beginTransaction()
                    .add(R.id.layoutContent4, new DiagnosticUserPreferenceFragment())
                    .commit();
        }

        mScrollView = (ScrollView) findViewById(R.id.scrollViewSettings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        try {
            setSupportActionBar(toolbar);
        } catch (Throwable t) {
            //Ignore crash in Samsung
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.settings);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (getApplicationContext().getString(R.string.languageKey).equals(s)) {
            finish();
        }

        if (getApplicationContext().getString(R.string.selectedThemeKey).equals(s)) {
            finish();
        }

    }

    @Override
    public void onPause() {
        int scrollbarPosition = mScrollView.getScrollY();

        PreferencesUtil.setInt(this, "settingsScrollPosition", scrollbarPosition);

        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        mScrollPosition = PreferencesUtil.getInt(this, "settingsScrollPosition", 0);

        mScrollView.post(new Runnable() {
            public void run() {
                mScrollView.scrollTo(0, mScrollPosition);
            }
        });
    }
}
