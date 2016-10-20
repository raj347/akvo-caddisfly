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

package org.akvo.caddisfly.sensor.colorimetry.strip.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;

/**
 * Created by linda on 11/5/15
 */
public class PercentageMeterView extends View {

    private static final int NUMBER_OF_BARS = 6;
    private final Paint paint;
    private float percentage = Float.NaN;
    private int green = 0;
    private int orange = 0;
    private int red = 0;

    public PercentageMeterView(Context context) {
        this(context, null);
    }

    public PercentageMeterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentageMeterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        //if (!isInEditMode()) {
        green = ContextCompat.getColor(context, R.color.jungle_green);
        orange = ContextCompat.getColor(context, R.color.orange);
        red = ContextCompat.getColor(context, R.color.cardinal);
        //}
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (Float.isNaN(percentage)) {
            return;
        }

        double number = (100 - percentage) * NUMBER_OF_BARS * 0.01;

        canvas.save();

        // Set each dot's diameter to half the canvas height
        canvas.translate(canvas.getWidth() / 2f - ((canvas.getHeight() * 0.5f + 0.2f) * 2.5f), 0);

        for (double i = 0; i < NUMBER_OF_BARS; i++) {

            // Reset color
            paint.setColor(Color.LTGRAY);

            if (number >= 0) {
                if (i < 2) {
                    // Red if number is lower than i + 1
                    // 2 red dots if number < 1 or 1 red dot if number > 1
                    if (number <= i + 1) {
                        paint.setColor(red);
                    }
                } else if (i < 4) {
                    // Orange if number between 1 and 4
                    // if number == 1.5 then 1 red followed by orange dot
                    // if number == 2.5 then 2 orange dots
                    // if number == 3.5 then 1 orange dot
                    if (number >= i - 1 && number <= i + 2) {
                        paint.setColor(orange);
                    }
                } else {
                    if (number > i) {
                        // Green if number larger than 3 but yellow if number exceeds optimum
                        if (number > NUMBER_OF_BARS) {
                            paint.setColor(Color.YELLOW);
                        } else {
                            paint.setColor(green);
                        }
                    }
                }
            }

            canvas.drawCircle(0, canvas.getHeight() * 0.5f, canvas.getHeight() * 0.25f, paint);

            // Position next circle
            canvas.translate(canvas.getHeight() * 0.5f + 0.2f, 0);
        }

        canvas.restore();

        super.onDraw(canvas);
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
        invalidate();
    }
}
