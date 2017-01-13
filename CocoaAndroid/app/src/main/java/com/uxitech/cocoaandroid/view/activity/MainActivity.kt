package com.uxitech.cocoaandroid

import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.databinding.DataBindingUtil
import com.uxitech.cocoaandroid.controller.Workspace

import com.uxitech.cocoaandroid.databinding.ActivityMainBinding
import com.uxitech.cocoaandroid.comm.BluetoothService
import com.uxitech.cocoaandroid.view.BaseView
import com.uxitech.cocoaandroid.view.WorkspaceView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var _binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setView()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothService.stop()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        BaseView.handleActivityResult(requestCode, resultCode, intent)
    }

    override fun onBackPressed() {
        BaseView.handleBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val isConnected = Workspace.BluetoothHandler.isDeviceConnected
        menu?.findItem(R.id.device_connected)
            ?.isVisible = isConnected
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return BaseView.handleOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        return BaseView.handleNavigationItemSelected(item)
    }

    fun setView() {
        //setContentView(R.layout.activity_main)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        BaseView.init(this)
        initOminoWorkspace()
    }

    fun initOminoWorkspace() {
        val workspaceBinding = _binding.contentMain.workspace
        WorkspaceView.init(this, workspaceBinding)
    }
}
