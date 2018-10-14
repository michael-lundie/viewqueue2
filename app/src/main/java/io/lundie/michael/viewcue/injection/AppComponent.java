/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 15:10
 */

package io.lundie.michael.viewcue.injection;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

import dagger.android.AndroidInjectionModule;
import io.lundie.michael.viewcue.App;
import io.lundie.michael.viewcue.injection.modules.AppModule;
import io.lundie.michael.viewcue.injection.modules.FragmentModule;
import io.lundie.michael.viewcue.injection.modules.SharedPreferencesModule;

@Singleton
@Component(modules = {  AndroidInjectionModule.class,
                        FragmentModule.class,
                        SharedPreferencesModule.class,
                        AppModule.class,
                        ActivityBuilder.class  })

public interface AppComponent {

    @Component.Builder
    interface Builder {
        AppComponent build();

        @BindsInstance
        Builder application(Application application);
    }

    void inject(App app);

}
