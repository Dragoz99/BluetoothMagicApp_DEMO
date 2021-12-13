package com.example.bluetoothmagicapp_demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class DeviceListActivity : AppCompatActivity() {
    lateinit var listPairedDevice :ListView //liste
    lateinit var listAviableDevice :ListView //liste

    lateinit var adapterPairDevice: ArrayAdapter<String>
    lateinit var adapterAviableDevice: ArrayAdapter<String>
    lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        init()
    }

    private fun init(){

        // inizializzazione
        listPairedDevice = findViewById(R.id.list_paired_device)
        listAviableDevice = findViewById(R.id.list_aviable_device)

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


    }
}