package com.example.bluetoothmagicapp_demo

import android.bluetooth.BluetoothAdapter
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.session.PlaybackState.STATE_NONE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bluetoothmagicapp_demo.ComunicationUtility.Companion.STATE_CONNECTED
import com.example.bluetoothmagicapp_demo.ComunicationUtility.Companion.STATE_CONNECTING
import com.example.bluetoothmagicapp_demo.ComunicationUtility.Companion.STATE_LISTEN
import kotlinx.android.synthetic.main.activity_main.*

open class MainActivity : AppCompatActivity() {


    private lateinit var listMainChat : ListView

    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val LOCATION_PERMISSION_REQUEST = 101
    private val SELECT_DEVICE = 102
    private lateinit var adapterMain : ArrayAdapter<String>
    private lateinit var context: Context
    private val TAG = "MainActivity"

    private lateinit var comunicationUtility : ComunicationUtility

    companion object {
        val MESSAGGE_STATE_CHANGE = 0
        val MESSAGGE_READ = 1
        val MESSAGGE_WRITE = 2
        val MESSAGGE_DEVICE_NAME = 3
        val MESSAGGE_TOAST = 4
        val TOAST = "toast"
        lateinit var DEVICE_NAME : String
        private lateinit var connectedDevice : String
        var consoleText =String()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        context = this

        //inizializza le cose importanti
        init()

        //inizializza il bluetooth
        initBluetooth()
        comunicationUtility = ComunicationUtility(context,handler)
        Log.d(TAG, "onCreate")

    }

    fun init(){
        listMainChat = findViewById(R.id.list_View_chat)
        adapterMain = ArrayAdapter<String>(this,R.layout.activity_main)
        listMainChat.adapter = adapterMain


        btn_ciao.setOnClickListener {
            Log.d(TAG, "onClickListener btn_ciao")
            var message : String = editText.text.toString()
            if(!message.isEmpty()) {
                editText.setText("")
                comunicationUtility.write(message.toByteArray())
            }

        }


        Log.d(TAG, "init()")

    }

