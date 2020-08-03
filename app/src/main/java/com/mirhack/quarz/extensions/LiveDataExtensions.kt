package com.mirhack.quarz.extensions

import androidx.lifecycle.MutableLiveData
import com.mirhack.quarz.utils.Event

fun <T>MutableLiveData<Event<T>>.sendEvent(event: T): Unit {
    this.value = Event(event)
}

fun <T>MutableLiveData<T>.send(event: T): Unit {
    this.value = event
}