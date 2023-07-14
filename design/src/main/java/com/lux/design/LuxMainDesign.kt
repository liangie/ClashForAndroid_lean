package com.lux.design

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.design.Design
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DesignAboutBinding
import com.github.kr328.clash.design.databinding.DesignLuxMainBinding
import com.github.kr328.clash.design.databinding.DesignMainBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.resolveThemedColor
import com.github.kr328.clash.design.util.root
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max

class LuxMainDesign(context: Context) : Design<LuxMainDesign.Request>(context) {
    enum class Request {
        ToggleStatus,
        OpenProxy,
        OpenProfiles,
        OpenProviders,
        OpenLogs,
        OpenSettings,
        OpenHelp,
        OpenAbout,

        AnimStart,
        AnimEnd,
        DrawerAction,
        ConnectAction,
    }

    private val binding = DesignLuxMainBinding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root

    suspend fun setProfileName(name: String?) {
        withContext(Dispatchers.Main) {
            binding.profileName = name
        }
    }

    suspend fun setClashRunning(running: Boolean) {
        withContext(Dispatchers.Main) {
            binding.clashRunning = running
        }
    }

    suspend fun setForwarded(value: Long) {
        withContext(Dispatchers.Main) {
            binding.forwarded = value.trafficTotal()
        }
    }

    suspend fun setMode(mode: TunnelState.Mode) {
        withContext(Dispatchers.Main) {
            binding.mode = when (mode) {
                TunnelState.Mode.Direct -> context.getString(R.string.direct_mode)
                TunnelState.Mode.Global -> context.getString(R.string.global_mode)
                TunnelState.Mode.Rule -> context.getString(R.string.rule_mode)
                TunnelState.Mode.Script -> context.getString(R.string.script_mode)
            }
        }
    }

    suspend fun setHasProviders(has: Boolean) {
        withContext(Dispatchers.Main) {
            binding.hasProviders = has
        }
    }

    suspend fun showAbout(versionName: String) {
        withContext(Dispatchers.Main) {
            val binding = DesignAboutBinding.inflate(context.layoutInflater).apply {
                this.versionName = versionName
            }

            AlertDialog.Builder(context)
                .setView(binding.root)
                .show()
        }
    }

    suspend fun showUpdatedTips() {
        withContext(Dispatchers.Main) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.version_updated)
                .setMessage(R.string.version_updated_tips)
                .setPositiveButton(R.string.ok) { _, _ -> }
                .show()
        }
    }

    var progressValue = 1
    suspend fun setProgressValue(value:Int){

    }

    suspend fun connectAnimation(status:Boolean){
        binding.ivConnection.isSelected = status
//        if (status){
//
//        }else{
//
//        }
    }

    suspend fun drawerAction(){
        binding.drawerRoot.apply {
            if (isDrawerOpen(GravityCompat.START)){
                closeDrawer(GravityCompat.START)
            }else{
                openDrawer(GravityCompat.START)
            }
        }
    }

    var task:TimerTask? = null
    suspend fun connectAction(){
        if (task != null){
            task?.cancel()
            task = null
        }else {
            task = object : TimerTask() {
                override fun run() {
                    MainScope().launch {
                        progressValue += 1
                        binding.circlePB.apply {
                            (progressValue % 200).let { progress ->
                                val newProgress = if (progress <= 100) {
                                    progress
                                } else {
                                    -(progress - 100)
                                }
                                this.progress = newProgress
                            }
                        }
                    }
                }
            }
            Timer().schedule(task, 0, 15)
        }
    }

    init {
        binding.self = this
        binding.colorClashStarted = context.resolveThemedColor(R.attr.colorPrimary)
        binding.colorClashStopped = context.resolveThemedColor(R.attr.colorClashStopped)
    }

    fun request(request: Request) {
        requests.trySend(request)
    }
}