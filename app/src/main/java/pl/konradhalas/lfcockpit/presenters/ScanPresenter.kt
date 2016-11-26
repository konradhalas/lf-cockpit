package pl.konradhalas.lfcockpit.presenters

import pl.konradhalas.lfcockpit.di.PresenterScoped
import pl.konradhalas.lfcockpit.domain.BLEDevice
import pl.konradhalas.lfcockpit.domain.BLEScanService
import java.io.Serializable
import javax.inject.Inject

data class DeviceViewModel(val name: String, val mac: String) : Serializable {
    companion object {
        fun of(bleDevice: BLEDevice): DeviceViewModel {
            return DeviceViewModel(
                    name = bleDevice.name ?: "Unknown",
                    mac = bleDevice.mac
            )
        }
    }
}

@PresenterScoped
class ScanPresenter @Inject constructor(private val bleScanService: BLEScanService) : BasePresenter<ScanPresenter.UI>() {

    fun startScan() {
        bleScanService.manageSubscription(
                bleScanService.start()
                        .doOnUnsubscribe { ui?.scanFinished() }
                        .doOnSubscribe { ui?.scanStarted() }
                        .map { DeviceViewModel.of(it) }
                        .subscribe(
                                { device -> ui?.showDevice(device) },
                                { throwable -> ui?.showScanError(throwable.toString()) }
                        )
        )
    }

    fun stopScan() {
        bleScanService.stop()
    }

    fun isScanning() = bleScanService.isScanning()

    interface UI {
        fun showDevice(device: DeviceViewModel)
        fun scanFinished()
        fun scanStarted()
        fun showScanError(error: String)
    }
}