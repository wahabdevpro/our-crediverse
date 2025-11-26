package systems.concurrent.crediversemobile.overlays

import android.content.Context
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.layoutManagers.CircularLayoutManager
import systems.concurrent.crediversemobile.recyclerViewAdapters.NavigationOverlayAdapter
import systems.concurrent.crediversemobile.utils.ActivityUtils
import systems.concurrent.crediversemobile.utils.Animator
import systems.concurrent.crediversemobile.utils.CustomUtils
import systems.concurrent.crediversemobile.utils.EventUtil

class NavigationOverlay(
    private val contextThemeWrapper: ContextThemeWrapper,
    private val parent: ViewGroup,
    private val context: Context
) {
    val view: View? = LayoutInflater.from(contextThemeWrapper)
        .inflate(R.layout.navigation_overlay, parent, false)

    private var navBarHeightPx: Int

    init {
        view?.setOnTouchListener { v, _ ->
            v.performClick()
            // make sure clicking on the background does not send events to any child elements...
            EventUtil.EVENT_CONSUMED
        }

        navBarHeightPx = ActivityUtils.getNavigationBarHeight(context)
        navBarHeightPx =
            if (navBarHeightPx > 0) navBarHeightPx
            else CustomUtils.dpToPx(context, 52)
    }

    fun openNavigationOverlay(overlayData: NavigationOverlayAdapter.OverlayData) {
        val overlayView = view

        view?.let {
            val rootWrapper: ConstraintLayout = view.findViewById(R.id.root_wrapper)
            val circleContainer: FlexboxLayout = view.findViewById(R.id.circle_container)
            val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)

            val rootWrapperAnimator = Animator(rootWrapper, view)
            val overlayAnimator = Animator(view)

            val overlayParams = view.layoutParams as ViewGroup.MarginLayoutParams
            overlayParams.bottomMargin = navBarHeightPx
            view.layoutParams = overlayParams


            overlayAnimator.fadeInAndTranslateUp(speed = 500)
            //parent.removeView(view)
            parent.addView(view)
            rootWrapperAnimator.backgroundFromTo(
                context.getColor(R.color.transparent),
                context.getColor(R.color.dark_faded_background),
                speed = 500
            )

            /**
             * Close the overlay if we click on the Background...
             */
            listOf(rootWrapper, circleContainer, recyclerView).forEach { viewGroup ->
                viewGroup.setOnTouchListener { v, _ ->
                    v.performClick()
                    overlayAnimator.fadeOut(speed = 300) {
                        overlayView?.let { parent.removeView(it) }
                    }
                    EventUtil.EVENT_CONSUMED
                }
            }

            view.findViewById<TextView>(R.id.close_nav).setOnClickListener {
                overlayAnimator.fadeOut(speed = 300) { parent.removeView(view) }
            }

            val layoutManager = CircularLayoutManager()
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter =
                NavigationOverlayAdapter(contextThemeWrapper, overlayData) { page ->
                    Log.e(_tag, "clicked on ${context.getString(page.navNameResource)}")

                    page.doNavigationAction()

                    overlayAnimator.fadeOut(speed = 300) {
                        parent.removeView(view)
                    }
                }
        }
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName
    }
}