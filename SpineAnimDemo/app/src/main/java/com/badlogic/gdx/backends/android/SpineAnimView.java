package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SnapshotArray;


/**
 * Created by miaojun on 2019/2/20.
 */
public class SpineAnimView extends View implements AndroidApplicationBase{
    private Activity mActivity;
    protected AndroidGraphics graphics;
    protected AndroidInput input;
    protected AndroidAudio audio;
    protected AndroidFiles files;
    protected AndroidNet net;
    protected AndroidClipboard clipboard;
    protected ApplicationListener listener;
    public Handler handler;
    protected boolean firstResume = true;
    protected final Array<Runnable> runnables = new Array<Runnable>();
    protected final Array<Runnable> executedRunnables = new Array<Runnable>();
    protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(LifecycleListener.class);
    private final Array<AndroidEventListener> androidEventListeners = new Array<AndroidEventListener>();
    protected int logLevel = LOG_INFO;
    protected ApplicationLogger applicationLogger;
    protected boolean useImmersiveMode = false;
    protected boolean hideStatusBar = false;
    private int wasFocusChanged = -1;
    private boolean isWaitingForAudio = false;

    public SpineAnimView(Context context) {
        super(context);
    }

    public SpineAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SpineAnimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setActivity(Activity activity){
        mActivity = activity;
    }

    /** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
     *
     * @param listener the {@link ApplicationListener} implementing the program logic **/
    public void initialize (ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(listener, config);
    }

    /** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. You can configure other aspects of the application with the rest of the fields in the
     * {@link AndroidApplicationConfiguration} instance.
     *
     * @param listener the {@link ApplicationListener} implementing the program logic
     * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
     *           etc.). */
    public void initialize (ApplicationListener listener, AndroidApplicationConfiguration config) {
        init(listener, config, false);
    }

    /** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
     * <p>
     * Note: you have to add the returned view to your layout!
     *
     * @param listener the {@link ApplicationListener} implementing the program logic
     * @return the GLSurfaceView of the application */
    public View initializeForView (ApplicationListener listener) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        return initializeForView(listener, config);
    }

    /** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. You can configure other aspects of the application with the rest of the fields in the
     * {@link AndroidApplicationConfiguration} instance.
     * <p>
     * Note: you have to add the returned view to your layout!
     *
     * @param listener the {@link ApplicationListener} implementing the program logic
     * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
     *           etc.).
     * @return the GLSurfaceView of the application */
    public View initializeForView (ApplicationListener listener, AndroidApplicationConfiguration config) {
        init(listener, config, true);
        return graphics.getView();
    }

    private void init(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView){
        if (this.getVersion() < MINIMUM_SDK) {
            throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
        }
        setApplicationLogger(new AndroidApplicationLogger());
        graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
                : config.resolutionStrategy);
        input = AndroidInputFactory.newAndroidInput(this, mActivity, graphics.view, config);
        audio = new AndroidAudio(mActivity, config);
        mActivity.getFilesDir(); // workaround for Android bug #10515463
        files = new AndroidFiles(mActivity.getAssets(), mActivity.getFilesDir().getAbsolutePath());
        net = new AndroidNet(this);
        this.listener = listener;
        this.handler = new Handler();
        this.clipboard = new AndroidClipboard(mActivity);

        // Add a specialized audio lifecycle listener
        addLifecycleListener(new LifecycleListener() {

            @Override
            public void resume () {
                // No need to resume audio here
            }

            @Override
            public void pause () {
                audio.pause();
            }

            @Override
            public void dispose () {
                audio.dispose();
            }
        });

        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();

        // detect an already connected bluetooth keyboardAvailable
        if (getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
            this.getInput().keyboardAvailable = true;
    }

    @Override
    public Array<Runnable> getRunnables() {
        return runnables;
    }

    @Override
    public Array<Runnable> getExecutedRunnables() {
        return executedRunnables;
    }

    @Override
    public void runOnUiThread(Runnable runnable) {

    }

    @Override
    public void startActivity(Intent intent) {

    }

    @Override
    public ApplicationListener getApplicationListener() {
        return listener;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public AndroidInput getInput() {
        return input;
    }

    @Override
    public Files getFiles() {
        return files;
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public void log(String tag, String message) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
    }

    @Override
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public void setApplicationLogger(ApplicationLogger applicationLogger) {
        this.applicationLogger = applicationLogger;
    }

    @Override
    public ApplicationLogger getApplicationLogger() {
        return applicationLogger;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.Android;
    }

    @Override
    public int getVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    @Override
    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize();
    }

    @Override
    public Preferences getPreferences(String name) {
        return new AndroidPreferences(getContext().getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    @Override
    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public void postRunnable(Runnable runnable) {
        synchronized (runnables) {
            runnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
    }

    @Override
    public void exit() {
        handler.post(new Runnable() {
            @Override
            public void run () {
                ((ViewGroup)getParent()).removeView(SpineAnimView.this);
            }
        });
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.removeValue(listener, true);
        }
    }

    @Override
    public SnapshotArray<LifecycleListener> getLifecycleListeners() {
        return lifecycleListeners;
    }

    @Override
    public Window getApplicationWindow() {
        return mActivity.getWindow();
    }

    @TargetApi(19)
    @Override
    public WindowManager getWindowManager() {
        return mActivity.getWindowManager();
    }

    @Override
    public void useImmersiveMode(boolean use) {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        graphics.clearManagedCaches();
        graphics.destroy();
    }
}
