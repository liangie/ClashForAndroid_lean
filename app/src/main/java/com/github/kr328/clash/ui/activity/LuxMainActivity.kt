package com.github.kr328.clash.ui.activity

import androidx.activity.result.contract.ActivityResultContracts
import com.github.kr328.clash.BaseActivity
import com.github.kr328.clash.HelpActivity
import com.github.kr328.clash.LogsActivity
import com.github.kr328.clash.ProfilesActivity
import com.github.kr328.clash.ProvidersActivity
import com.github.kr328.clash.ProxyActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.SettingsActivity
import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.common.util.ticker
import com.github.kr328.clash.design.MainDesign
import com.github.kr328.clash.design.ui.ToastDuration
import com.github.kr328.clash.store.TipsStore
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService
import com.github.kr328.clash.util.withClash
import com.github.kr328.clash.util.withProfile
import com.lux.design.LuxMainDesign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LuxMainActivity : BaseActivity<LuxMainDesign>() {
    override suspend fun main() {
        val design = LuxMainDesign(this)

        setContentDesign(design)

        launch(Dispatchers.IO) {
            showUpdatedTips(design)
        }

        design.fetch()

        val ticker = ticker(TimeUnit.SECONDS.toMillis(1))

        design.setProgressValue(50)

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.ActivityStart,
                        Event.ServiceRecreated,
                        Event.ClashStop, Event.ClashStart,
                        Event.ProfileLoaded, Event.ProfileChanged -> design.fetch()

                        else -> Unit
                    }
                }
                design.requests.onReceive {

                    when (it) {
                        LuxMainDesign.Request.ToggleStatus -> {
                            if (clashRunning)
                                stopClashService()
                            else
                                design.startClash()
                        }

                        LuxMainDesign.Request.OpenProxy ->
                            startActivity(ProxyActivity::class.intent)

                        LuxMainDesign.Request.OpenProfiles ->
                            startActivity(ProfilesActivity::class.intent)

                        LuxMainDesign.Request.OpenProviders ->
                            startActivity(ProvidersActivity::class.intent)

                        LuxMainDesign.Request.OpenLogs ->
                            startActivity(LogsActivity::class.intent)

                        LuxMainDesign.Request.OpenSettings ->
                            startActivity(SettingsActivity::class.intent)

                        LuxMainDesign.Request.OpenHelp ->
                            startActivity(LuxSplashActivity::class.intent)

                        LuxMainDesign.Request.OpenAbout ->
                            design.showAbout(queryAppVersionName())

                        LuxMainDesign.Request.AnimStart ->
                            design.connectAnimation(true)

                        LuxMainDesign.Request.AnimEnd ->
                            design.connectAnimation(false)

                        LuxMainDesign.Request.DrawerAction ->
                            design.drawerAction()

                        LuxMainDesign.Request.ConnectAction ->
                            design.connectAction()
                    }
                }
                if (clashRunning) {
                    ticker.onReceive {
                        design.fetchTraffic()
                    }
                }
            }
        }
    }

    private suspend fun showUpdatedTips(design: LuxMainDesign) {
        val tips = TipsStore(this)

        if (tips.primaryVersion != TipsStore.CURRENT_PRIMARY_VERSION) {
            tips.primaryVersion = TipsStore.CURRENT_PRIMARY_VERSION

            val pkg = packageManager.getPackageInfo(packageName, 0)

            if (pkg.firstInstallTime != pkg.lastUpdateTime) {
                design.showUpdatedTips()
            }
        }
    }

    private suspend fun LuxMainDesign.fetch() {
        setClashRunning(clashRunning)

        val state = withClash {
            queryTunnelState()
        }
        val providers = withClash {
            queryProviders()
        }

        setMode(state.mode)
        setHasProviders(providers.isNotEmpty())

        withProfile {
            setProfileName(queryActive()?.name)
        }
    }

    private suspend fun LuxMainDesign.fetchTraffic() {
        withClash {
            setForwarded(queryTrafficTotal())
        }
    }

    private suspend fun LuxMainDesign.startClash() {
        val active = withProfile { queryActive() }

        if (active == null || !active.imported) {
            showToast(R.string.no_profile_selected, ToastDuration.Long) {
                setAction(R.string.profiles) {
                    startActivity(ProfilesActivity::class.intent)
                }
            }

            return
        }

        val vpnRequest = startClashService()

        try {
            if (vpnRequest != null) {
                val result = startActivityForResult(
                    ActivityResultContracts.StartActivityForResult(),
                    vpnRequest
                )

                if (result.resultCode == RESULT_OK)
                    startClashService()
            }
        } catch (e: Exception) {
            design?.showToast(R.string.unable_to_start_vpn, ToastDuration.Long)
        }
    }

    private suspend fun queryAppVersionName(): String {
        return withContext(Dispatchers.IO) {
            packageManager.getPackageInfo(packageName, 0).versionName
        }
    }
}