package com.uxitech.cocoaandroid.comm

import android.os.Bundle
import android.app.Activity
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.*
import android.os.Handler
import android.os.Message
import android.util.Log

import java.io.IOException
import java.util.UUID
import java.io.InputStream
import java.io.OutputStream

import com.uxitech.cocoaandroid.util.MyException
import com.uxitech.cocoaandroid.util.UIFactory.showShortToast
import android.content.Intent


/**
 * Created by kuanyu on 2016/12/22.
 */

enum class BTState(val v: Int) {
    NONE(-1),
    LISTEN(1),
    CONNECTING(2),
    CONNECTED(3),
    DISCONNECTED(4)
}

enum class BTActions(val v: String) {
    FOUND(BluetoothDevice.ACTION_FOUND),
    DISCOVERY_STARTED(BluetoothAdapter.ACTION_DISCOVERY_STARTED),
    DISCOVERY_FINISHED(BluetoothAdapter.ACTION_DISCOVERY_FINISHED),
    BOND_STATE_CHANGED(BluetoothDevice.ACTION_BOND_STATE_CHANGED),
}

object BluetoothService {

    // Debug Tag
    private val TAG = "BluetoothService"

    // My UUID
    // 1. For connecting Android Peer
    //private val MY_UUID = UUID.randomUUID()
    // 2. For connecting Bluetooth serial board
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // UI related properties
    private var _activity: Activity? = null

    // Bluetooth related properties
    private val _adapter: BluetoothAdapter?
        by lazy { BluetoothAdapter.getDefaultAdapter() }

    private var _handler: Handler? = null   // message handler
    private var _connectThread: ConnectThread? = null
    private var _acceptThread: AcceptThread? = null
    private var _connectedThread: ConnectedThread? = null

    private var _state = BTState.NONE


    // Methods
    /*
     * Init properties in BluetoothService class and
     * enable bluetooth.
     */
    fun init(activity: Activity)  {
        _activity = activity
        if (_adapter == null) {
            throw MyException("Device not support bluetooth!")
        }
    }

    fun initHandler(handler: Handler) {
        _handler = handler
    }

    fun isEnable(): Boolean =
        _adapter!!.isEnabled

    fun notEnable(): Boolean =
        !(_adapter!!.isEnabled)


    fun enableBluetooth() {
        val requestCode: Int = ConnectRequest.ENABLE_BT.v
        val intentEnable = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        val options = Bundle()

        startActivityForResult(_activity,
            intentEnable,
            requestCode,
            options)
    }

