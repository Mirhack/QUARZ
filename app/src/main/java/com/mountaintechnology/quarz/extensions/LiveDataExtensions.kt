package com.mountaintechnology.quarz.extensions

import androidx.lifecycle.MutableLiveData
import com.mountaintechnology.quarz.utils.Event

fun <T>MutableLiveData<Event<T>>.sendEvent(event: T): Unit {
    this.value = Event(event)
}

fun <T>MutableLiveData<T>.update(event: T): Unit {
    this.value = event
}