package com.miaojun.spine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.miaojun.spine.mode.SpineBoy;

/**
 * Created by miaojun on 2019/2/18.
 */
public class SpineBoyActivity extends AndroidApplication {
    private FrameLayout mParentLayout;
    private SpineBoy spineBoy;
    private View mSpineView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spineboy);

        mParentLayout = findViewById(R.id.parent_layout);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = config.g = config.b = config.a = 8;

        spineBoy = new SpineBoy();

        mSpineView = initializeForView(spineBoy, config);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        mSpineView.setLayoutParams(layoutParams);
        mParentLayout.addView(mSpineView);

    }
}
