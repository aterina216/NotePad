package com.example.notepad.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.notepad.R
import com.example.notepad.databinding.FragmentSplashBinding
import com.example.notepad.view.MainActivity

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Начальное состояние - невидимо
        binding.splashContent.alpha = 0f

        // Запускаем анимацию после отрисовки
        binding.root.postDelayed({
            startSplashAnimation()
        }, 50)
    }

    private fun startSplashAnimation() {
        // Плавное появление всего контента
        binding.splashContent.animate()
            .alpha(1f)
            .setDuration(400)
            .start()

        // Запускаем векторную анимацию
        startVectorAnimation()

        // Переход через 2.5 секунды
        binding.root.postDelayed({
            exitAnimation()
        }, 2500)
    }

    private fun startVectorAnimation() {
        try {
            val drawable = binding.notebookImage.drawable

            when (drawable) {
                is android.graphics.drawable.AnimatedVectorDrawable -> {
                    drawable.start()
                }
                is androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat -> {
                    drawable.start()
                }
                else -> {
                    // Fallback: простая анимация средствами View
                    startFallbackAnimation()
                }
            }
        } catch (e: Exception) {
            startFallbackAnimation()
        }
    }

    private fun startFallbackAnimation() {
        // Резервная анимация, если векторная не сработает
        binding.notebookImage.alpha = 0f
        binding.notebookImage.scaleX = 0.7f
        binding.notebookImage.scaleY = 0.7f

        binding.notebookImage.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .withStartAction {
                // Анимация появления строк с задержкой
                binding.root.postDelayed({
                    animateLinesFallback()
                }, 400)
            }
            .start()
    }

    private fun animateLinesFallback() {
        // Имитация появления строк через несколько TextView
        val lines = listOf(binding.line1, binding.line2, binding.line3, binding.line4)
        lines.forEachIndexed { index, textView ->
            textView.alpha = 0f
            textView.animate()
                .alpha(1f)
                .setStartDelay(index * 100L)
                .setDuration(250)
                .start()
        }
    }

    private fun exitAnimation() {
        binding.splashContent.animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                (activity as? MainActivity)?.onSplashFinished()
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}