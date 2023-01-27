package com.smartestidea.a2fac.ui.adapter.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.databinding.ItemKeyBinding

class KeysAdapter(
    private val keys:List<String>,
    private val context: Context
) : RecyclerView.Adapter<KeysAdapter.KeysVH>() {

    inner class KeysVH(val binding:ItemKeyBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeysVH = KeysVH(ItemKeyBinding.inflate(
        LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: KeysVH, position: Int) {
        holder.binding.tvKey.text = keys[position].ifEmpty {
            context.resources.getString(
                R.string.key_none)
        }
    }

    override fun getItemCount(): Int = keys.size

}