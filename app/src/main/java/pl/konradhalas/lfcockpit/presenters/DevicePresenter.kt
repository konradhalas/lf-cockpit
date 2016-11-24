package pl.konradhalas.lfcockpit.presenters

import android.content.Context
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleConnection
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

sealed class DataOrSignal {
    class Data(val data: String) : DataOrSignal()
    class Signal(val signal: Int) : DataOrSignal()
}

class DevicePresenter(val context: Context, val ui: UI) {
    private var subscription: Subscription? = null
    private var connectionStateSubscription: Subscription? = null

    fun connect() {
        val rxBleClient = RxBleClient.create(context)
        val bleDevice = rxBleClient.getBleDevice(ui.getDeviceMac())
        subscription = bleDevice
                .establishConnection(context, false)

                .flatMap { connection ->
                    Observable.merge(
                            connection.setupNotification(RX_TX_UUID).flatMap { o -> o }.map { data -> DataOrSignal.Data(String(data, Charsets.US_ASCII)) },
                            Observable.interval(1, TimeUnit.SECONDS).flatMap { connection.readRssi() }.map { signal -> DataOrSignal.Signal(signal) }
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { data ->
                            when (data) {
                                is DataOrSignal.Signal -> ui.showSignalStrength(data.signal)
                                is DataOrSignal.Data -> ui.showData(data.data)
                            }
                        },
                        { throwable -> ui.showError(throwable.toString()) }
                )
        connectionStateSubscription = bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { state ->
                            ui.showConnectionStatus(when (state) {
                                RxBleConnection.RxBleConnectionState.CONNECTING -> "Connecting"
                                RxBleConnection.RxBleConnectionState.CONNECTED -> "Connected"
                                RxBleConnection.RxBleConnectionState.DISCONNECTED -> "Disconnected"
                                RxBleConnection.RxBleConnectionState.DISCONNECTING -> "Disconnectin"
                                else -> "Unknown"
                            })
                        },
                        { throwable -> ui.showError(throwable.toString()) }
                )
    }

    fun disconnect() {
        subscription?.unsubscribe()
        connectionStateSubscription?.unsubscribe()
    }

    interface UI {
        fun getDeviceMac(): String
        fun showSignalStrength(signal: Int)
        fun showData(data: String)
        fun showError(error: String)
        fun showConnectionStatus(toString: String)
    }

    companion object {
        private val RX_TX_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    }
}