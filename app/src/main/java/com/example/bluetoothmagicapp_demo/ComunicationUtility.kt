package com.example.bluetoothmagicapp_demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


open class ComunicationUtility {
    private var context : Context
    private val handler : Handler

    private val TAG  = "CommunicationUtility"

   // private var connectedThread : ConnectThread? = null
    private var acceptThread : AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private val APP_UUID = UUID.fromString("865d40c2-3b15-4504-945a-4ab448d19122") //ATTENZIONE
    private var bluetoothAdapter: BluetoothAdapter
    private val APP_NAME = "MAGIC_APP"
    private var state__: Int

    companion object {  // oggetti static
        val STATE_NONE : Int = 0
        val STATE_LISTEN: Int = 1
        val STATE_CONNECTING: Int = 2
        val STATE_CONNECTED: Int  = 3
    }


    constructor(context: Context, handler: Handler){
        this.context=context
        this.handler=handler

        state__ = STATE_NONE
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.d(TAG, "CommunicatioUtility constructor")
    }

    open fun getState(): Int{
        return state__
        Log.d(TAG, "getState()")
    }
    @Synchronized open fun setState(state: Int){
        this.state__= state
        handler.obtainMessage(MainActivity.MESSAGGE_STATE_CHANGE,state,-1).sendToTarget()
        Log.d(TAG, " @Synchronized setState()")

    }
    @Synchronized open fun start(){
        if(connectThread != null){
            connectThread?.cancle()
            connectThread = null
        }
        if(acceptThread == null){
            acceptThread = AcceptThread()
            acceptThread?.start()
        }
        Log.d(TAG, " @Synchronized start()")
        setState(STATE_LISTEN)

        if(connectedThread!= null ){
            connectedThread?.cancle()
            connectedThread = null
        }

    }
    @Synchronized open fun stop(){
        if(connectThread != null){
            connectThread!!.cancle()
            connectThread = null
        }
        if(acceptThread != null){
            acceptThread!!.cancle()
            connectThread = null
        }
        if(connectedThread!= null ){
            connectedThread!!.cancle()
            connectedThread = null
        }

        setState(STATE_LISTEN)
        Log.d(TAG, " @Synchronized stop()")
    }
    /*
   * funzione Synchronized per ricevere lo stato dell'oggetto CommuncationUtility
   *
   * manda lo state al handler
   *
   * */
    @Synchronized open fun connectionFailed(){
        val message: Message = handler.obtainMessage(MainActivity.MESSAGGE_TOAST)
        val bundle = Bundle()
        bundle.putString(MainActivity.TOAST, "Cant connect to device")
        message?.data = bundle
        handler.sendMessage(message)

        this@ComunicationUtility.start()
        Log.d(TAG, " @Synchronized  connectionFailed()")
    }
    @Synchronized open fun connected(device: BluetoothDevice){
        if(connectThread != null){
            connectThread!!.cancle()
            connectThread = null
        }
        if(connectedThread!= null ){
            connectedThread!!.cancle()
            connectedThread = null
        }

        val message: Message = handler.obtainMessage(MainActivity.MESSAGGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(MainActivity.DEVICE_NAME, device.name)
        message.data = bundle
        handler.sendMessage(message)
        setState(STATE_CONNECTED)
        Log.d(TAG, " @Synchronized connected()")
    }

    @Synchronized open fun connect(device: BluetoothDevice){

        if(state__ == STATE_CONNECTING){ //pulisci
            connectThread?.cancle()
            connectThread = null
        }
        //crea connessione
        connectThread = ConnectThread(device)
        connectThread?.start()

        if(connectedThread!= null ){
            connectedThread!!.cancle()
            connectedThread = null
        }
        setState(STATE_CONNECTING)
        Log.d(TAG, " @Synchronized connect()")
    }

    open fun write(buffer: ByteArray){
        var connThread: ConnectedThread? = null
        synchronized(this){
            if(state__ != STATE_CONNECTED){
                return
                connThread = connectedThread
            }
            connThread= connectedThread
        }
        connThread?.write(buffer)
        Log.d(TAG, "write()")
    }
    @Synchronized private fun connected(socket: BluetoothSocket , device: BluetoothDevice){
        if(connectThread != null){
            connectThread!!.cancle()
            connectThread = null
        }
        if (connectedThread != null) {
            connectedThread!!.cancle()
            connectedThread = null
        }
        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()

        var message = handler.obtainMessage(MainActivity.MESSAGGE_DEVICE_NAME)
        var bundle = Bundle()
        bundle.putString(MainActivity.DEVICE_NAME, device.name)
        message.data = bundle
        handler.sendMessage(message)

        setState(STATE_CONNECTED)
        Log.d(TAG, "@Synchronized connected($socket,$device)")

    }

    private fun connectionLost(){
        var message : Message = handler.obtainMessage(MainActivity.MESSAGGE_TOAST)
        var bundle = Bundle()
        bundle.putString(MainActivity.TOAST, "Connection lost")
        message.data = bundle
        handler.sendMessage(message)


        start()
        Log.d(TAG, " connectionLost() ")



    }
    //*********************************************************************************
   private inner class ConnectThread : Thread{
        private val socket: BluetoothSocket
        private val device: BluetoothDevice
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
            synchronized(ComunicationUtility){
                connectThread = null
            }
            connected(socket, device)
        }



        fun cancle(){
            try{
                socket.close()
                Log.d(TAG, " ConnectThread cancle()")
            }catch(e: IOException){
                Log.e("Connect->Cancle", e.toString())
            }
        }
    }

