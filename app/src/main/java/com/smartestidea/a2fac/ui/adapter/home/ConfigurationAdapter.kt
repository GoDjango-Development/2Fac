package com.smartestidea.a2fac.ui.adapter.home

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bitvale.switcher.SwitcherC
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.google.android.material.divider.MaterialDivider
import com.smartestidea.a2fac.ui.view.DetailActivity
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.ui.viewmodel.settings.TFPViewModel

class ConfigurationAdapter(
    private val configurations: MutableList<Configuration> = mutableListOf(),
    private val context: Context,
    private val tfpViewModel: TFPViewModel,
)
    : DragDropSwipeAdapter<String, ConfigurationAdapter.ViewHolder>(configurations.map { it.name }) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.tvName)
        val dragIcon: ImageView = itemView.findViewById(R.id.ivDrag)
        val switchConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.clSwitch)
        val switchCard:CardView = itemView.findViewById(R.id.cvSwitch)
        val switchTextView: TextView = itemView.findViewById(R.id.tvSwitch)
        val layout:ConstraintLayout = itemView.findViewById(R.id.contentLayout)
        val parent:ConstraintLayout = itemView.findViewById(R.id.parent)
        val divider:MaterialDivider = itemView.findViewById(R.id.divider)
    }

    override fun getViewHolder(itemView: View) = ViewHolder(itemView)

    override fun getItemCount(): Int = configurations.size


    override fun onBindViewHolder(item: String, viewHolder: ViewHolder, position: Int) {
        // Here we update the contents of the view holder's views to reflect the item's data
        with(viewHolder){
            with(configurations[position]){
                if(position != itemCount-1) {
                    val tintColor = ContextCompat.getColor(context,if(isOn) R.color.green_success else R.color.red_error)
                    divider.isVisible = true
                    switchConstraintLayout.isVisible=true
                    dragIcon.isVisible = true
                    itemText.text = name
                    switchCard.setCardBackgroundColor(tintColor)
                    switchTextView.text = context.resources.getString( if(isOn) R.string.on else R.string.off )
                    switchTextView.setTextColor(tintColor)
                    parent.setOnClickListener {
                        val config = this
                        Intent(context, DetailActivity::class.java).apply {
                            putExtra("id", name)
                            Log.i("id-type-i", "$config - ${config.getType()}")
                            putExtra("type", config.getType().toString())
                            context.startActivity(this)
                        }
                    }
                }else{
                    itemText.text = name
                    itemText.alpha = 0.3f
                    switchConstraintLayout.isVisible=false
                    dragIcon.isVisible = false
                    divider.isVisible = false
                }
            }
        }

    }

    override fun getViewToTouchToStartDraggingItem(item: String, viewHolder: ViewHolder, position: Int): View {
        // We return the view holder's view on which the user has to touch to drag the item
        return viewHolder.dragIcon
    }
}