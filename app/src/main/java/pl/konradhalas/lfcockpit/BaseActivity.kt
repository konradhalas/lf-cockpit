package pl.konradhalas.lfcockpit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import pl.konradhalas.lfcockpit.di.PresenterComponent

abstract class BaseActivity : AppCompatActivity() {
    private var component: PresenterComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setComponent()
    }

    fun setComponent() {
        component = App.getComponent(this).plus()
        performInjection(component!!)
    }

    protected abstract fun performInjection(component: PresenterComponent)
}