package pl.konradhalas.lfcockpit.di

import dagger.Subcomponent
import pl.konradhalas.lfcockpit.DeviceActivity
import pl.konradhalas.lfcockpit.ScanActivity


@Subcomponent
@PresenterScoped
interface PresenterComponent {
    fun inject(scanActivity: ScanActivity)
    fun inject(deviceActivity: DeviceActivity)
}
