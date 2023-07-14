package com.lux.design

import android.content.Context
import android.view.View
import com.github.kr328.clash.design.Design
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.databinding.DesignLuxSplashBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.resolveThemedColor
import com.github.kr328.clash.design.util.root

class LuxSplashDesign(ctx:Context):Design<LuxSplashDesign.Request>(ctx) {

    enum class Request{

    }



    private val binding = DesignLuxSplashBinding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root

    init {
//        binding.self = this
//        binding.colorClashStarted = context.resolveThemedColor(R.attr.colorPrimary)
//        binding.colorClashStopped = context.resolveThemedColor(R.attr.colorClashStopped)
    }
}