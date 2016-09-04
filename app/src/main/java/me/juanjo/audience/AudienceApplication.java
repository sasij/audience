package me.juanjo.audience;

import android.app.Application;

import com.karumi.dexter.Dexter;

/**
 * Created with ♥
 *
 * @author Juanjo
 */
public class AudienceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initializeDexter();
    }

    private void initializeDexter() {
        Dexter.initialize(this);
    }


}
