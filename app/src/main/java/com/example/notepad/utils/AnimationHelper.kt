package com.example.notepad.utils

import android.view.View
import android.view.animation.AccelerateInterpolator

object AnimationHelper {

    fun startDeleteAnimation(view: View, onAnimationEnd: () -> Unit){
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .rotation(15f)
            .setDuration(2000)
            .setInterpolator (AccelerateInterpolator())
            .withEndAction {
                println("✅ Анимация ЗАВЕРШЕНА")
                onAnimationEnd() }
            .start()
    }
}