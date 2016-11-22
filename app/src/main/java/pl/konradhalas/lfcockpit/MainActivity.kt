package pl.konradhalas.lfcockpit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.polidea.rxandroidble.RxBleClient
import pl.konradhalas.lfcockpit.common.recycle.ArrayRecycleAdapter
import pl.konradhalas.lfcockpit.common.recycle.SingleViewViewHolder
import pl.konradhalas.lfcockpit.views.DeviceListItemView
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

data class DeviceViewModel(val name: String, val mac: String) : Serializable

class MainActivity : AppCompatActivity() {

    private val devicesView by lazy { findViewById(R.id.devices) as RecyclerView }
    private val progressView by lazy { findViewById(R.id.progress) as ProgressBar }
    private val devices: ArrayList<DeviceViewModel> = ArrayList()
    private var scanSubscription: Subscription? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        devicesView.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_scan, menu)
        val scanItem = menu.findItem(R.id.action_scan)
        scanItem.setOnMenuItemClickListener {
            if (scanSubscription == null) {
                startScan()
            } else {
                stopScan()
            }

            true
        }
        if (scanSubscription == null) {
            scanItem.title = "Scan"
        } else {
            scanItem.title = "Pause"

        }
        return true
    }

    private fun stopScan() {
        scanSubscription?.unsubscribe()
        scanSubscription = null
        invalidateOptionsMenu()
    }

    override fun onResume() {
        super.onResume()
        startScan()
    }

    private fun startScan() {
        devices.clear()
        devicesView.adapter = DevicesAdapter(devices)
        val rxBleClient = RxBleClient.create(this)
        scanSubscription = rxBleClient
                .scanBleDevices()
                .sample(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map { result -> DeviceViewModel(result.bleDevice.name, result.bleDevice.macAddress) }
                .take(20)
                .doOnUnsubscribe { progressView.isIndeterminate = false }
                .doOnSubscribe { progressView.isIndeterminate = true }
                .subscribe(
                        { device -> showDevice(device) },
                        { throwable ->
                            Toast.makeText(this, throwable.toString(), Toast.LENGTH_LONG).show()
                            stopScan()
                        }
                )
        Observable.timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { stopScan() }
        invalidateOptionsMenu()
    }

    private fun showDevice(device: DeviceViewModel) {
        val deviceIndex = devices.indexOfFirst { d -> d.mac == device.mac }
        if (deviceIndex != -1) {
            devices[deviceIndex] = device
        } else {
            devices.add(device)
        }
        devicesView.adapter = DevicesAdapter(devices)
    }

    private fun connectToDevice(device: DeviceViewModel) {
        stopScan()
        this.startActivity(DeviceActivity.createIntent(this, device))
        this.finish()
    }

    override fun onPause() {
        super.onPause()
        stopScan()
    }

    inner class DevicesAdapter(devices: List<DeviceViewModel>) : ArrayRecycleAdapter<DeviceViewModel, SingleViewViewHolder<DeviceListItemView>>(this, devices) {

        override fun onCreateViewHolder(inflater: LayoutInflater, viewGroup: ViewGroup, viewType: Int): SingleViewViewHolder<DeviceListItemView> {
            val view = inflater.inflate(R.layout.view_device_list_item, viewGroup, false) as DeviceListItemView
            view.setConnectListener({ connectToDevice(it) })
            return SingleViewViewHolder(view)
        }

        override fun onBindViewHolder(data: DeviceViewModel, viewHolder: SingleViewViewHolder<DeviceListItemView>, index: Int) {
            viewHolder.view.setContent(data)
        }

    }

}
