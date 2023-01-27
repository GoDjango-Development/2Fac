package com.smartestidea.a2fac.ui.adapter.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smartestidea.a2fac.databinding.CvFolderBinding

class FoldersAdapter(
    private val folders: MutableList<String>,
    private val onDelete: (Int) -> Unit,
    private val onUpdate:(Int,String) ->Unit
):RecyclerView.Adapter<FoldersAdapter.FoldersVH>() {
    inner class FoldersVH(val binding: CvFolderBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersVH = FoldersVH(
        CvFolderBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: FoldersVH, position: Int) {
        with(holder){
            with(holder.binding) {
                with(folders[position]) {
                    etFolderName.setText(this)
                    btnDelete.setOnClickListener {
                       onDelete(adapterPosition)
                    }
                    etFolderName.addTextChangedListener {
                        onUpdate(adapterPosition, it.toString())
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = folders.size


}