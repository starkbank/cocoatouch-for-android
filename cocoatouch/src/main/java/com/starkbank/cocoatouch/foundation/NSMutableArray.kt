package com.starkbank.cocoatouch.foundation


class NSMutableArray<NSObject> : NSArray<NSObject>() {

    fun addObject(`object`: NSObject) {
        array.add(`object`)
    }

    fun insertObject(`object`: NSObject, index: Int) {
        array.add(index, `object`)
    }

    fun removeObjectAtIndex(index: Int) {
        array.removeAt(index)
    }

    fun removeAllObjects() {
        array.removeAll(array)
    }

    fun removeObject(`object`: NSObject) {
        array.remove(`object`)
    }
}