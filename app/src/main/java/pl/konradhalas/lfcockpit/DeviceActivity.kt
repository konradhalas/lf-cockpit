package pl.konradhalas.lfcockpit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import com.polidea.rxandroidble.RxBleClient
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.*

class DeviceActivity : AppCompatActivity() {
    private var subscription: Subscription? = null
    private val dataView by lazy { findViewById(R.id.data) as TextView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        title = getDevice().name

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_device, menu)
        menu.findItem(R.id.action_disconnect).setOnMenuItemClickListener {
            subscription?.let { it.unsubscribe() }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            true
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val rxBleClient = RxBleClient.create(this)
        subscription = rxBleClient.getBleDevice(getDevice().mac)
                .establishConnection(this, false)
                .flatMap({ connection -> connection.setupNotification(DeviceActivity.RX_TX_UUID) })
                .flatMap({ notificationObservable -> notificationObservable })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { data -> dataView.append(String(data, Charsets.US_ASCII)) },
                        { throwable -> Toast.makeText(this, throwable.toString(), Toast.LENGTH_LONG).show() }
                )
    }

    override fun onPause() {
        super.onPause()
        subscription?.unsubscribe()
    }

    private fun getDevice(): DeviceViewModel = intent.getSerializableExtra(ARG_DEVICE) as DeviceViewModel

    companion object {
        private val ARG_DEVICE = "ARG_DEVICE"
        private val RX_TX_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

        fun createIntent(ctx: Context, device: DeviceViewModel): Intent {
            return Intent(ctx, DeviceActivity::class.java).putExtra(ARG_DEVICE, device)
        }
    }
}