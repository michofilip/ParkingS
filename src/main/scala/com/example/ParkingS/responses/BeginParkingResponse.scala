package com.example.ParkingS.responses

import scala.beans.BeanProperty

class BeginParkingResponse(@BeanProperty var id: String,
                           @BeanProperty var disabled: String,
                           @BeanProperty var begin: String) {
    def this() {
        this(null, null, null)
    }
}
