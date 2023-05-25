package com.smartestidea.a2fac.ui.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.core.RecyclerItemClickListener
import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.data.model.ConfigurationSender
import com.smartestidea.a2fac.databinding.ActivityDetailBinding
import com.smartestidea.a2fac.databinding.BtmsheetKeySelectorBinding
import com.smartestidea.a2fac.ui.adapter.detail.FoldersAdapter
import com.smartestidea.a2fac.ui.adapter.detail.KeysAdapter
import com.smartestidea.a2fac.ui.viewmodel.settings.TFPViewModel
import dagger.hilt.android.AndroidEntryPoint


//ipServer,portServer,publicKey,hash,len,protocol,protoHandler
@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {
    private val binding:ActivityDetailBinding by lazy{ ActivityDetailBinding.inflate(layoutInflater)}
    private val tfpViewModel:TFPViewModel by viewModels()
    private lateinit var currentKey:String
    private lateinit var adapter:FoldersAdapter
    private lateinit var foldersList:MutableList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        tfpViewModel.loading.observe(this){
            binding.progress.isVisible = it
            binding.btnSave.isVisible = !it
        }

//        binding.btnSmsSender.setOnClickListener {
//            SmsSender("+5355096521","THIS REALLY WORK")
//        }



        val bundle = intent.extras
        val idIntent = bundle?.getString("id")
        val typeIntent = bundle?.getString("type")

        tfpViewModel.onCreate()

        tfpViewModel.keys.observe(this) { keys ->
//            binding.fabExistingKey.setOnClickListener {
//                selectKey(keys)
//            }
        }
            tfpViewModel.configurations.observe(this){configs->
                tfpViewModel.run()
                val config: Configuration = configs.filter { it.name  == idIntent && (
                        if(typeIntent == TYPE.RECEIVER.toString()) {
                            Log.i("id-type-c",typeIntent)
                            it is ConfigurationReceiver
                        }else{
                            it is ConfigurationSender
                        }
                        )}[0]
                currentKey = config.publicKey
                println(currentKey)

                binding.apply {
                    switchCompat.setChecked(config.isOn,true)
                    switchCompat.setOnCheckedChangeListener { checked ->
                        val type = config.getType()
                        if(checked) {
                            tfpViewModel.connect(config.name,coordinator,this@DetailActivity,type){isOn->
                                switchCompat.setChecked(isOn)
                            }
                            tfpViewModel.run()
                        } else {
                            tfpViewModel.of(config.name, type)
                            tfpViewModel.run()
                        }
                    }
                    tvConfName.text = config.name
                    etIpServe.setText(config.ipServer)
                    etPortServe.setText(config.portServe.toString())
                    etHash.setText(config.hash)
                    etProtocol.setText(config.protocol)
                    tvKey.text = config.publicKey
                    //senders fields
                    if(config is ConfigurationSender) {
                        etKeyword.setText(config.keyword)
                        etSafeFolder.setText(config.safeFolder)
                        rvFolders.isVisible = false
                        fabAddFile.isVisible = false
                    }else{
                    //receiver fields
                        cardKey.isVisible = false
                        cardSafeFolder.isVisible = false
                        rvFolders.layoutManager = LinearLayoutManager(this@DetailActivity)
                        foldersList = (config as ConfigurationReceiver).safeFolders.toMutableList()
                        adapter = FoldersAdapter(
                            folders = foldersList
                            , { pos -> onDeleteItem(pos) }
                            , {pos, string -> onUpdateItem(pos, string)})
                        rvFolders.adapter = adapter
                        fabAddFile.setOnClickListener {addNewFolder()}
                    }
                    etInterval.setText(config.interval.toString())
//                    tvKey.text = config.publicKey.substringBefore(" :: ")
                    btnSave.setOnClickListener {
                        try {
                            if (etInterval.text?.toString()?.toInt()!! >= 1) {
                                updateOrCreateConfig(config)
                                Snackbar.make(
                                    coordinator,
                                    resources.getString(R.string.success),
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setBackgroundTint(
                                        ContextCompat.getColor(
                                            this@DetailActivity,
                                            R.color.green_success
                                        )
                                    )
                                    .show()

                            } else {
                                Snackbar.make(coordinator, resources.getString(R.string.wrong_interval), Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(ContextCompat.getColor(this@DetailActivity, R.color.red_error))
                                    .show()
                            }
                        }catch(e:Exception){
                            Snackbar.make(coordinator, resources.getString(R.string.wrong_interval), Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(ContextCompat.getColor(this@DetailActivity, R.color.red_error))
                                .show()
                        }
                    }
                    binding.etInterval.setOnEditorActionListener { _, actionId, event ->
                        if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                          updateOrCreateConfig(config)
                        }
                        false
                    }
//                    fabNewKey.setOnClickListener {
//
//                    }
                    fabPasteKey.setOnClickListener {
                        chooseKeyFromClipboard()
                    }


                }
            }


    }

    private fun chooseKeyFromClipboard() {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val clipData: ClipData? = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val key = clipData.getItemAt(0).text
                // Establecer el texto en la vista correspondiente, por ejemplo:
                binding.tvKey.text = key
            }
        }
    }

    private fun addNewFolder() {
        foldersList.add("")
        adapter.notifyItemInserted(foldersList.size-1)
    }

    private fun onUpdateItem(pos: Int,value:String) {
          foldersList[pos] = value
    }

    private fun onDeleteItem(pos:Int) {
        foldersList.removeAt(pos)
        adapter.notifyItemRemoved(pos)
    }


    private fun updateOrCreateConfig(config:Configuration) {
        binding.apply {
            tfpViewModel.update(
                if(config is ConfigurationReceiver)
                    ConfigurationReceiver(
                        config.name,
                        etIpServe.text.toString(),etPortServe.text.toString().toInt(),
//                        if(currentKey.isNullOrEmpty()) config.publicKey else currentKey!!
                        tvKey.text.toString()
                        ,etHash.text.toString(),
                        etProtocol.text.toString(),config.pos,etInterval.text.toString().toInt(),
                        false,foldersList,config.alreadyDownloads)
                else
                    ConfigurationSender(
                        config.name,
                        etIpServe.text.toString(),etPortServe.text.toString().toInt(),
//                        if(currentKey.isNullOrEmpty()) config.publicKey else currentKey!!
                        tvKey.text.toString()
                        ,etHash.text.toString(),
                        etProtocol.text.toString(),config.pos,etInterval.text.toString().toInt(),
                        false,etKeyword.text.toString(),etSafeFolder.text.toString())
            )
        }
    }


    private fun chooseKeyFromFile(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type ="text/plain"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try{
            startActivityForResult(Intent.createChooser(intent,resources.getString(R.string.select_file)),100)
        }catch (e:Exception){
            Snackbar.make(binding.coordinator,resources.getString(R.string.install_file_manager),Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun selectKey(keys:List<String>){
        val bottomSheetDialog = BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog)
        val bindingBS = BtmsheetKeySelectorBinding.inflate(LayoutInflater.from(this))
        val recycler = bindingBS.rvKeys
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = KeysAdapter(keys, this)
        recycler.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                recycler,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        currentKey = keys[position]
//                        binding.tvKey.text = keys[position].substringBefore(" :: ")
                    }

                    override fun onLongItemClick(view: View?, position: Int) {
                        // do whatever
                    }
                })
        )
        bottomSheetDialog.apply {
            setContentView(bindingBS.root)
            show()
        }
    }

}