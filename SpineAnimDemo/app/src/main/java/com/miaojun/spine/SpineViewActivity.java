package com.miaojun.spine;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.SpineAnimView;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.miaojun.spine.mode.SpineBoy;

/**
 * Created by miaojun on 2019/2/20.
 */
public class SpineViewActivity extends Activity {

    static {
        GdxNativesLoader.load();
    }
    private FrameLayout parent_layout;

    private SpineBoy spineBoy;
    private View mSpineView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spine_view);

        parent_layout = findViewById(R.id.parent_layout);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = config.g = config.b = config.a = 8;

        spineBoy = new SpineBoy();
        SpineAnimView spineAnimView = new SpineAnimView(this);
        spineAnimView.setActivity(this);

        mSpineView = spineAnimView.initializeForView(spineBoy, config);

        if (mSpineView instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) mSpineView;
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderOnTop(true);
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(450,450);
        mSpineView.setLayoutParams(layoutParams);
        parent_layout.addView(mSpineView);
    }
}
