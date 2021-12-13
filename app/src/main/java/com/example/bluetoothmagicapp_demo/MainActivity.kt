package com.example.bluetoothmagicapp_demo

import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

class MainActivity : AppCompatActivity() {


    lateinit var bluetoothAdapter: BluetoothAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //inizializza il bluetooth
        initBluetooth()
    }

    fun initBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            Toast.makeText(this, "No bluetooth found", Toast.LENGTH_SHORT)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_searc_device -> {
                Toast.makeText(this,"Clicked Search Devies", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.menu_bluetooth_on ->{
               // Toast.makeText(this,"Clicked bluetooth on", Toast.LENGTH_LONG).show()
                enableBluetooth()
                return true
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }



    fun enableBluetooth(){
        if(bluetoothAdapter.isEnabled){
            Toast.makeText(this, "Bluetooth e' gia abilitato", Toast.LENGTH_SHORT).show()
        }else{
            bluetoothAdapter.enable()
        }
    }
}