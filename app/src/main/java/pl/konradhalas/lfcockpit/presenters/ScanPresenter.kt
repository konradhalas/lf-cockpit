package pl.konradhalas.lfcockpit.presenters

import android.content.Context
import com.polidea.rxandroidble.RxBleClient
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.concurrent.TimeUnit

data class DeviceViewModel(val name: String, val mac: String) : Serializable


class ScanPresenter(val context: Context, val ui: UI) {
    private var scanSubscription: Subscription? = null

    fun startScan() {
        scanSubscription = RxBleClient.create(context)
                .scanBleDevices()
                .sample(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe { ui.scanFinished() }
                .doOnSubscribe { ui.scanStarted() }
                .map { result -> DeviceViewModel(result.bleDevice.name, result.bleDevice.macAddress) }
                .subscribe(
                        { device -> ui.showDevice(device) },
                        { throwable ->
                            ui.showScanError(throwable.toString())
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