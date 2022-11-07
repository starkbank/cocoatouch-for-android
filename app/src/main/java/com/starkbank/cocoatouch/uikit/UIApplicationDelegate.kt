package com.starkbank.cocoatouch.uikit


interface UIApplicationDelegate {
    fun application(application: UIApplication, didFinishLaunchingWithOptions: Map<String, String>?): Boolean
    fun applicationWillResignActive(application: UIApplication)
    fun applicationDidEnterBackground(application: UIApplication)
    fun applicationWillEnterForeground(application: UIApplication)
    fun applicationDidBecomeActive(application: UIApplication)
    fun applicationWillTerminate(application: UIApplication)
}