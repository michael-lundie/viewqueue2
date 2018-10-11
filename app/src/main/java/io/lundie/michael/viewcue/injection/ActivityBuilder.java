/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 21:08
 */

package io.lundie.michael.viewcue.injection;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import io.lundie.michael.viewcue.ui.activities.MainActivity;

@Module
public abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = FragmentModule.class)
    abstract MainActivity bindMainActivity();
}
