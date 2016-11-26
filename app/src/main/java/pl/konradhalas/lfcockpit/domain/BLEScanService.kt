package pl.konradhalas.lfcockpit.domain

import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleDevice
import pl.konradhalas.lfcockpit.di.PresenterScoped
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class BLEDevice(val name: String?, val mac: String) : Serializable {
    companion object {
        fun of(bleDevice: RxBleDevice): BLEDevice {
            return BLEDevice(
                    name = bleDevice.name,
                    mac = bleDevice.macAddress
            )
        }
    }
}

@PresenterScoped
class BLEScanService @Inject constructor(private val rxBleClient: RxBleClient) {
    private var scanSubscription: Subscription? = null

    fun start(): Observable<BLEDevice> {
        return rxBleClient
                .scanBleDevices()
                .sample(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    Observable.timer(5, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { stop() }
                }
                .doOnError { stop() }
                .map { result -> BLEDevice.of(result.bleDevice) }
    }

    fun stop() {
        scanSubscription?.unsubscribe()
        scanSubscription = null
    }

    fun isScanning() = scanSubscription != null

    fun manageSubscription(subscription: Subscription) {
        scanSubscription = subscription
    }
}

