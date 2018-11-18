package com.example.ParkingS.requests

import scala.beans.BeanProperty

class BeginParkingRequest(@BeanProperty var disabled: String) {
    def this() {
        this(null)
    }
}