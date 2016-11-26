package pl.konradhalas.lfcockpit.presenters

import android.util.Log
import pl.konradhalas.lfcockpit.di.PresenterScoped
import pl.konradhalas.lfcockpit.domain.BLEDeviceService
import pl.konradhalas.lfcockpit.domain.Command
import pl.konradhalas.lfcockpit.domain.Message
import javax.inject.Inject

@PresenterScoped
class DevicePresenter @Inject constructor(
        private val bleDeviceService: BLEDeviceService) : BasePresenter<DevicePresenter.UI>() {

    fun connect() {
        showUnknownState()
        ui?.let { ui ->
            bleDeviceService.manageSubscription(
                    bleDeviceService
                            .connect(ui.getDeviceMac())
                            .subscribe(
                                    { state -> ui.showConnectionStatus(state.description) },
                                    { throwable -> onError(throwable) }
                            )
            )
            bleDeviceService.manageSubscription(
                    bleDeviceService
                            .checkSignal()
                            .subscribe(
                                    { signal -> ui.showSignalStrength(signal.strength) },
                                    { throwable -> onError(throwable) }
                            )
            )
            bleDeviceService.manageSubscription(
                    bleDeviceService
                            .receiveMessage()
                            .subscribe(
                                    { message -> ui.receivedMessage(message) },
                                    { throwable -> onError(throwable) }
                            )
            )

        }
    }

    fun sendCommand(command: Command) {
        bleDeviceService.manageSubscription(
                bleDeviceService
                        .sendCommand(command)
                        .subscribe(
                                {},
                                { throwable -> onError(throwable) }
                        )
        )
    }

    fun disconnect() {
        showUnknownState()
        bleDeviceService.disconnect()
    }

    private fun showUnknownState() {
        ui?.let {
            it.showConnectionStatus("Unknown")
            it.showSignalStrength(null)
        }
    }

    private fun onError(throwable: Throwable) {
        Log.e("DevicePresenter", "error", throwable)
        ui?.showError(throwable.toString())
    }

    interface UI {
        fun getDeviceMac(): String
        fun showSignalStrength(signal: Int?)
        fun receivedMessage(message: Message)
        fun showError(error: String)
        fun showConnectionStatus(toString: String)
    }

}