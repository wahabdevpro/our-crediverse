package systems.concurrent.crediversemobile.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class InterceptedFragment @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val _tag = this::class.java.kotlin.simpleName

    /**
     *
     * Unused for now - we did need it at one point, but stopped using it
     *
     * Rather keep it, the chance of needing to intercept the fragment still exists
     *
     */
}
