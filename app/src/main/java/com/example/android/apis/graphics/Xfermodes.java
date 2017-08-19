/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.os.Bundle;
import android.view.View;

public class Xfermodes extends GraphicsActivity {

    // create a bitmap with a circle, used for the "dst" image
    static Bitmap makeDst(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(0xFFFFCC44);
        c.drawOval(new RectF(0, 0, w * 3 / 4, h * 3 / 4), p);
        return bm;
    }

    // create a bitmap with a rect, used for the "src" image
    static Bitmap makeSrc(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(0xFF66AAFF);
        c.drawRect(w / 3, h / 3, w * 19 / 20, h * 19 / 20, p);
        return bm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    private static class SampleView extends View {
        private static final int W = 100;
        private static final int H = 100;
        private static final int ROW_MAX = 4;   // number of samples per row

        private Bitmap mSrcB;
        private Bitmap mDstB;
        private Shader mBG;     // background checker-board pattern

        private static final Xfermode[] sModes = {
                new PorterDuffXfermode(PorterDuff.Mode.CLEAR), // 绘制不提交在画布上
                new PorterDuffXfermode(PorterDuff.Mode.SRC), // 只显示源图像,即后画在画布上的图像
                new PorterDuffXfermode(PorterDuff.Mode.DST), // 只显示目标图像，即已在画布上的初始图像
                new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER), // 正常绘制显示，即后绘制的叠加在原来绘制的图上

                new PorterDuffXfermode(PorterDuff.Mode.DST_OVER),// 上下两层都显示但是下层(DST)居上显示
                new PorterDuffXfermode(PorterDuff.Mode.SRC_IN), // 取两层绘制的交集且只显示上层(SRC)
                new PorterDuffXfermode(PorterDuff.Mode.DST_IN), // 取两层绘制的交集且只显示下层(DST)
                new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT), // 取两层绘制的不相交的部分且只显示上层(SRC)

                new PorterDuffXfermode(PorterDuff.Mode.DST_OUT), // 取两层绘制的不相交的部分且只显示下层(DST)
                new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP), // 两层相交，取下层(DST)的非相交部分和上层(SRC)的相交部分
                new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP), // 两层相交，取上层(SRC)的非相交部分和下层(DST)的相交部分
                new PorterDuffXfermode(PorterDuff.Mode.XOR), // 挖去两图层相交的部分

                new PorterDuffXfermode(PorterDuff.Mode.DARKEN), // 显示两图层全部区域且加深交集部分的颜色
                new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN), // 显示两图层全部区域且点亮交集部分的颜色
                new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY), // 显示两图层相交部分且加深该部分的颜色
                new PorterDuffXfermode(PorterDuff.Mode.SCREEN) // 显示两图层全部区域且将该部分颜色变为透明色
        };

        private static final String[] sLabels = {
                "Clear", "Src", "Dst", "SrcOver",
                "DstOver", "SrcIn", "DstIn", "SrcOut",
                "DstOut", "SrcATop", "DstATop", "Xor",
                "Darken", "Lighten", "Multiply", "Screen"
        };

        public SampleView(Context context) {
            super(context);

            mSrcB = makeSrc(W, H);
            mDstB = makeDst(W, H);

            // make a checkerboard pattern
            Bitmap bm = Bitmap.createBitmap(new int[]{0xFFFFFFFF, 0xFFCCCCCC,
                            0xFFCCCCCC, 0xFFFFFFFF}, 2, 2,
                    Bitmap.Config.RGB_565);
            // 使用重复模式进行着色
            mBG = new BitmapShader(bm,
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT);
            // 对背景进行放大
            Matrix m = new Matrix();
            m.setScale(6, 6);
            mBG.setLocalMatrix(m);
            // 关闭硬件加速 http://blog.csdn.net/u010335298/article/details/51983420
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            Paint labelP = new Paint(Paint.ANTI_ALIAS_FLAG);
            labelP.setTextAlign(Paint.Align.CENTER);

            Paint paint = new Paint();
            paint.setFilterBitmap(false);

            canvas.translate(15, 35);

            int x = 0;
            int y = 0;
            for (int i = 0; i < sModes.length; i++) {
                // draw the border
                paint.setStyle(Paint.Style.STROKE);
                paint.setShader(null);
                canvas.drawRect(x - 0.5f, y - 0.5f,
                        x + W + 0.5f, y + H + 0.5f, paint);

                // draw the checker-board pattern 绘制棋盘图案
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(mBG);
                canvas.drawRect(x, y, x + W, y + H, paint);

                // draw the src/dst example into our offscreen bitmap offscreen 画面以外的
                int sc = canvas.saveLayer(x, y, x + W, y + H, null,
                        Canvas.MATRIX_SAVE_FLAG |
                                Canvas.CLIP_SAVE_FLAG |
                                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                                Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                                Canvas.CLIP_TO_LAYER_SAVE_FLAG);
                canvas.translate(x, y);
                canvas.drawBitmap(mDstB, 0, 0, paint);
                paint.setXfermode(sModes[i]);
                canvas.drawBitmap(mSrcB, 0, 0, paint);
                paint.setXfermode(null);
                canvas.restoreToCount(sc);

                // draw the label
                canvas.drawText(sLabels[i],
                        x + W / 2, y - labelP.getTextSize() / 2, labelP);

                x += W + 10;

                // wrap around when we've drawn enough for one row
                if ((i % ROW_MAX) == ROW_MAX - 1) {
                    x = 0;
                    y += H + 30;
                }
            }
        }
    }
}

