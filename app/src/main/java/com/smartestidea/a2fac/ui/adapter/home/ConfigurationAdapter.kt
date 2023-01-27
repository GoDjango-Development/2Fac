package com.smartestidea.a2fac.ui.adapter.home

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bitvale.switcher.SwitcherC
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.smartestidea.a2fac.ui.view.DetailActivity
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.ui.viewmodel.settings.TFPViewModel

class ConfigurationAdapter(
    private val configurations: List<Configuration> = emptyList(),
    private val context: Context,
    private val tfpViewModel: TFPViewModel
)
    : DragDropSwipeAdapter<String, ConfigurationAdapter.ViewHolder>(configurations.map { it.id.toString() }) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.tvName)
        val dragIcon: ImageView = itemView.findViewById(R.id.ivDrag)
        val switchCompat: SwitcherC = itemView.findViewById(R.id.switchCompat)
        val layout:ConstraintLayout = itemView.findViewById(R.id.contentLayout)
    }

    override fun getViewHolder(itemView: View) = ViewHolder(itemView)

    override fun onBindViewHolder(item: String, viewHolder: ViewHolder, position: Int) {
        // Here we update the contents of the view holder's views to reflect the item's data
        with(viewHolder){
            with(configurations[position]){
                itemText.text = name
                switchCompat.setChecked(isOn,true)
                switchCompat.setOnCheckedChangeListener { checked ->
                    val type = getType()
                    if(checked) tfpViewModel.on(item.toInt(),type) else tfpViewModel.of(item.toInt(),type)
                }
                layout.setOnClickListener {
                    val config = this
                    Intent(context, DetailActivity::class.java).apply {
                        putExtra("id",id)
                        Log.i("id-type-i","$config - ${config.getType()}")
                        putExtra("type",config.getType().toString())
                        context.startActivity(this)
                    }
                }
            }
        }

    }

    override fun getViewToTouchToStartDraggingItem(item: String, viewHolder: ViewHolder, position: Int): View {
        // We return the view holder's view on which the user has to touch to drag the item
        return viewHolder.dragIcon
    }
}