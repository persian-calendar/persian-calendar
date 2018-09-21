package com.byagowi.persiancalendar;

import android.app.Activity;
import android.app.Application;

import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.byagowi.persiancalendar.view.fragment.MonthFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Scope;
import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.support.DaggerApplication;

public class MainApplication extends Application implements HasActivityInjector {
    @Override
    public void onCreate() {
        super.onCreate();
        DaggerMainApplication_AppComponent.create().inject(this);
        ReleaseDebugDifference.mainApplication(this);
        Utils.initUtils(getApplicationContext());
    }

    @Inject
    AppDependency appDependency;

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }

    // AppModule.java
    @Module(includes = AndroidInjectionModule.class)
    static abstract class AppModule {
        @PerActivity
        @ContributesAndroidInjector(modules = MainActivityModule.class)
        abstract MainActivity mainActivityInjector();
    }

    // AppComponent.java
    @Singleton
    @Component(modules = AppModule.class)
    interface AppComponent {
        void inject(MainApplication app);
    }


    // MainActivityModule.java
    @Module
    static public abstract class MainActivityModule {
        @PerFragment
        @ContributesAndroidInjector(modules = MainFragmentModule.class)
        abstract CalendarFragment mainFragmentInjector();
    }

    // MainFragmentModule.java
    @Module
    static public abstract class MainFragmentModule {
        @PerChildFragment
        @ContributesAndroidInjector(modules = MainChildFragmentModule.class)
        abstract MonthFragment mainChildFragmentInjector();
    }

    // MainChildFragmentModule.java
    @Module
    static public abstract class MainChildFragmentModule {
    }

    // PerActivity.java
    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PerActivity {
    }

    // PerFragment.java
    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PerFragment {
    }

    // PerChildFragment.java
    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PerChildFragment {
    }

    // AppDependency.java
    @Singleton
    static public final class AppDependency {
        @Inject
        AppDependency() {
        }
    }

    // ActivityDependency.java
    @PerActivity
    static public final class ActivityDependency {
        @Inject
        ActivityDependency() {
        }
    }

    // FragmentDependency.java
    @PerFragment
    static public final class FragmentDependency {
        @Inject
        FragmentDependency() {
        }
    }

    // ChildFragmentDependency.java
    @PerChildFragment
    static public final class ChildFragmentDependency {
        @Inject
        ChildFragmentDependency() {
        }
    }
}
