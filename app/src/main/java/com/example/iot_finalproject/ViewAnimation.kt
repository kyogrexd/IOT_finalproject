package com.example.iot_finalproject

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils

object ViewAnimation {
    enum class AnimationStatus {
        OnAnimationStart, OnAnimationEnd, OnAnimationRepeat
    }

    private fun getAnimationListener(result: ((AnimationStatus) -> Unit)?): Animation.AnimationListener {
        return object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) { result?.let { it(AnimationStatus.OnAnimationStart) }  }
            override fun onAnimationEnd(animation: Animation?) { result?.let { it(AnimationStatus.OnAnimationEnd) }  }
            override fun onAnimationRepeat(animation: Animation?) { result?.let { it(AnimationStatus.OnAnimationRepeat) } }
        }
    }
    /**
     * 升降1
     */
    fun slideUP(context: Context, view: View, duration: Long? = null, result: ((AnimationStatus) -> Unit)? = null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.slid_up)
        anim.setAnimationListener(getAnimationListener(result))
        duration?.let { anim.duration = it }
        view.clearAnimation()
        view.startAnimation(anim)
    }
    fun slideDown(context: Context, view: View, duration: Long? = null, result: ((AnimationStatus) -> Unit)? = null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.slid_down)
        anim.setAnimationListener(getAnimationListener(result))
        duration?.let { anim.duration = it }
        view.clearAnimation()
        view.startAnimation(anim)
    }

    /**
     * 升降2
     */
    fun rise(context: Context, view: View, duration: Long? = null, result: ((AnimationStatus) -> Unit)? = null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.view_rise)
        anim.setAnimationListener(getAnimationListener(result))
        duration?.let { anim.duration = it }
        view.clearAnimation()
        view.startAnimation(anim)
    }
    fun drop(context: Context, view: View, duration: Long? = null, result: ((AnimationStatus) -> Unit)? = null) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.view_drop)
        anim.setAnimationListener(getAnimationListener(result))
        duration?.let { anim.duration = it }
        view.clearAnimation()
        view.startAnimation(anim)
    }
    fun clearRippleAnimation(view: View) {
        view.animation = null
        view.clearAnimation()
    }
}