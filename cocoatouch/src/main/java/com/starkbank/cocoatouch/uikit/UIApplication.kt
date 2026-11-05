package com.starkbank.cocoatouch.uikit
import android.content.Context


object UIApplication: UIResponder() {

    var delegate: UIApplicationDelegate? = null
    var isNotificationsEnabled: Boolean = true
    internal var storyboard: UIStoryboard? = null
    internal var activity: UIActivity? = null
    internal var applicationContext: Context? = null

    fun sharedApplication(): UIApplication {
        return this
    }

    internal fun attach(activity: UIActivity) {
        this.activity = activity
        this.storyboard = activity as? UIStoryboard
        this.applicationContext = activity.applicationContext
        UIImagePickerController.shared = UIImagePickerController()
        UIDocumentPickerViewController.shared = UIDocumentPickerViewController()
    }

    internal fun detach(activity: UIActivity) {
        if (this.activity !== activity) return
        this.activity = null
        this.storyboard = null
    }

    fun context(): Context {
        return this.activity!!
    }

    fun activity(): UIActivity {
        return this.activity!!
    }

    fun storyboard(): UIStoryboard {
        return this.storyboard!!
    }
}