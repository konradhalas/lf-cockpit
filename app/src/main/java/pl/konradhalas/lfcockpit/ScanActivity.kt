package pl.konradhalas.lfcockpit

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import pl.konradhalas.lfcockpit.common.recycle.ArrayRecycleAdapter
import pl.konradhalas.lfcockpit.common.recycle.SingleViewViewHolder
import pl.konradhalas.lfcockpit.presenters.DeviceViewModel
import pl.konradhalas.lfcockpit.presenters.ScanPresenter
import pl.konradhalas.lfcockpit.views.DeviceListItemView
import java.util.*


class ScanActivity : AppCompatActivity(), ScanPresenter.UI {
    private val devicesView by lazy { findViewById(R.id.devices) as RecyclerView }
    private val emptyView by lazy { findViewById(R.id.empty) as TextView }
    private val progressView by lazy { findViewById(R.id.progress) as ProgressBar }
    private val devices: ArrayList<DeviceViewModel> = ArrayList()
    lateinit private var scanPresenter: ScanPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        devicesView.layoutManager = LinearLayoutManager(this)
        devicesView.adapter = DevicesAdapter(devices)
        title = getString(R.string.scan_title)
        scanPresenter = ScanPresenter(this, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_scan, menu)
        val scanItem = menu.findItem(R.id.action_scan)
        scanItem.setOnMenuItemClickListener {
            if (scanPresenter.isScanning()) scanPresenter.stopScan() else scanPresenter.startScan()
            true
        }
        scanItem.title = if (scanPresenter.isScanning()) getString(R.string.pause) else getString(R.string.scan)
        return true
    }

    override fun onResume() {
        super.onResume()
        scanPresenter.startScan()
    }

    private fun updateDevicesList() {
        (devicesView.adapter as DevicesAdapter).swapData(devices)
    }


    override fun showDevice(device: DeviceViewModel) {
        if (!devices.contains(device)) {
            devices.add(device)
            updateDevicesList()
        }
    }

    override fun scanFinished() {
        invalidateOptionsMenu()
        progressView.isIndeterminate = false
        if (devices.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        }
    }

    override fun scanStarted() {
        invalidateOptionsMenu()
        emptyView.visibility = View.GONE
        devices.clear()
        updateDevicesList()
        progressView.isIndeterminate = true
    }

    override fun showScanError(error: String) {
        Snackbar.make(devicesView, "Error: $error", Snackbar.LENGTH_LONG).show()
    }

    private fun connectToDevice(device: DeviceViewModel) {
        scanPresenter.stopScan()
        this.startActivity(DeviceActivity.createIntent(this, device))
        this.finish()
    }

    override fun onPause() {
        super.onPause()
        scanPresenter.stopScan()
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
