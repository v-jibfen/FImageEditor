package com.github.vfeng.image.frame;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

public interface ECFramePortrait {

    boolean show();

    boolean remove();

    boolean dismiss();

    boolean isShowing();

    RectF getFrame();

    void onFrame(Canvas canvas);

    void registerCallback(IECFrame.Callback callback);

    void unregisterCallback(IECFrame.Callback callback);

    interface Callback {

        <V extends View & IECFrame> void onDismiss(V frameView);

        <V extends View & IECFrame> void onShowing(V frameView);

        <V extends View & IECFrame> boolean onRemove(V frameView);
    }
}
