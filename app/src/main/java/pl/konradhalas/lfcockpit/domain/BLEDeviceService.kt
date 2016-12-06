package pl.konradhalas.lfcockpit.domain

import android.content.Context
import android.util.Log
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter
import pl.konradhalas.lfcockpit.di.PresenterScoped
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class Signal(val strength: Int)

data class ConnectionState(val description: String) {
    companion object {
        fun of(state: RxBleConnection.RxBleConnectionState): ConnectionState {
            return ConnectionState(
                    description = when (state) {
                        RxBleConnection.RxBleConnectionState.CONNECTING -> "Connecting"
                        RxBleConnection.RxBleConnectionState.CONNECTED -> "Connected"
                        RxBleConnection.RxBleConnectionState.DISCONNECTING -> "Disconnecting"
                        RxBleConnection.RxBleConnectionState.DISCONNECTED -> "Disconnected"
                        else -> "Unknown"
                    }
            )
        }
    }
}

@PresenterScoped
class BLEDeviceService @Inject constructor(
        private val context: Context,
        private val rxBleClient: RxBleClient) {

    private var compositeSubscription = CompositeSubscription()
    private var connectionObservable: Observable<RxBleConnection>? = null
    private var TAG = "BLE Device Service"

    fun connect(mac: String): Observable<ConnectionState> {
        connectionObservable = rxBleClient
                .getBleDevice(mac)
                .establishConnection(context, false)
                .compose(ConnectionSharingAdapter())
        return rxBleClient
                .getBleDevice(mac)
                .observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .map { state -> ConnectionState.of(state) }
    }

    fun receiveMessage(): Observable<Message> {
        return connectionObservable!!
                .flatMap { connection -> connection.setupNotification(RX_TX_UUID) }
                .flatMap { o -> o }
                .map { data -> String(data, Charsets.US_ASCII) }
                .doOnNext { data -> Log.i(TAG, "received $data") }
                .map { data -> MessagesParser.parse(data) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun sendCommand(command: Command): Observable<Nothing> {
        var data = CommandsSerializer.serialize(command)
        return connectionObservable!!
                .flatMap { it.writeCharacteristic(RX_TX_UUID, data) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { data -> Log.i(TAG, "send ${String(data, Charsets.US_ASCII)}") }
                .flatMap { Observable.empty<Nothing>() }
    }

    fun checkSignal(): Observable<Signal> {
        return connectionObservable!!
                .flatMap { connection -> Observable.interval(1, TimeUnit.SECONDS).flatMap { connection.readRssi() } }
                .observeOn(AndroidSchedulers.mainThread())
                .map(::Signal)
    }

    fun disconnect() {
        compositeSubscription.clear()
    }

    fun manageSubscription(subscription: Subscription) {
        compositeSubscription.add(subscription)
    }

    companion object {
        private val RX_TX_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    }

}
