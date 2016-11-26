package pl.konradhalas.lfcockpit.presenters

open class BasePresenter<UI> {
    protected var ui: UI? = null

    fun setup(uiToAttach: UI) {
        ui = uiToAttach
    }
}