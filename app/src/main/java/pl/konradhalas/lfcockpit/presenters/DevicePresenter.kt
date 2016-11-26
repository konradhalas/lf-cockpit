package pl.konradhalas.lfcockpit.presenters

import android.content.Context
import android.util.Log
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit


class DevicePresenter(val context: Context, val ui: UI) {
    private var compositeSubscription = CompositeSubscription()
    private var connectionObservable: Observable<RxBleConnection>? = null
    private var rxBleClient: RxBleClient? = null

    fun connect() {
        showUnknownState()
        if (rxBleClient == null) {
            rxBleClient = RxBleClient.create(context)
        }
        val bleDevice = rxBleClient!!.getBleDevice(ui.getDeviceMac())
        connectionObservable = bleDevice
                .establishConnection(context, false)
                .compose(ConnectionSharingAdapter())
        val notificationSubscription = connectionObservable!!
                .flatMap { connection -> connection.setupNotification(RX_TX_UUID) }
                .flatMap { o -> o }
                .map { data -> String(data, Charsets.US_ASCII) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { data -> ui.showData(data) },
                        { throwable -> onError(throwable) }
                )
        compositeSubscription.add(notificationSubscription)
        val signalSubscription = connectionObservable!!
                .flatMap { connection -> Observable.interval(1, TimeUnit.SECONDS).flatMap { connection.readRssi() } }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { data -> ui.showSignalStrength(data) },
                        { throwable -> onError(throwable) }
                )
        compositeSubscription.add(signalSubscription)
        val connectionStateSubscription = bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { state ->
                            ui.showConnectionStatus(when (state) {
                                RxBleConnection.RxBleConnectionState.CONNECTING -> "Connecting"
                                RxBleConnection.RxBleConnectionState.CONNECTED -> "Connected"
                                RxBleConnection.RxBleConnectionState.DISCONNECTED -> "Disconnected"
                                RxBleConnection.RxBleConnectionState.DISCONNECTING -> "Disconnecting"
                                else -> "Unknown"
                            })
                        },
                        { throwable -> onError(throwable) }
                )
        compositeSubscription.add(connectionStateSubscription)
    }


    fun toggleLed() {
        if (connectionObservable != null) {
            connectionObservable!!
                    .flatMap { it.writeCharacteristic(RX_TX_UUID, "TOOGLE".toByteArray()) }
                    .take(1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {},
                            { throwable -> onError(throwable) }
                    )

        }
    }

    fun disconnect() {
        showUnknownState()
        compositeSubscription.clear()
    }

    private fun showUnknownState() {
        ui.showConnectionStatus("Unknown")
        ui.showSignalStrength(null)
    }

    private fun onError(throwable: Throwable) {
        Log.e("DevicePresenter", "error", throwable)
        ui.showError(throwable.toString())
    }

    interface UI {
        fun getDeviceMac(): String
        fun showSignalStrength(signal: Int?)
        fun showData(data: String)
        fun showError(error: String)
        fun showConnectionStatus(toString: String)
    }

    companion object {
        private val RX_TX_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    }

}