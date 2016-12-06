package pl.konradhalas.lfcockpit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import pl.konradhalas.lfcockpit.di.PresenterComponent
import pl.konradhalas.lfcockpit.presenters.DevicePresenter
import pl.konradhalas.lfcockpit.presenters.DeviceViewModel
import javax.inject.Inject


class DeviceActivity : BaseActivity(), DevicePresenter.UI {

    private val contentView by lazy { findViewById(R.id.content) as LinearLayout }
    private val signalView by lazy { findViewById(R.id.signal) as TextView }
    private val stateView by lazy { findViewById(R.id.state) as TextView }
    private val batteryView by lazy { findViewById(R.id.battery) as TextView }
    private val toggleButton by lazy { findViewById(R.id.toggle_button) as Button }
    private val buttonStatus by lazy { findViewById(R.id.button_status) as Button }

    @Inject
    lateinit var presenter: DevicePresenter

    override fun performInjection(component: PresenterComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setup(this)
        setContentView(R.layout.activity_device)
        title = getDevice().name
        toggleButton.setOnClickListener { presenter.toggleLED() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_device, menu)
        menu.findItem(R.id.action_disconnect).setOnMenuItemClickListener {
            presenter.disconnect()
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
            true
        }
        return true
    }

    override fun showButtonState(up: Boolean) {
        buttonStatus.text = "Button state: ${if (up) "UP" else "DOWN"}"
    }

    override fun showBatteryVoltage(voltage: Int?) {
        if (voltage != null) {
            batteryView.text = "%.2f V".format(voltage.toFloat() / 1000)
        } else {
            batteryView.text = "- V"
        }
    }

    override fun showError(error: String) {
        Snackbar.make(contentView, "Error: $error", Snackbar.LENGTH_LONG).show()
    }

    override fun showConnectionStatus(status: String) {
        stateView.text = status
    }

    override fun getDeviceMac() = getDevice().mac

    override fun showSignalStrength(signal: Int?) {
        signalView.text = "${signal ?: "-"} dB"
    }

    override fun onResume() {
        super.onResume()
        presenter.connect()
    }

    override fun onPause() {
        super.onPause()
        presenter.disconnect()
    }

    private fun getDevice(): DeviceViewModel = intent.getSerializableExtra(ARG_DEVICE) as DeviceViewModel

    companion object {
        private val ARG_DEVICE = "ARG_DEVICE"

        fun createIntent(ctx: Context, device: DeviceViewModel): Intent {
            return Intent(ctx, DeviceActivity::class.java).putExtra(ARG_DEVICE, device)
        }
    }
}