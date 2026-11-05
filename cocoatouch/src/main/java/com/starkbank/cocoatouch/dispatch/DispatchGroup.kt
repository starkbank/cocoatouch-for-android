package com.starkbank.cocoatouch.dispatch

class DispatchGroup {
    private var total = 0
    private var completed = 0
    private var callback: (() -> Unit)? = null
    fun enter(){
        total += 1
    }

    fun leave(){
        completed += 1
        if(total == completed){
            callback?.invoke()
        }
    }

    fun notify(dispatchQueue: DispatchQueue, work: () -> Unit){
        callback = work
    }
}

enum class DispatchQueue{
    main,
    global
}

