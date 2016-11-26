package pl.konradhalas.lfcockpit.di

import dagger.Component


@Component(modules = arrayOf(AppModule::class))
@AppScoped
interface AppComponent {
    fun plus(): PresenterComponent
}