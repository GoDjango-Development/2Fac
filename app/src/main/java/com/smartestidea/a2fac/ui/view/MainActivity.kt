package com.smartestidea.a2fac.ui.view

import android.Manifest
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.android.material.snackbar.Snackbar
import com.permissionx.guolindev.PermissionX
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.core.ServerManager.serverListener
import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.data.model.ConfigurationSender
import com.smartestidea.a2fac.databinding.ActivityMainBinding
import com.smartestidea.a2fac.databinding.AdNewConfigurationBinding
import com.smartestidea.a2fac.ui.adapter.home.ConfigurationAdapter
import com.smartestidea.a2fac.ui.viewmodel.settings.TFPViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val tfpViewModel: TFPViewModel by viewModels()
    private val bindingAD by lazy { AdNewConfigurationBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {

        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)

        screenSplash.setKeepOnScreenCondition { false }
        setContentView(binding.root)

        requestPermissionsX()

        tfpViewModel.onCreate()

        tfpViewModel.configurations.observe(this){configs->
            tfpViewModel.run()

            val adapter = ConfigurationAdapter((configs + listOf(
                ConfigurationSender(resources.getString(R.string.create_more),"",0,"","","", Int.MAX_VALUE,0,false,"","")
            )).toMutableList(), this, tfpViewModel)
            binding.dragList.adapter  = adapter

            binding.dragList.dragListener = onItemDragListener(configs)
            binding.dragList.swipeListener = onItemSwipeListener()


            //drag & drop
            binding.dragList.apply {
                layoutManager =  LinearLayoutManager(this@MainActivity)
                orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
                orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_UNCONSTRAINED_DRAGGING
                behindSwipedItemIconDrawableId  = R.drawable.ic_delete_button_svgrepo_com
                behindSwipedItemIconSecondaryDrawableId = R.drawable.ic_delete_button_svgrepo_com
                behindSwipedItemIconMargin = 0.5F
                behindSwipedItemBackgroundSecondaryColor = ContextCompat.getColor(context, R.color.red_error)
                behindSwipedItemBackgroundColor = ContextCompat.getColor(context, R.color.red_error)
                reduceItemAlphaOnSwiping = true
            }

            //dialog
            binding.btnNewConfig.setOnClickListener {
                addConfiguration(configs)
            }

        }
        tfpViewModel.loading.observe(this){
            binding.progress.isVisible = it
            binding.dragList.isVisible = !it
        }

    }

    private fun requestPermissionsX() {
        PermissionX.init(this)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
//                    Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                } else {
//                    Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                    Snackbar.make(binding.coordinator,resources.getString(R.string.grant_permission), Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(this,R.color.red_error))
                        .show()
                }
            }
    }


    private fun addConfiguration(list: List<Configuration>) {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            if(bindingAD.root.parent != null){ (bindingAD.root.parent as ViewGroup).removeView(bindingAD.root) }
            setContentView(bindingAD.root)
            bindingAD.apply {
                var parent:ViewGroup? = null
                if (btnOk.parent != null) {
                    parent = (btnOk.parent as ViewGroup)
                    parent.removeView(btnOk) // <- fix
                }
                btnOk.setOnClickListener {
                    val name = etName.text.toString()
                    if(name.isNotEmpty()){
                        if(!(list.map { it.name }.contains(name))){
                        list.let {
                            when(rgType.checkedRadioButtonId){
                                rbDown.id-> ConfigurationReceiver(name,"",0,"","","",
                                    it.size,1,false,
                                    emptyList<String>().toMutableList(), emptyList<String>().toMutableList()
                                )
                                else -> ConfigurationSender(name,"",0,"","","",it.size,1,false,
                                    "",""
                                )
                            }
                        }.let { config->
                            tfpViewModel.insert(config)
                            tfpViewModel.onCreate()
                        }
                        }else
                            showInsertNameSB()
                    }else
                        showInsertNameSB()

                    cancel()
                }
                parent?.addView(btnOk)

            }
            bindingAD.btnCancel.setOnClickListener { cancel() }
            show()
        }
    }
    private fun showInsertNameSB() = Snackbar.make(binding.coordinator, resources.getString(R.string.insert_name), Snackbar.LENGTH_LONG)
        .setBackgroundTint(ContextCompat.getColor(this,R.color.red_error)).show()

    private fun onItemSwipeListener() = object : OnItemSwipeListener<String> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: String): Boolean {
            // Handle action of item swiped
            // Return false to indicate that the swiped item should be removed from the adapter's data set (default behaviour)
            // Return true to stop the swiped item from being automatically removed from the adapter's data set (in this case, it will be your responsibility to manually update the data set as necessary)
            Log.i("POSITION",position.toString())
            tfpViewModel.configurations.value?.let{
                if(position< it.size)
                    tfpViewModel.delete(it[position].name,binding.coordinator,this@MainActivity)
            }
            return true
        }
    }

    private fun onItemDragListener(list:List<Configuration>) = object : OnItemDragListener<String> {
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: String) {
            // Handle action of item being dragged from one position to another
            Log.e("itemIdDrag", item)

        }

        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: String) {
            // Handle action of item dropped
            Log.e("itemDropped", item)
            val ids: MutableList<String> = list.map { it.name } as MutableList<String>
            ids.removeAt(initialPosition)
            ids.add(finalPosition,item)

            Log.e("indexes",ids.map { "$it: ${ids.indexOf(it)}"}.toString())
            ids.forEach {
                tfpViewModel.updatePos(it,ids.indexOf(it))
            }
        }
    }

    override fun onResume() {
        tfpViewModel.onCreate()
        super.onResume()
    }
}
