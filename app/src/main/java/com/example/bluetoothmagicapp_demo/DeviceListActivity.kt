package com.example.bluetoothmagicapp_demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible

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

        // inizializzazione
        listPairedDevice = findViewById(R.id.list_paired_device)
        listAviableDevice = findViewById(R.id.list_aviable_device)

        progressScanDevices = findViewById(R.id.progress_scan_devices)

        adapterPairDevice = ArrayAdapter(this, R.layout.device_list_item)
        adapterAviableDevice = ArrayAdapter(this, R.layout.device_list_item)


        //setAdapter
        listPairedDevice.setAdapter(adapterPairDevice)
        listAviableDevice.setAdapter(adapterAviableDevice)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var pairedDevice : Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

        if(pairedDevice != null && pairedDevice.size >0){
            for(device: BluetoothDevice in pairedDevice){
                adapterPairDevice.add(device.name + "\n" + device.address)
            }
        }


        //qui !!



        // INTENT FILTER
        //intent filter to descovery devices
        //and register
        var intentFilter_AF = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver,intentFilter_AF)
        var intentFilter_ADF = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver,intentFilter_ADF)
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
            else->
            Toast.makeText(this, "scan device clicked", Toast.LENGTH_SHORT).show()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun scanDevices(){
      // progressScanDevices.visibility(View.VISIBLE)
        progressScanDevices.isVisible = true
        adapterAviableDevice.clear()

        Toast.makeText(this,"Scan Started",Toast.LENGTH_SHORT).show()
        if(bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()

    }
    //================================================
    // LISTENER
    //================================================
    /*versione 1 : OLD
   private val bluetoothDeviceListener: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            var action : String? = intent?.action

            if(BluetoothDevice.ACTION_FOUND.equals(action)){ //azione trovato !!!!
                val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if(device?.bondState != BluetoothDevice.BOND_BONDED){
                    adapterAviableDevice.add(device?.name +"\n" +device?.address)

                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                progressScanDevices.isVisible=false
                if(adapterAviableDevice.count == 0){
                    Toast.makeText(context, "No new Devices found", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Click on the device to... ", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }*/
    //versione 2 : NEW
    private val mReceiver = object : BroadcastReceiver() {
        override
        fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from
                // the Intent
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it,
                // because it's been listed already
                if (device?.bondState != BluetoothDevice.BOND_BONDED) {
                    adapterAviableDevice!!.add(device?.name + "\n" + device?.address)
                }
                // When discovery is finished, change the
                // Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                setTitle("Select Device")
                if (adapterAviableDevice!!.count == 0) {
                    val noDevices = "No device"
                    adapterAviableDevice!!.add(noDevices)
                }
            }
        }
    }
}