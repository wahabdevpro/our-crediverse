package systems.concurrent.crediversemobile.layoutManagers

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import systems.concurrent.crediversemobile.utils.NavigationManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min
import kotlin.math.PI

class CircularLayoutManager : RecyclerView.LayoutManager() {
    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    private val realItemCount get() = itemCount - 1

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        recycler?.let { recyclerInstance ->
            detachAndScrapAttachedViews(recyclerInstance)

            if (realItemCount < 0) return

            val parentCenterX = width / 2
            val parentCenterY = height
            val radius = min(parentCenterX, parentCenterY) / 1.2

            var child = recyclerInstance.getViewForPosition(0)
            addView(child)
            measureChildWithMargins(child, 0, 0)

            var childWidth = getDecoratedMeasuredWidth(child)
            var childHeight = getDecoratedMeasuredHeight(child)

            val centerX = parentCenterX - (childWidth / 2)
            val centerY = parentCenterY - childHeight
            layoutDecorated(
                child, centerX, centerY,
                centerX + childWidth, centerY + childHeight
            )

            for (i in 0 until realItemCount) {
                val position = i + 1

                child = recyclerInstance.getViewForPosition(position)
                addView(child)
                measureChildWithMargins(child, 0, 0)

                childWidth = getDecoratedMeasuredWidth(child)
                childHeight = getDecoratedMeasuredHeight(child) + 200

                val offset = 2 * PI / (realItemCount * 2) / 2
                // Calculate the position of each child view in a circle
                val angle = ((2 * PI * i) / (realItemCount * 2)) + PI + offset
                val childCenterX = parentCenterX + (radius * cos(angle)).toInt() - childWidth / 2
                val childCenterY = parentCenterY + (radius * sin(angle)).toInt() - childHeight / 2

                layoutDecorated(
                    child, childCenterX, childCenterY,
                    childCenterX + childWidth, childCenterY + childHeight - 200
                )
            }
        }
    }
}
