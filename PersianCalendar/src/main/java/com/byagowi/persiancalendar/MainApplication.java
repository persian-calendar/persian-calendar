package com.byagowi.persiancalendar;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;
import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import dagger.android.support.DaggerApplication;

public class MainApplication extends DaggerApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ReleaseDebugDifference.mainApplication(this);
        Utils.initUtils(getApplicationContext());
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this).build();
    }

    public static class TestingTT {
        static private int a = 100;

        TestingTT() {
            a += 100;
        }

        public int getA() {
            return a;
        }
    }

    @Scope
    @Documented
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface MyActivityScope {
    }
}

@Module
abstract class ActivitiesBindingModule {
    @MainApplication.MyActivityScope
    @ContributesAndroidInjector(modules = {MainActivityModule.class})
    abstract MainActivity mainActivity();
}

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivitiesBindingModule.class
})
interface AppComponent extends AndroidInjector<MainApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder create(Application app);

        AppComponent build();
    }
}

@Module
class AppModule {
    @Provides
    @Singleton
    SharedPreferences provideSharedPreference(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @Singleton
    MainApplication.TestingTT providesDatabase(Application app) {
//        Room.databaseBuilder(app, AppDatabase:: class.java, "book-db")
//            .
//
//        allowMainThreadQueries().
//
//                build()
        return new MainApplication.TestingTT();
    }
}

@Module
abstract class MainActivityModule {
//    @Module
//    companion object
//
//    {
//        @JvmStatic
//        @Provides
//        fun provideABCKey (preference:SharedPreferences):Any = 32
//    }
}
