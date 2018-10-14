package io.lundie.michael.viewcue.injection.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Coding tutorial used from:
 * https://android.jlelse.eu/dagger-2-the-simplest-approach-3e23502c4cab
 * https://proandroiddev.com/dagger-2-component-builder-1f2b91237856
 */
@Module
public class SharedPreferencesModule {

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }
}
