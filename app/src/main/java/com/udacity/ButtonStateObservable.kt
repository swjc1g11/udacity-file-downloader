package com.udacity

import java.util.*

interface ButtonStateObservableChangeListener {
    fun update(buttonState: ButtonState)
}

class ButtonStateObservable private constructor()  {
    companion object {
        private val observableButtonState = ButtonStateObservable()

        fun getInstance(): ButtonStateObservable {
            return observableButtonState
        }
    }
    val buttonState = ButtonState.Initial
    val observers = ArrayList<ButtonStateObservableChangeListener>()

    fun addObserver(observer: ButtonStateObservableChangeListener) {
        observers.add(observer)
    }

    fun removeObserver(observer: ButtonStateObservableChangeListener) {
        observers.remove(observer)
    }

    fun notifyAll(value: ButtonState) {
        observers.forEach {
            it.update(value)
        }
    }

    fun setButtonState(buttonState: ButtonState) {
        synchronized(observers) {
            notifyAll(buttonState)
        }
    }
}