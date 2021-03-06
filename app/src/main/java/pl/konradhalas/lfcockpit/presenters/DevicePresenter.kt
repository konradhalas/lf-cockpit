package pl.konradhalas.lfcockpit.presenters

import android.util.Log
import pl.konradhalas.lfcockpit.di.PresenterScoped
import pl.konradhalas.lfcockpit.domain.BLEDeviceService
import pl.konradhalas.lfcockpit.domain.Command
import pl.konradhalas.lfcockpit.domain.Message
import pl.konradhalas.lfcockpit.domain.SensorValue
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
                                    { message ->
                                        when (message) {
                                            is Message.BatteryMessage -> ui.showBatteryVoltage(message.voltage)
                                            is Message.SensorsMessage -> ui.showSensorsValues(message.values)
                                        }
                                    },
                                    { throwable -> onError(throwable) }
                            )
            )
        }
    }

    fun startStop() {
        bleDeviceService.manageSubscription(
                bleDeviceService
                        .sendCommand(Command.StartStopCommand())
                        .take(1)
                        .subscribe(
                                {},
                                { throwable -> onError(throwable) }
                        )
        )
    }

    fun calibrate() {
        bleDeviceService.manageSubscription(
                bleDeviceService
                        .sendCommand(Command.CalibrateCommand())
                        .take(1)
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
            it.showBatteryVoltage(null)
        }
    }

    private fun onError(throwable: Throwable) {
        Log.e("DevicePresenter", "error", throwable)
        ui?.showError(throwable.toString())
    }

    interface UI {
        fun getDeviceMac(): String
        fun showSignalStrength(signal: Int?)
        fun showError(error: String)
        fun showConnectionStatus(toString: String)
        fun showBatteryVoltage(voltage: Int?)
        fun showSensorsValues(sensorsValues: List<SensorValue>)
    }

}