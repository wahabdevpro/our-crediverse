package systems.concurrent.crediversemobile.utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.util.ChartUtils
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.repositories.MasRepository
import java.time.DayOfWeek

class ChartingUtils {
    data class ColumnData(
        val mainChartData: ColumnChartData, private var _preview: ColumnChartData? = null
    ) {
        val previewChartData get() = _preview
        val chartYPadding get() = _chartYPadding

        private var _chartYPadding = 0

        fun setPreviewData(data: ColumnChartData) {
            _preview = data
        }

        fun setChartPadding(pastData: List<Float>, presentData: List<Float>) {
            val maxOfPast = pastData.maxOf { it }
            val maxOfPresent = presentData.maxOf { it }
            val maximumData = listOf(maxOfPast, maxOfPresent).maxOf { it }

            _chartYPadding = when {
                maximumData < 1000 -> 16
                maximumData < 10000 -> 32
                maximumData < 100000 -> 64
                maximumData < 1000000 -> 128
                else -> 256
            }
        }
    }

    data class TachometerData(val mainChartData: PieChartData, val fillPercent: Float)

    data class Colors(
        val dull: Int = R.color.medium_gray,
        val success: Int = R.color.green,
        val failure: Int = R.color.red,
        val active: Int = R.color.cs_primary
    )

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        fun getWeeklyLabels(
            context: Context, starting: DayOfWeek = MasRepository.weekStartDay
        ): List<AxisValue> {
            val weekdayStringsList = listOf(
                context.getString(R.string.weekday_abbreviation_monday),
                context.getString(R.string.weekday_abbreviation_tuesday),
                context.getString(R.string.weekday_abbreviation_wednesday),
                context.getString(R.string.weekday_abbreviation_thursday),
                context.getString(R.string.weekday_abbreviation_friday),
                context.getString(R.string.weekday_abbreviation_saturday),
                context.getString(R.string.weekday_abbreviation_sunday),
            )

            val startingIndex = DayOfWeek.values().indexOf(starting)

            val rolloverStringsList = weekdayStringsList.subList(startingIndex, 7) +
                    weekdayStringsList.subList(0, startingIndex)

            return rolloverStringsList.mapIndexed { i, v -> AxisValue(i.toFloat()).setLabel(v) }
        }

        fun getTachometerChartData(
            context: Context,
            fillPercent: Float? = null,
            chartColor: Int = R.color.cs_primary
        ): TachometerData {
            // I shouldn't have to ...
            //  but for some reason I must enforce the seed for the random number generator :/
            val random = kotlin.random.Random(System.currentTimeMillis())

            val actualFillPercent = when (fillPercent) {
                null -> random.run { (500 + nextInt(-300, 310)).toFloat() / 10 }
                else -> {
                    if (fillPercent in 0f..100f) fillPercent else {
                        if (fillPercent > 100f) 100f else 50f
                    }
                } // if percent invalid ... make dead center
            }

            Log.e(_tag, "percent: $actualFillPercent")

            val values = ArrayList<SliceValue>()
            // Add slices for the top and bottom halves
            /**
             * DO NOT REMOVE THIS TRANSPARENT SLICE
             */
            values.add(SliceValue(75f, Color.TRANSPARENT))
            /** **/

            values.add(SliceValue(actualFillPercent, context.getColor(chartColor)))
            values.add(SliceValue(100f - actualFillPercent, context.getColor(R.color.gray)))

            /**
             * DO NOT REMOVE THIS TRANSPARENT SLICE
             */
            values.add(SliceValue(25f, Color.TRANSPARENT))
            /** **/

            val data = PieChartData(values)
            data.setHasCenterCircle(true)
            data.centerCircleScale = 0.7f // Adjust the size of the center circle

            // Customize the appearance of the chart
            data.slicesSpacing = 2 // Remove spacing between the slices
            data.setHasLabels(false) // Hide labels
            data.setHasLabelsOnlyForSelected(false) // Hide labels for selected slices

            return TachometerData(data, actualFillPercent)
        }

        fun getSideBySideBarChartData(
            context: Context,
            pastData: List<Float>,
            presentData: List<Float>,
            labels: List<AxisValue>? = null,
            colors: Colors = Colors()
        ): ColumnData? {
            if (pastData.isEmpty() || presentData.isEmpty() || presentData.size > pastData.size) {
                Log.w(_tag, "No data to display, returning void from generateData()")
                return null
            }

            val getCurrentSubColumnWithColor: (Int) -> SubcolumnValue = { i ->
                val presentDataValue = if (i >= presentData.size) 0f else presentData[i]
                val color =
                    if (presentDataValue == 0f) colors.dull
                    else {

                        val isLastIndexForPresentData = i == (presentData.size - 1)
                        val presentIsMoreOrEqualPast = presentData[i] >= pastData[i]

                        if (isLastIndexForPresentData) colors.active
                        else if (presentIsMoreOrEqualPast) colors.success
                        else colors.failure
                    }

                SubcolumnValue(presentDataValue, context.getColor(color))
            }

            val numColumns = pastData.size
            val columns: MutableList<Column> = ArrayList()
            var values: MutableList<SubcolumnValue?>
            for (index in 0 until numColumns) {
                values = ArrayList()
                values.add(SubcolumnValue(pastData[index], context.getColor(R.color.gray)))
                values.add(getCurrentSubColumnWithColor(index))

                columns.add(Column(values).setHasLabelsOnlyForSelected(true))
            }

            val data = ColumnChartData(columns)
            data.axisXBottom = Axis().apply {
                textColor = context.getColor(R.color.black)
                textSize = 12
                setHasLines(true)
            }
            data.axisXBottom.values = mutableListOf<AxisValue>()
            labels?.forEach { data.axisXBottom.values.add(it) }

            data.axisYLeft = Axis().apply {
                textColor = context.getColor(R.color.black)
                textSize = 12
                setHasLines(true)
            }

            val columnData = ColumnData(data, null)
            columnData.setChartPadding(pastData, presentData)

            return columnData
        }

        fun getSideBySideBarChartDataWithPreview(
            context: Context,
            pastData: List<Float>,
            presentData: List<Float>,
            labels: List<AxisValue>? = null,
            colors: Colors = Colors()
        ): ColumnData? {
            val columnData =
                getSideBySideBarChartData(context, pastData, presentData, labels, colors)
                    ?: return null

            // prepare preview data, is better to use separate deep copy for preview chart.
            // set color to grey to make preview area more visible.
            val previewData = ColumnChartData(columnData.mainChartData)
            for (column in previewData.columns) {
                for (value in column.values) {
                    value.color = ChartUtils.DEFAULT_DARKEN_COLOR
                }
            }

            columnData.setPreviewData(previewData)

            return columnData
        }
    }
}