package com.github.kr328.clash.ui.activity

import com.github.kr328.clash.BaseActivity
import com.lux.design.LuxMainDesign
import com.lux.design.LuxSplashDesign
//import com.lux.design.LuxSplashDesign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LuxSplashActivity: BaseActivity<LuxSplashDesign>() {
    override suspend fun main() {
        val design = LuxSplashDesign(this)
        setContentDesign(design)

    }
}