package systems.concurrent.crediversemobile.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.coroutines.NonDisposableHandle.parent

class Animator(private val child: View, private val _parent: View? = null) {

    private var parent: View = _parent ?: child.rootView

    fun backgroundFromTo(
        from: Int, to: Int, speed: Long = 500, delay: Long = 0, onFinish: (() -> Unit)? = null
    ) {
        child.visibility = View.VISIBLE
        child.setBackgroundColor(from)

        val colorFrom = child.background as ColorDrawable
        val colorAnimation = ObjectAnimator.ofArgb(child, "backgroundColor", colorFrom.color, to)

        colorAnimation.duration = speed
        colorAnimation.startDelay = delay
        colorAnimation.addListener(getAnimator(onFinish))
        colorAnimation.start()
    }

    fun fadeIn(speed: Long = 500, onFinish: (() -> Unit)? = null) {
        child.visibility = View.VISIBLE
        child.alpha = 0f
        child.animate()
            .alpha(1f)
            .setDuration(speed)
            .setListener(getAnimator(onFinish))
            .start()
    }

    private fun getAnimator(onFinish: (() -> Unit)? = null): Animator.AnimatorListener {
        return object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                onFinish?.invoke()
                child.animate().setListener(null)
            }
        }
    }

    fun fadeOut(speed: Long = 500, onFinish: (() -> Unit)? = null) {
        child.visibility = View.VISIBLE
        child.alpha = 1f
        child.animate()
            .alpha(0f)
            .setDuration(speed)
            .setListener(getAnimator(onFinish))
            .start()
    }

    fun fadeInAndTranslateUp(speed: Long = 300, delay: Long = 0, onFinish: (() -> Unit)? = null) {
        child.visibility = View.VISIBLE
        child.alpha = 0f
        child.translationY = parent.height.toFloat()
        child.animate()
            .alpha(1f).translationY(0f)
            .setStartDelay(delay)
            .setDuration(speed)
            .setListener(getAnimator(onFinish))
            .start()
    }

    fun fadeOutAndTranslateDown(speed: Long = 300, onFinish: (() -> Unit)? = null) {
        child.alpha = 1f
        child.y = 0f
        child.translationY = 0f
        child.animate()
            .alpha(0f)
            .translationY(parent.height.toFloat())
            .setDuration(speed)
            .setListener(getAnimator(onFinish))
            .start()
    }
}