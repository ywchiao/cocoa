package com.uxitech.cocoaandroid

import android.os.Bundle
import android.view.View
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.bluetooth.BluetoothDevice
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*

import com.uxitech.cocoaandroid.comm.BluetoothService
import com.uxitech.cocoaandroid.comm.BTActions
import android.widget.TextView
import com.uxitech.cocoaandroid.util.UIFactory.showShortToast


/**
 * Created by kuanyu on 2016/12/27.
 */

class DeviceListActivity : AppCompatActivity() {

    // Static properties
    companion object {
        val EXTRA_DEVICE_ADDRESS = "device_address"
    }

    // Circular progress for waiting bluetooth discovery
    private var _progressBar: ProgressBar? = null

    // Used to check whether this activity register the _broadcastReceiver
    private var _isRegisterReceiver: Boolean = false

    // Newly discovered devices
    // NOTE:
    //     `by lazy` is necessary, otherwise, it will
    //      access the system service (which is not available
    //      at the beginning) to Activities before onCreate()
    private val _newDevicesAdapter by lazy {
        ArrayAdapter<String>(this, R.layout.device_item)
    }


    // Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the window
        setContentView(R.layout.activity_device_list)

        // Set result CANCELED in case the user backs out
        setResult(RESULT_CANCELED)

        // Init Bluetooth service
        BluetoothService.init(this)
        val scanButton = findViewById(R.id.button_scan) as Button
        scanButton.setOnClickListener { v: View ->
            BluetoothService.discover(_broadcastReceiver)
            //v.visibility = View.GONE
        }

        setPairedDevicesView()
        setNewDevicesView()
    }

    override fun onDestroy() {
        super.onDestroy()

        BluetoothService.cancelDiscovery()

        if (_isRegisterReceiver) {
            unregisterReceiver(_broadcastReceiver)
        }
    }

    private fun stringifyDevices(devices: Set<BluetoothDevice>): List<String> {
        val stringify = { device: BluetoothDevice ->
            "${device.name}\n${device.address}"
        }
        return devices.map(stringify)
    }


    private fun setPairedDevicesView() {
        val devices = BluetoothService.getPairedDevices()
        val pairedDeviceList = stringifyDevices(devices) as MutableList
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.device_item)
        arrayAdapter.addAll(pairedDeviceList)

        val listView = findViewById(R.id.paired_devices) as ListView
        listView.adapter = arrayAdapter
        listView.onItemClickListener = _handleDeviceClicked

        val titleView = findViewById(R.id.title_paired_devices)
        titleView.visibility = View.VISIBLE
    }


    private fun setNewDevicesView() {
        val listView = findViewById(R.id.new_devices) as ListView
        listView.adapter = _newDevicesAdapter
        listView.onItemClickListener = _handleDeviceClicked
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private val _handleDeviceClicked = AdapterView.OnItemClickListener {
        av, view: View, arg2, arg3 ->
            // Cancel discovery because it's costly and we're about to connect
            BluetoothService.cancelDiscovery()

            // Get the device MAC address which is the sub-string after `\n`
            val info = (view as TextView)
                .text.toString()
                .split('\n')
            val address = info[1]

            // Create the result Intent with MAC address to start connection
            val intent = Intent()
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

            // Set result and finish this Activity
            setResult(RESULT_OK, intent)
            finish()
    }

    /* Handle each stage of Bluetooth discovery */
    private val _broadcastReceiver = object : BroadcastReceiver() {

        private val TAG = "Bluetooth BroadcastReceiver"

        override fun onReceive(ctx: Context, intent: Intent) {
            _isRegisterReceiver = true

            when (intent.action) {
                // Discovery begin, clear the new devices list
                // and show the progress bar
                BTActions.DISCOVERY_STARTED.v -> {
                    _newDevicesAdapter.clear()

                    _progressBar = findViewById(R.id.device_search_progress_bar)
                        as ProgressBar
                    _progressBar?.visibility = View.VISIBLE
                }

                // Found a device, so add it to the device list
                BTActions.FOUND.v ->  {
                    val device = intent.getParcelableExtra<BluetoothDevice>(
                                        BluetoothDevice.EXTRA_DEVICE)

                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        _newDevicesAdapter.add("${device.name}\n${device.address}")
                    }
                }

                // Finishing discovery, hide the progress bar. If no devices
                // found, notify the user.
                BTActions.DISCOVERY_FINISHED.v -> {
                    _progressBar?.visibility = View.GONE
                    _progressBar = null

                    if (_newDevicesAdapter.isEmpty)
                        _newDevicesAdapter.add("No devices found")
                }
            }
        } // END onReceive
    }  // END _broadcastReceiver
}

