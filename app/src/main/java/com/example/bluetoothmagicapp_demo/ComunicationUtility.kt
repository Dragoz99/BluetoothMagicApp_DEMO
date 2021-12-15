package com.example.bluetoothmagicapp_demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.system.Os.connect
import android.util.Log
import java.io.IOException
import java.util.*


open class ComunicationUtility {
    private var context : Context
    private val handler : Handler

   // private var connectedThread : ConnectThread? = null
    private var acceptThread : AcceptThread? = null


    companion object {  // oggetti static
        private var connectThread: ConnectThread? = null
        private val APP_NAME = "MAGIC_APP"
        lateinit private var bluetoothAdapter: BluetoothAdapter
        private val APP_UUID = UUID.fromString("865d40c2-3b15-4504-945a-4ab448d19122") //ATTENZIONE
        open val STATE_NONE = 0
        open val STATE_LISTEN = 1
        open val STATE_CONNECTING = 2
        open val STATE_CONNECTED = 3
    }
    private var state = STATE_NONE

    constructor(context: Context, handler: Handler){
        this.context=context
        this.handler=handler

        state = STATE_NONE
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    open fun getState(): Int{
        return state
    }
    @Synchronized
    fun setState(state: Int){
        this.state= state
        handler.obtainMessage(MainActivity.MESSAGGE_STATE_CHANGE,state,-1).sendToTarget()

    }
    @Synchronized
    fun start(){
        if(connectThread != null){
            connectThread!!.cancle()
            connectThread = null
        }
        if(acceptThread == null){
            acceptThread = AcceptThread()
            acceptThread!!.start();
        }
        setState(STATE_LISTEN)

    }
    @Synchronized
    fun stop(){
            if(connectThread != null){
                connectThread!!.cancle()
                connectThread = null
            }
        if(acceptThread != null){
            acceptThread!!.cancle()
            connectThread = null
        }
        setState(STATE_LISTEN)

    }
    /*
   * funzione Synchronized per ricevere lo stato dell'oggetto CommuncationUtility
   *
   * manda lo state al handler
   *
   * */
    @Synchronized
    fun connectionFailed(){
        var message: Message = handler.obtainMessage(MainActivity.MESSAGGE_TOAST)
        lateinit var bundle : Bundle
        bundle.putString(MainActivity.TOAST, "Cant connect to device")
        message.data = bundle
        handler.sendMessage(message)

        this@ComunicationUtility.start()
    }
    @Synchronized
    fun connected(device: BluetoothDevice){
        if(connectThread != null){
            connectThread!!.cancle()
            connectThread = null
        }
        val message: Message = handler.obtainMessage(MainActivity.MESSAGGE_DEVICE_NAME)
        var bundle = Bundle()
        bundle.putString(MainActivity.DEVICE_NAME, device.name)
        message.data = bundle
        handler.sendMessage(message)
        setState(STATE_CONNECTED)

    }


    fun connect(device: BluetoothDevice){

        if(state == STATE_CONNECTING){ //pulisci
            connectThread?.cancle()
            connectThread = null
        }
        //crea connessione
        connectThread = ConnectThread(device)
        connectThread!!.start()
        setState(STATE_CONNECTING)


    }
    //*********************************************************************************
   private inner class ConnectThread : Thread{
        private var socket: BluetoothSocket
        private var device: BluetoothDevice
        constructor(device: BluetoothDevice){
            this.device = device
            lateinit var tmp: BluetoothSocket
            try{
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID)
            }catch (e: IOException){
                Log.e("Connect->Constructor", e.toString())
            }
            socket= tmp
        }
        override fun run(){
            try{
                socket.connect()
            }catch (e: IOException){
                Log.e("Connect->Run", e.toString())
                try{
                    socket.close()
                }catch (e: IOException){
                    Log.e("Connect->CloseSocket", e.toString())
                }
            }

            //Probabile errore
            synchronized(ComunicationUtility){
                connectThread = null
            }

            //qui
            connected(device)


        }



        open fun cancle(){
            try{
                socket.close()
            }catch(e: IOException){
                Log.e("Connect->Cancle", e.toString())
            }
        }

    }

    //*********************************************************************************
    private inner class AcceptThread: Thread() {
        var serverSocket: BluetoothServerSocket
// modificare il costruttore
        
        init{
            var tmp : BluetoothServerSocket? = null
            try{
                tmp = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
            }catch(e: IOException){
                Log.e("Accept->Constructor", e.toString())
            }
            serverSocket = tmp!!

        }
        override fun run(){
            var socket: BluetoothSocket? = null
            try{
               socket = serverSocket.accept()
            }catch (e :IOException){
                Log.e("Accept->Run", e.toString())
                try {
                    serverSocket.close()
                }catch (e: IOException){
                    Log.e("Accept->Close", e.toString())
                }
            }
            if(socket != null){
                if(state.equals(STATE_LISTEN)){
                    //doshit
                }
                if(state.equals(STATE_CONNECTED)){
                    connected(socket.remoteDevice)
                }
                if(state.equals(STATE_NONE)){

                }
                if(state.equals(STATE_CONNECTED)){
                    try{
                        socket.close()
                    }catch(e: IOException){
                        Log.e("Accept->CloseSocket", e.toString())
                    }
                }

            }
        }

        open fun cancle() {
            try {
                serverSocket.close()
            } catch (e: IOException) {
                Log.e("Accept->CloseServer", e.toString())
            }
        }

    }
    @Synchronized private fun connected (socket: BluetoothSocket , device: BluetoothDevice){
        if(connectThread != null){
            connectThread!!.cancle()
            connectThread = null
        }

    }

}