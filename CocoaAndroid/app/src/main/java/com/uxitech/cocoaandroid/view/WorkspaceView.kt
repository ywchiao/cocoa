package com.uxitech.cocoaandroid.view

import com.uxitech.cocoaandroid.MainActivity
import com.uxitech.cocoaandroid.controller.Workspace
import com.uxitech.cocoaandroid.databinding.WorkspaceBinding


object WorkspaceView {
    private lateinit var _activityMain: MainActivity
    private lateinit var _workspaceBinding: WorkspaceBinding

    fun init(activity: MainActivity, binding: WorkspaceBinding) {
        _activityMain = activity
        _workspaceBinding = binding

        Workspace.init(_activityMain)
    }
}


