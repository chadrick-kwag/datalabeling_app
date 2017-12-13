package com.example.chadrick.datalabeling.Models

import java.lang.ref.WeakReference
import java.util.concurrent.CancellationException
import kotlin.reflect.KProperty

/**
 * Created by chadrick on 17. 12. 14.
 */

//class WeakRefHolder<T>(private val creator: ()->T){
//    private var value : WeakReference<T> = WeakReference(creator())
//
//    operator fun getValue(thisRef: Any? , property: KProperty<*>):T =
//            value.get() ?: creator().also { value = WeakReference(it) }
//
//}

class WeakRefHolder<T> {
    var weakref : WeakReference<T>? = null

    operator fun setValue(thisRef : Any?, property: KProperty<*> ,value : T){
        weakref = WeakReference(value)

    }

    operator fun getValue(thisRef:Any? , property: KProperty<*>): T {
        return weakref?.get() ?: throw CancellationException()
    }
}

// var mcontext: Context by weakrefholder()

// mcontext = context

//weakrefholder().setvalue(context)