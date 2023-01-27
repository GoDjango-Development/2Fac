package com.smartestidea.a2fac.ui.view

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.android.material.snackbar.Snackbar
import com.smartestidea.a2fac.R
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
    private val bindingAD: AdNewConfigurationBinding by lazy { AdNewConfigurationBinding.inflate(layoutInflater) }
    private var alertDialog: Dialog?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        screenSplash.setKeepOnScreenCondition{false}
        setContentView(binding.root)
        requestSmsPermission()

        tfpViewModel.onCreate()
        createAlertDialog()

        

        tfpViewModel.configurations.observe(this){configs->
            Log.i("id-type-main",configs.joinToString { " " })
                tfpViewModel.run()
            val configurationAdapter = ConfigurationAdapter(configs, this, tfpViewModel)
            binding.dragList.adapter = configurationAdapter
        }
        tfpViewModel.loading.observe(this){
            binding.progress.isVisible = it
            binding.dragList.isVisible = !it
        }

        binding.dragList.apply {
            layoutManager =  LinearLayoutManager(this@MainActivity)
            orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
            orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_UNCONSTRAINED_DRAGGING
            dragListener = onItemDragListener
            swipeListener = onItemSwipeListener
            behindSwipedItemIconDrawableId = R.drawable.ic_delete_button_svgrepo_com
            behindSwipedItemIconMargin = 0.5F
            behindSwipedItemBackgroundColor = ContextCompat.getColor(context, R.color.red_error)
            reduceItemAlphaOnSwiping = true
        }
        binding.btnNewConfig.setOnClickListener {
            addConfiguration()
        }

    }
    private fun addConfiguration() {
        alertDialog?.show()
    }
    private fun createAlertDialog(){
        alertDialog = Dialog(this)
        alertDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(bindingAD.root)
            bindingAD.apply {
                btnOk.setOnClickListener {
                    val name = etName.text.toString()
                    if(name.isNotEmpty()){
                        tfpViewModel.configurations.value?.let {
                            when(rgType.checkedRadioButtonId){
                                rbDown.id->ConfigurationReceiver(0,name,"",0,"key :: none","","",
                                    it.size,1000,false,
                                    emptyList<String>().toMutableList(), emptyList<String>().toMutableList()
                                )
                                else -> ConfigurationSender(0,name,"",0,"key :: none","","",it.size,1000,false,
                                    "",""
                                )
                            }
                        }?.let {
                            tfpViewModel.insert(it)
                        }
                    }else
                        Snackbar.make(binding.coordinator, resources.getString(R.string.insert_name), Snackbar.LENGTH_LONG).show()

                    cancel()
                }
            }
            bindingAD.btnCancel.setOnClickListener { cancel() }

        }
    }
    private val onItemSwipeListener = object : OnItemSwipeListener<String> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: String): Boolean {
            // Handle action of item swiped
            // Return false to indicate that the swiped item should be removed from the adapter's data set (default behaviour)
            // Return true to stop the swiped item from being automatically removed from the adapter's data set (in this case, it will be your responsibility to manually update the data set as necessary)
            Log.e("itemIdSwipe",item)
            tfpViewModel.delete(item.toInt(),binding.coordinator,this@MainActivity)

            return false
        }
    }

    private val onItemDragListener = object : OnItemDragListener<String> {
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: String) {
            // Handle action of item being dragged from one position to another
            Log.e("itemIdDrag", item)

        }

        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: String) {
            // Handle action of item dropped
            Log.e("itemDropped", item)
            val ids: MutableList<Int> = tfpViewModel.configurations.value?.map { it.id } as MutableList<Int>
            ids.removeAt(initialPosition)
            ids.add(finalPosition,item.toInt())

            Log.e("indexes",ids.map { "$it: ${ids.indexOf(it)}"}.toString())
            ids.forEach {
                tfpViewModel.updatePos(it,ids.indexOf(it))
            }
        }
    }

    //Permissions
    private fun requestSmsPermission() {
        val permissions = listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissions.forEach {
            val grant = ContextCompat.checkSelfPermission(this, it)
            if (grant != PackageManager.PERMISSION_GRANTED) {
                val permissionList = arrayOfNulls<String>(1)
                permissionList[0] = it
                ActivityCompat.requestPermissions(this, permissionList, 1)
            }
        }
    }
}
