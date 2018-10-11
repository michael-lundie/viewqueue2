/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 15:10
 */

package io.lundie.michael.viewcue.injection;

import android.app.Activity;
import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

import dagger.Subcomponent;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import io.lundie.michael.viewcue.App;

@Singleton
@Component(modules = {AndroidInjectionModule.class,
                        FragmentModule.class, AppModule.class, ActivityBuilder.class})
public interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();

    }

    void inject(App app);

}
