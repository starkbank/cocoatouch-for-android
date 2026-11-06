package com.starkbank.cocoatouch.uikit
import android.content.Context
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi


object UIApplication: UIResponder() {

    var delegate: UIApplicationDelegate? = null
    @InternalCocoaTouchApi
    var isNotificationsEnabled: Boolean = true
    var appearance: UIAppearance = UIDefaultAppearance
    @InternalCocoaTouchApi
    var storyboard: UIStoryboard? = null
        internal set
    @InternalCocoaTouchApi
    var activity: UIActivity? = null
        internal set
    internal var applicationContext: Context? = null

    fun sharedApplication(): UIApplication {
        return this
    }

    fun initialize(context: Context) {
        if (applicationContext != null) return
        applicationContext = context.applicationContext
    }

    internal fun attach(activity: UIActivity) {
        this.activity = activity
        (activity as? UIStoryboard)?.let { this.storyboard = it }
        this.applicationContext = activity.applicationContext
        val pickers = activity.pickers ?: ActivityPickers(
            image = UIImagePickerController(activity),
            document = UIDocumentPickerViewController(activity)
        ).also { activity.pickers = it }
        UIImagePickerController.shared = pickers.image
        UIDocumentPickerViewController.shared = pickers.document
    }

    internal fun detach(activity: UIActivity) {
        activity.pickers?.release()
        activity.pickers = null
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