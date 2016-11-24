package pl.konradhalas.lfcockpit.views

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.widget.TextView
import pl.konradhalas.lfcockpit.R
import pl.konradhalas.lfcockpit.presenters.DeviceViewModel

class DeviceListItemView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    private val nameView by lazy { findViewById(R.id.name) as TextView }
    private val macView by lazy { findViewById(R.id.mac) as TextView }

    private var content: DeviceViewModel? = null

    fun setContent(data: DeviceViewModel) {
        content = data
        nameView.text = data.name
        macView.text = data.mac
    }

    fun setConnectListener(listener: (DeviceViewModel) -> Unit) {
        setOnClickListener {
            content?.let { listener(it) }
        }
    }
}