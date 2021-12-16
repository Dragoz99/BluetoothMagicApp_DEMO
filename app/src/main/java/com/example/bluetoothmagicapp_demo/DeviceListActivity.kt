package com.example.bluetoothmagicapp_demo

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import android.widget.TextView
/*
*   APPUNTI:
*
* QUESTA CALSSE SEVER PER SCOPRIRE I DIPSOSITIVI BLUETOOTH ACCESSI
*
*   1) Query paired devices
*   2) Discover devices
*
* */



class DeviceListActivity : AppCompatActivity() {
    lateinit var progressScanDevices :ProgressBar

    private lateinit var listPairedDevice :ListView //liste
    private lateinit var listAviableDevice :ListView //liste

    private lateinit var adapterPairDevice: ArrayAdapter<String>
    private lateinit var adapterAviableDevice: ArrayAdapter<String>
    private lateinit var bluetoothAdapter: BluetoothAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        init()
    }

    private fun init(){
//=================================================================================
        // inizializzazione
        listPairedDevice = findViewById(R.id.list_paired_device)
        listAviableDevice = findViewById(R.id.list_aviable_device)
        progressScanDevices = findViewById(R.id.progress_scan_devices)

        adapterPairDevice = ArrayAdapter(this, R.layout.device_list_item)
        adapterAviableDevice = ArrayAdapter(this, R.layout.device_list_item)
//------------------------------------------------------------------------------------
        //setAdapter
        listPairedDevice.setAdapter(adapterPairDevice)
        listAviableDevice.setAdapter(adapterAviableDevice)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

//------------------------------------------------------------------------------------
        //Query paired devices
        //alla scoperta dei dispositivi
        var pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevice?.forEach{ device ->
            val deviceName = device.name        // device Name
            val deviceHardwareAddress = device.address // MAC address
            adapterAviableDevice.add("$deviceName \n $deviceHardwareAddress")
        }

        //vecchia versione
        /* var pairedDevice : Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

         if(pairedDevice != null && pairedDevice.size >0){
             for(device: BluetoothDevice in pairedDevice){
                 adapterPairDevice.add(device.name + "\n" + device.address)
             }
         }*/

//------------------------------------------------------------------------------------
        // Register for broadcasts when a device is discovered.
        val intentFilter_AF = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothDeviceListener,intentFilter_AF)
        val intentFilter_ADF = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(bluetoothDeviceListener,intentFilter_ADF)
//------------------------------------------------------------------------------------

        //trigger sul click del dispositivo scoperto
        listAviableDevice.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val info = (view as TextView).text.toString()
            val address = info.substring(info.length -17)

            Log.d("Address", address);

            var intent = Intent()
            intent.putExtra("deviceAddress",address)
            setResult(Activity.RESULT_OK, intent)
            finish()

        })
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_device_list,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_scan_device ->{
                scanDevices()
                return true
            }
            else->{
                return super.onOptionsItemSelected(item) //funzione ricorsiva
                Toast.makeText(this, "scan device clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scanDevices(){
        progressScanDevices.isVisible = true
        adapterAviableDevice.clear()

        Toast.makeText(this,"Scan Started",Toast.LENGTH_SHORT).show()
        if(bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val bluetoothDeviceListener = object: BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            val action : String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    adapterAviableDevice.add("$deviceName \n $deviceHardwareAddress")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                    progressScanDevices.visibility = View.GONE
                    if (adapterAviableDevice.count == 0) {
                        Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Click on the device to start the chat", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }

    }

    //VERSIONE VECCHIA
   /* private val bluetoothDeviceListener = object : BroadcastReceiver() {
        override
        fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action //String

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from
                // the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it,
                // because it's been listed already
                if (device?.bondState != BluetoothDevice.BOND_BONDED) {
                    adapterAviableDevice.add(device?.name + "\n" + device?.address)
                }
                // When discovery is finished, change the
                // Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                //setProgressBarIndeterminateVisibility(false)
                progressScanDevices.visibility = View.GONE
                if (adapterAviableDevice.count == 0) {
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Click on the device to start the chat", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothDeviceListener)
    }
}