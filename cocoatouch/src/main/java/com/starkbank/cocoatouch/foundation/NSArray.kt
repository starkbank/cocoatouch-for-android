package com.starkbank.cocoatouch.foundation


open class NSArray<NSObject> : Iterable<NSObject> {

    protected var array = ArrayList<NSObject>()

    fun count(): Int {
        return array.size
    }

    fun objectAtIndex(index: Int): NSObject {
        return array[index]
    }

    override fun iterator(): Iterator<NSObject> {
        return array.iterator()
    }

    fun lastObject(): NSObject? {
        return if (array.isEmpty()) null else array[array.size - 1]
    }
}