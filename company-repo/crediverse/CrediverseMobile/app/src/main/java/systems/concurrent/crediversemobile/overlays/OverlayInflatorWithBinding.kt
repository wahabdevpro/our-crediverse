package systems.concurrent.crediversemobile.overlays

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import systems.concurrent.crediversemobile.R

open class OverlayInflatorWithBinding<Binding : ViewBinding>(
    contextThemeWrapper: ContextThemeWrapper,
    val parent: ViewGroup,
    layoutResource: Int,
    rootView: ViewGroup? = null,
) {
    val view: View
    val binding: Binding

    private val closeButton: FloatingActionButton?

    init {
        view = LayoutInflater.from(contextThemeWrapper).inflate(layoutResource, rootView, false)
        binding = DataBindingUtil.bind(view)!!
        closeButton = binding.root.findViewById(R.id.close)

        setupDefaultCloseButtonAction()
    }

    private fun setupDefaultCloseButtonAction() {
        closeButton?.setOnClickListener { closeOverlay() }
    }

    open fun openOverlay() = parent.addView(view)
    fun closeOverlay() = parent.removeView(view)

    /**
     * Custom on Close action -- still incorporates 'closeOverlay()'
     */
    fun onClose(callback: ((View) -> Unit)? = null) {
        closeButton?.setOnClickListener {
            closeOverlay()
            callback?.invoke(it)
        }
    }
}