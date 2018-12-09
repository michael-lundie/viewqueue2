package io.lundie.michael.viewcue.injection.modules;

import android.app.Application;
import android.util.Log;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lundie.michael.viewcue.utilities.AppConstants;

/**
 * Returns new singleton instance of the AppConstants method
 * (allowing access of constants defined in XML, requiring access via context)
 */
@Module
public class AppConstantsModule {

    @Provides
    @Singleton
    AppConstants provideAppConstants(Application application) {
        return new AppConstants(application);
    }
}
