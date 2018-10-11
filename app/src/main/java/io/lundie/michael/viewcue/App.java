/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 15:25
 */

/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 14:29
 */

package io.lundie.michael.viewcue;

import android.app.Activity;
import android.app.Application;
import android.arch.persistence.room.Insert;
import android.content.Context;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.lundie.michael.viewcue.injection.DaggerAppComponent;

public class App extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> androidInjector;

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerAppComponent.builder().application(this).build().inject(this);
        context = getApplicationContext();
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return androidInjector;
    }

}
