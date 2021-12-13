package com.example.bluetoothmagicapp_demo

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val LOCATION_PERMISSION_REQUEST = 101


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
                checkPermission()

                return true
            }
            R.id.menu_bluetooth_on ->{
                Toast.makeText(this,"Clicked bluetooth on", Toast.LENGTH_LONG).show()
                enableBluetooth()
                return true
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
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
            startActivity(intent)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == LOCATION_PERMISSION_REQUEST){
            if(grantResults.size >0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                var intent = Intent(this,DeviceListActivity::class.java)
                startActivity(intent)
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
    }


}