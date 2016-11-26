package pl.konradhalas.lfcockpit.di

import android.content.Context
import com.polidea.rxandroidble.RxBleClient
import dagger.Module
import dagger.Provides

@Module
class AppModule(val context: Context) {

    @Provides
    @AppScoped
    fun provideRxBleClient(): RxBleClient = RxBleClient.create(context)

    @Provides
    @AppScoped
    fun provideContext(): Context = context
}