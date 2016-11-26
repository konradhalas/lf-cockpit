package pl.konradhalas.lfcockpit.presenters

import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleDevice
import pl.konradhalas.lfcockpit.di.PresenterScoped
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class DeviceViewModel(val name: String, val mac: String) : Serializable {
    companion object {
        fun of(bleDevice: RxBleDevice): DeviceViewModel {
            return DeviceViewModel(
                    name = bleDevice.name ?: "Unknown",
                    mac = bleDevice.macAddress
            )
        }
    }
}

@PresenterScoped
class ScanPresenter @Inject constructor(private val rxBleClient: RxBleClient) : BasePresenter<ScanPresenter.UI>() {
    private var scanSubscription: Subscription? = null

    fun startScan() {
        scanSubscription = rxBleClient
                .scanBleDevices()
                .sample(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe { ui?.scanFinished() }
                .doOnSubscribe { ui?.scanStarted() }
                .map { result -> DeviceViewModel.of(result.bleDevice) }
                .subscribe(
                        { device -> ui?.showDevice(device) },
                        { throwable ->
                            ui?.showScanError(throwable.toString())
                            stopScan()
                        }
                )
        Observable.timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { stopScan() }
    }

    fun stopScan() {
        scanSubscription?.unsubscribe()
        scanSubscription = null
    }

    fun isScanning() = scanSubscription != null

    interface UI {
        fun showDevice(device: DeviceViewModel)
        fun scanFinished()
        fun scanStarted()
        fun showScanError(error: String)
    }
}