    //*********************************************************************************
    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private inner class AcceptThread: Thread {
        private var serverSocket: BluetoothServerSocket
        
        constructor(){
            var tmp : BluetoothServerSocket? = null
            try{
                tmp = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
            }catch(e: IOException){
                Log.e("Accept->Constructor", e.toString())
            }
            serverSocket = tmp!!
            Log.d("debug_demo", " AcceptThread constructor done !")

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
                when(state__){
                    STATE_LISTEN -> {

                    }
                    STATE_CONNECTING->{
                        connected(socket, socket.remoteDevice)
                        Log.d(TAG, " AcceptThread STATE_CONNECTING ")
                    }
                    STATE_NONE->{

                    }
                    STATE_CONNECTED->{
                        try{
                            socket.close()
                            Log.d(TAG, " AcceptThread STATE_CONNECTED ")
                        }catch(e: IOException){
                            Log.e("Accept->CloseSocket", e.toString())
                        }
                    }
                }
            }
        }

        open fun cancle() {
            try {
                serverSocket.close()
                Log.d(TAG, " AcceptThread cancle() ")

            } catch (e: IOException) {
                Log.e("Accept->CloseServer", e.toString())
            }
        }

    }

    //*********************************************************************************

    private inner class ConnectedThread: Thread{
        private var socket : BluetoothSocket
        private var inputStream: InputStream?
        private var outputStream: OutputStream?

        constructor(socket: BluetoothSocket){
            this.socket = socket

            var tmpIN : InputStream? = null
            var tmpOut : OutputStream? = null

            try{
                tmpIN = socket.inputStream
                tmpOut = socket.outputStream
            }catch (e: IOException){}
            inputStream = tmpIN
            outputStream = tmpOut
        }
        override fun run(){
            var buffer = ByteArray(1024)
            var bytes: Int
            try{
                bytes = inputStream!!.read(buffer)
                handler.obtainMessage(MainActivity.MESSAGGE_READ, bytes, -1, buffer).sendToTarget()
            }catch(e: IOException){
                connectionLost()
            }
        }
        fun write(buffer: ByteArray){
            try{
                outputStream?.write(buffer)
                handler.obtainMessage(MainActivity.MESSAGGE_WRITE, -1,-1 ,buffer).sendToTarget()
            }catch(e: IOException){

            }

        }
        open fun cancle() {
                try{
                    socket.close()
                }catch (e: IOException){}
        }

    }

    //*********************************************************************************

   /* private inner class AcceptThread_v2: Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(APP_NAME,APP_UUID )
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                   // manageMyConnectedSocket(it) //method is designed to initiate the thread for transferring data,
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }*/

}