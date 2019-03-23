package com.almondrush.telegramquest.legend

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.almondrush.telegramquest.R
import com.almondrush.telegramquest.dto.Line
import java.text.SimpleDateFormat
import java.util.Date

internal class ChartPointerInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val POINTER_INFO_DATE_PATTERN = "EEE, MMM d"
    }

    init {
        visibility = View.GONE
    }

    private val pointerInfoView = LayoutInflater.from(context).inflate(R.layout.pointer_info, this, false)
    private val pointerInfoDate: TextView = pointerInfoView.findViewById(R.id.pointer_info_date)
    private val itemContainer: ViewGroup = pointerInfoView.findViewById(R.id.pointer_info_item_container)
    private var pointerInfoItems: List<PointerInfoViewHolder> = emptyList()

    init {
        addView(pointerInfoView)
    }

    val dateFormat = SimpleDateFormat(POINTER_INFO_DATE_PATTERN, context.resources.configuration.locale)

    fun setLines(lines: List<Line>) {
        itemContainer.removeAllViews()
        pointerInfoItems = lines.map { line ->
            PointerInfoViewHolder(context, itemContainer, line.name, line.color)
        }
        requestLayout()
    }

    fun setupWith(view: ChartXAxisPointerView) {
        view.listener = object : ChartXAxisPointerView.Listener {
            override fun onSelectedPointChanged(time: Long, values: List<Long>) {
                pointerInfoDate.text = dateFormat.format(Date(time))
                pointerInfoItems.forEachIndexed { index, item ->
                    item.setValue(values[index])
                }
                visibility = View.VISIBLE
            }

            override fun onSelectionRemoved() {
                visibility = View.GONE
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutParams = layoutParams.apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    private class PointerInfoViewHolder(context: Context, parent: ViewGroup, name: String, color: Int) {
        private val view = LayoutInflater.from(context).inflate(R.layout.pointer_info_item, parent, false)
        private val nameView: TextView = view.findViewById(R.id.pointer_info_item_name)
        private val valueView: TextView = view.findViewById(R.id.pointer_info_item_value)

        init {
            nameView.text = name
            nameView.setTextColor(color)
            valueView.setTextColor(color)
            parent.addView(view)
        }

        fun setValue(value: Long) {
            valueView.text = value.toString()
        }
    }
}