    /* Make ths bluetooth device discoverable */
    fun expose() {
        val exposeTime = 120  // seconds

        if (_adapter?.scanMode !== BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, exposeTime)
            _activity?.startActivity(discoverableIntent)
        }
    }

    fun discover(broadcastReceiver: BroadcastReceiver) {
        if (_adapter?.isDiscovering as Boolean) {
            _adapter?.cancelDiscovery()
        }

        expose()

        val intentFilter = IntentFilter()
        intentFilter.addAction(BTActions.FOUND.v)
        intentFilter.addAction(BTActions.DISCOVERY_STARTED.v)
        intentFilter.addAction(BTActions.DISCOVERY_FINISHED.v)

        _activity?.registerReceiver(broadcastReceiver, intentFilter)

        _adapter?.startDiscovery()
    }

    fun cancelDiscovery() {
        _adapter?.cancelDiscovery()
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        return _adapter!!.bondedDevices
    }

    fun connectDevice(intent: Intent, key: String) {
        // Get the device MAC address
        val address = intent.extras.getString(key).toString()
        val ctx = _activity as Context
        showShortToast(ctx, address)
        // Get the BluetoothDevice object
        val device = _adapter!!.getRemoteDevice(address)
        // Attempt to connect to the device
        connect(device)
    }

    /*
     * Set the current state of connection
     * */
    @Synchronized
    private fun setState(state: BTState) {
        _state = state
        val msgType = MessageType.STATE_CHANGE.v
        _handler?.obtainMessage(msgType, state.v, NOTHING)
                ?.sendToTarget()
    }

    @Synchronized
    fun getState(): BTState = _state

    /*
     *  Start AcceptThread to begin a session in listening(server) mode.
     *  Called by the Activity onResume()
     */
    @Synchronized
    fun listen() {
        Log.d(TAG, "Start bluetooth connection")

        clearConnectThread()
        clearConnectedThread()

        setState(BTState.LISTEN)

        if (_acceptThread == null) {
            _acceptThread = AcceptThread()
            _acceptThread?.start()
        }
    }

    /*
     * Start the ConnectThread to initiate a connection
     * to remote device.
     *
     * Params:
     *     @device:  The Bluetooth device to connect
     */
    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "try to connect with: $device")

        if (_state == BTState.CONNECTING) {
            clearConnectThread()
        }

        clearConnectedThread()

        _connectThread = ConnectThread(device)
        _connectThread?.start()
        setState(BTState.CONNECTING)
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun manageConnection(socket: BluetoothSocket, device: BluetoothDevice) {
        Log.d(TAG, "start connected thread")

        clearThreads()

        // Start the thread to manage the connection and perform transmission
        _connectedThread = ConnectedThread(socket)
        _connectedThread?.start()

        // Send the name of the connected device back to the UI
        val msgType = MessageType.DEVICE_NAME.v
        pushMsg(msgType, HandlerKey.DEVICE_NAME, device.name)

        setState(BTState.CONNECTED)
    }

    /*
     * Stop all threads
     *
     */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")

        clearThreads()

        setState(BTState.NONE)
    }


    /*
     * Write to the ConnectedThread in an unSynchronized manner.
     *
     */
    fun write(data: ByteArray) {
        var thread: ConnectedThread? = null

        synchronized (this) {
            if (_state != BTState.CONNECTED) return
            thread = _connectedThread
        }

        thread?.write(data)
    }

    /*
     * Indicate that the connection attempt failed and notify the UI.
     */
    fun connectionFailed() {
        pushMsg(MessageType.TOAST.v, HandlerKey.TOAST, "Unable to connect device")

        // restart listen mode
        //listen()
    }

    /*
     * Indicate that the connection was lost and notify the UI.
     */
    fun connectionLost() {
        pushMsg(MessageType.TOAST.v,
                HandlerKey.TOAST,
                "Device connection was lost")
        // Restart bluetooth listen mode
        //listen()
    }

    /*
     * Push message to MessageQueue(Android) through Handler(Android)
     */
    fun pushMsg(msgType: Int, key: HandlerKey, content: String) {
        val msg: Message = _handler?.obtainMessage(msgType) as Message
        val bundle = Bundle()

        bundle.putString(key.v, content)
        msg.data = bundle

        _handler?.sendMessage(msg)
    }

    /*
     * This thread listen for incoming connections (server-side client).
     * It runs until a connection is accepted (or cancelled).
     *
     */
    private class AcceptThread : Thread() {
        private val _serverSocket: BluetoothServerSocket

        init {
            var tmp: BluetoothServerSocket? = null

            try {
                tmp = _adapter?.listenUsingRfcommWithServiceRecord("BluetoothSecure", MY_UUID)
            } catch(ex: IOException) {
                Log.e(TAG, "Socket listen() failed", ex)
            }

            _serverSocket = tmp as BluetoothServerSocket
        }

        override fun run() {
            Log.d(TAG, "Begin AcceptThread")

            var mySocket: BluetoothSocket

            // Listen to the server socket if we're not connected
            while(_state != BTState.CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection OR an exception
                    mySocket = _serverSocket.accept()
                } catch(ex: IOException) {
                    Log.e(TAG, "Socket accept() failed", ex)
                    break
                }

                if (mySocket != null) {
                    synchronized (BluetoothService) {
                        when (_state) {
                            BTState.LISTEN, BTState.CONNECTING ->
                                manageConnection(mySocket, mySocket.remoteDevice)

                            BTState.NONE, BTState.CONNECTED ->
                                try {
                                    mySocket.close()
                                } catch(ex: IOException) {
                                    Log.e(TAG, "Could not close unwanted socket", ex)
                                }
                            BTState.DISCONNECTED -> {}
                        }
                    }
                } // END If
            }  // END while
            Log.i(TAG, "End _acceptThread")
        }  // END run()

        fun cancel() {
            Log.d(TAG, "Socket cancel $this")

            try {
                _serverSocket.close()
            } catch(ex: IOException) {
                Log.e(TAG, "Failed to close() server socket", ex)
            }
        }
    }

    /*
     * This thread attempt to make an outgoing connection with a device,
     * finally, the connection either succeeds or fails.
     *
     */
    private class ConnectThread(private val __device: BluetoothDevice) : Thread() {

        private val __socket: BluetoothSocket

        init {
            var tmp: BluetoothSocket? = null

            try {
                tmp = __device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch(ex: IOException) {
                Log.e(TAG, "Socket create() failed", ex)
            }

            __socket = tmp as BluetoothSocket
        }

        override fun run() {
            // Always cancel discovery because it will slow down a connection
            _adapter?.cancelDiscovery()

            Log.d(TAG, __socket.toString())

            // Make a connection
            try {
                Log.d(TAG, "Trying to establish BT socket connection")
                __socket.connect()
            } catch(ex1: IOException) {
                Log.e(TAG, "connection failed", ex1)
                //Log.e(TAG, Log.getStackTraceString(ex1))

                // Close the socket
                try {
                   __socket.close()
                } catch (ex2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure.", ex2)
                }
                connectionFailed()
                return
            }

            // Work done! Let's reset the connection thread
            synchronized (BluetoothService) {
                _connectThread = null
            }

            manageConnection(__socket, __device)
        }

        fun cancel() {
            try {
                __socket.close()
            } catch(ex: IOException) {
                Log.e(TAG, "close() of connect socket failed", ex)
            }
        }
    }


    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread(private val __socket: BluetoothSocket) : Thread() {

        private val __inStream: InputStream
        private val __outStream: OutputStream

        init {
            Log.d(TAG, "create ConnectedThread")

            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the bluetooth socket input and output stream
            try {
                tmpIn = __socket.inputStream
                tmpOut = __socket.outputStream
            } catch(ex: IOException) {
                Log.e(TAG, "get IO stream from socket failed on ConnectedThread", ex)
            }

            __inStream = tmpIn as InputStream
            __outStream = tmpOut as OutputStream
        }

        override fun run() {
            Log.i(TAG, "Begin _connectedThread")

            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening the input stream while connected
            while(_state == BTState.CONNECTED) {
                try {
                    bytes = __inStream.read(buffer)

                    // Sends the obtained bytes to the UI
                    _handler?.obtainMessage(MessageType.READ.v, bytes, NOTHING, buffer)
                            ?.sendToTarget()

                } catch(ex: IOException) {
                    Log.e(TAG, "disconnected", ex)

                    connectionLost()

                    // Start the service over to restart listening mode
                    //listen()
                    break
                }
            }
        }

        /*
         * Write to connected OutStream
         */
        fun write(buffer: ByteArray) {
            try {
                __outStream.write(buffer)

                // Share the send message back to UI
                _handler?.obtainMessage(MessageType.WRITE.v, NOTHING, NOTHING, buffer)
                        ?.sendToTarget()
            } catch(ex: IOException) {
                Log.e(TAG, "Exception during write", ex)
                // FIXME: This is not a right way to determine Bluetooth disconnection,
                // the only reason I put setState here (temporary) is that
                // user can not send command to remote device when Bluetooth
                // is turn off.
                // P.S.: __socket.isConnected still retain true when I turn off Bluetooth.
                setState(BTState.DISCONNECTED)
            }
        }

        fun cancel() {
            try {
                __socket.close()
            } catch(ex: IOException) {
                Log.e(TAG, "close() of connect socket failed", ex)
            }
        }
    }

    fun clearThreads() {
        clearConnectThread()
        clearConnectedThread()
        clearAcceptThread()
    }

    /*
     * Cancel the thread that is running(connect) or
     * completed(connected) the connection
     */
    fun clearConnectThread() {
        if (_connectThread != null) {
            _connectThread?.cancel()
            _connectThread = null
        }
    }

    fun clearConnectedThread() {
        if (_connectedThread != null) {
            _connectedThread?.cancel()
            _connectedThread = null
        }
    }

    /*
     * Cancel the accept thread because we only
     * want to connect to one device
     */
    fun clearAcceptThread() {
        if (_acceptThread != null) {
            _acceptThread?.cancel()
            _acceptThread = null
        }
    }

}