    fun initBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            Toast.makeText(this, "No bluetooth found", Toast.LENGTH_SHORT)
        }
        Log.d(TAG, "initBluetooth()")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        Log.d(TAG, "onCreateOptionsMenu()")
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_searc_device -> {
                Toast.makeText(this,"Clicked Search Devies", Toast.LENGTH_LONG).show()
                checkPermission()

                Console.setText(consoleText + "aperto menu device searc \n")
                return true
            }
            R.id.menu_bluetooth_on ->{
                Toast.makeText(this,"Clicked bluetooth on", Toast.LENGTH_LONG).show()

                // CONSOLE PRINT //
                Console.setText(consoleText + "bluetooth accesso \n")


                enableBluetooth()
                return true
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
        Log.d(TAG, "onOptionsItemSelected()")
        return super.onOptionsItemSelected(item)
    }

    fun checkPermission(){
        // controllo i permessi
      if(bluetoothAdapter.isEnabled){
          val enableBtIntent = Intent(this, DeviceListActivity::class.java)
          startActivity(enableBtIntent) // 1 = REQUEST_ENABLE_BTN
      }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_PERMISSION_REQUEST);
        }else{
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(intent,SELECT_DEVICE)
        }
        Log.d(TAG, "checkPermission()")

    }

   /* override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == LOCATION_PERMISSION_REQUEST){
            if(grantResults.size >0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                var intent = Intent(this,DeviceListActivity::class.java)
                startActivityForResult(intent,SELECT_DEVICE)
            }else{
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Location permission is required. \n Please grant")
                    .setPositiveButton("grant") { dialog, Int ->
                       // Toast.makeText(applicationContext, android.R.string.no, Toast.LENGTH_SHORT).show()
                        checkPermission()}
                    .setNegativeButton("denay"){ dialog, Int ->
                        this@MainActivity.finish()}
                    .create()

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }*/
   override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
       when(requestCode){
           LOCATION_PERMISSION_REQUEST->{
               if(grantResults.size >0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   var intent = Intent(context,DeviceListActivity::class.java)
                   startActivityForResult(intent,SELECT_DEVICE)
               }else{
                   AlertDialog.Builder(context)
                       .setCancelable(false)
                       .setMessage("Location permission is required. \n Please grant")
                       .setPositiveButton("grant") { dialog, Int -> checkPermission()}
                       .setNegativeButton("denay"){ dialog, Int -> this@MainActivity.finish()}
                       .create()
                       .show()
               }
           }else->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
           }
       }
       Log.d(TAG, "onRequestPermissionsResult()")
   }



    fun enableBluetooth(){
        if(!bluetoothAdapter.isEnabled){
            bluetoothAdapter.enable()
        }

        if(bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            val discoveryIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300)
            startActivity(discoveryIntent)
        }
        Log.d(TAG, "enableBluetooth()")
    }


    //server per il click del dispositivo scelto durante il discovery
    /*quando il dipositivo è stato individuato , se lo clicco avvia questa funzione
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode ==SELECT_DEVICE && resultCode == RESULT_OK){
            val address = data?.getStringExtra("deviceAddress")
            comunicationUtility.connect(bluetoothAdapter.getRemoteDevice(address))
            Log.d(TAG, "onActivityResult()")
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private val handler = Handler(object : Handler.Callback {
        override fun handleMessage(message: Message): Boolean {

            when(message.what){
                MESSAGGE_STATE_CHANGE->{
                    when(message.arg1){
                        STATE_NONE ->{
                            setState("Not Connected")
                            Log.d(TAG,"++ STATE_NONE not Connected ++")
                        }
                        STATE_LISTEN ->{
                            setState("Not Connected")
                            Log.d(TAG,"++ STATE_LISTEN not Connected ++")

                        }
                        STATE_CONNECTING ->{
                            setState("Connecting")
                            Log.d(TAG,"++ ConnetING ++")
                        }
                        STATE_CONNECTED ->{
                            setState("Connected $connectedDevice")
                            Log.d(TAG,"++ ConnectED ++")
                        }

                    }
                }
                MESSAGGE_READ -> {

                    var buffer = message.obj as ByteArray
                    var inputBuffer = String(buffer,0,message.arg1)
                    adapterMain.add("$connectedDevice : $inputBuffer")
                   // Toast.makeText(this@MainActivity,"MESSAGGIO INVIATO",Toast.LENGTH_SHORT).show()
                    Console.setText(consoleText +"messaggio inviato $buffer \n")
                    Log.d(TAG,"++ MESSAGGE_READ ++")

                }
                MESSAGGE_WRITE -> {
                    var buffer1 = message.obj as ByteArray
                    var outputStream = String(buffer1)
                   adapterMain.add("me: $outputStream")

                    Console.setText(consoleText +"messaggio ricevuto $buffer1 \n")
                    Log.d(TAG,"++ MESSAGGE_WRITE ++")
                }
                MESSAGGE_DEVICE_NAME ->{
                    connectedDevice = message.data.getString(DEVICE_NAME).toString()
                    Toast.makeText(this@MainActivity,connectedDevice,Toast.LENGTH_SHORT).show()
                    Console.setText(consoleText +"MESSAGGE_DEVICE_NAME = $connectedDevice \n")
                    Log.d(TAG,"++ MESSAGGE_DEVICE_NAME ++")
                }
                MESSAGGE_TOAST -> {
                    Toast.makeText(this@MainActivity,message.data.getString(TOAST),Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"++ MESSAGGE_TOAST ++")
                }
            }
            return false
        }
    })

    private fun setState(subTitle: CharSequence){
        //supportActionBar?.setSubtitle(subTitle)
        supportActionBar?.subtitle = subTitle
        Console.setText(consoleText +"$subTitle")
        Log.d(TAG,"setState()")
    }

     override fun onDestroy(){
        super.onDestroy()
        if(comunicationUtility!=null){
            comunicationUtility.stop()
        }
         Log.d(TAG,"onDestroy")
    }



}