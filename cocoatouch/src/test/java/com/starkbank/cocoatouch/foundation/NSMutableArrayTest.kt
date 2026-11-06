package com.starkbank.cocoatouch.foundation

import org.junit.Assert.assertEquals
import org.junit.Test

class NSMutableArrayTest {

    @Test
    fun removeObjectRemovesEveryOccurrenceAndKeepsOrder() {
        val array = NSMutableArray<String>()
        listOf("a", "b", "a", "c").forEach { array.addObject(it) }
        array.removeObject("a")
        assertEquals(listOf("b", "c"), (0 until array.count()).map { array.objectAtIndex(it) })
    }
}
