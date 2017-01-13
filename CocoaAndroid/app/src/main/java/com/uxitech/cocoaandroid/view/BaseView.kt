package com.uxitech.cocoaandroid.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.view.View
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem

import com.uxitech.cocoaandroid.R
import com.uxitech.cocoaandroid.MainActivity
import com.uxitech.cocoaandroid.DeviceListActivity
import com.uxitech.cocoaandroid.comm.BluetoothService
import com.uxitech.cocoaandroid.comm.ConnectRequest
import com.uxitech.cocoaandroid.util.MyException
import com.uxitech.cocoaandroid.util.UIFactory.showShortToast

/**
 * Created by kuanyu on 2016/12/22.
 *
 * This view just for separating some boilerplate codes
 * from MainActivity.
 */

object BaseView {
    private lateinit var _activityMain: MainActivity

    fun init(activityMain: MainActivity) {
        _activityMain = activityMain

        initActionBar()
    }

    fun initActionBar() {
        val toolbar = getView<Toolbar>(R.id.toolbar)
        _activityMain.setSupportActionBar(toolbar)

        val drawer = getView<DrawerLayout>(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(_activityMain,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)

        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navView = getView<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(_activityMain)
    }


    fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            ConnectRequest.CONNECT_DEVICE.v ->
                if (resultCode == Activity.RESULT_OK) {
                    BluetoothService.connectDevice(
                        intent as Intent,
                        DeviceListActivity.EXTRA_DEVICE_ADDRESS)
                }
            ConnectRequest.ENABLE_BT.v -> {
                if (resultCode == Activity.RESULT_OK) {
                    jumpToDevicesPage()
                } else {
                    showShortToast(_activityMain, "Bluetooth not enabled")
                }
            }

            else -> throw MyException("Undefined request code for ActivityResult!")
        }
    }

    fun handleBackPressed() {
        val drawer = getView<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            _activityMain.onBackPressed()
        }
    }

    /* Handle events of action bar items being selected */
    fun handleOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when(id) {
            R.id.action_settings -> return true

            R.id.bluetooth_settings ->  {
                if (BluetoothService.notEnable()) {
                    BluetoothService.init(_activityMain)
                    BluetoothService.enableBluetooth()
                } else {
                    jumpToDevicesPage()
                }
                return true
            }

            R.id.wifi_settings -> return true

            // Just a icon to indicate device is connected
            R.id.device_connected -> return true

            //else -> throw MyException("Unknown menu item of action bar!")
        }

        return _activityMain.onOptionsItemSelected(item)
    }

    fun handleNavigationItemSelected(item: MenuItem): Boolean {
        val drawer = getView<DrawerLayout>(R.id.drawer_layout)
        val id = item.itemId

        when(id) {
            R.id.nav_camera ->
                Snackbar.make(drawer, "nav camera", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            R.id.nav_gallery ->
                Snackbar.make(drawer, "nav gallery", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            R.id.nav_slideshow ->
                Snackbar.make(drawer, "nav slideshow", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            R.id.nav_manage ->
                Snackbar.make(drawer, "nav manage", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            R.id.nav_share ->
                Snackbar.make(drawer, "nav share", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            R.id.nav_send ->
                Snackbar.make(drawer, "nav send", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            else ->
                Snackbar.make(drawer, "Undefined nav item!", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun <T: View> getView(id: Int): T {
        val view = _activityMain.findViewById(id)
        return view as T
    }

    /* Launch the DeviceListActivity to see devices or do discovery */
    private fun jumpToDevicesPage() {
        val serverIntent = Intent(_activityMain, DeviceListActivity::class.java)
        val requestCode = ConnectRequest.CONNECT_DEVICE.v
        val options = Bundle()
        startActivityForResult(_activityMain, serverIntent, requestCode, options)
    }
}
