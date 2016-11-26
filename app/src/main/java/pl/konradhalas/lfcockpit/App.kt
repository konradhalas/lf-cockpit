package pl.konradhalas.lfcockpit

import android.app.Application
import android.content.Context
import pl.konradhalas.lfcockpit.di.AppComponent
import pl.konradhalas.lfcockpit.di.AppModule
import pl.konradhalas.lfcockpit.di.DaggerAppComponent

class App : Application() {

    val component: AppComponent by lazy {
        DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
    }

    companion object {
        fun getComponent(ctx: Context): AppComponent = (ctx.applicationContext as App).component
    }
}