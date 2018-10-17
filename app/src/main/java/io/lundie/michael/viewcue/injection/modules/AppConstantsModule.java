package io.lundie.michael.viewcue.injection.modules;

import android.app.Application;
import android.util.Log;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lundie.michael.viewcue.utilities.AppConstants;

@Module
public class AppConstantsModule {

    @Provides
    @Singleton
    AppConstants provideAppConstants(Application application) {
        Log.i("AppConstantsModule", "TEST: APPLICATION = " + application);
        return new AppConstants(application);
    }
}
