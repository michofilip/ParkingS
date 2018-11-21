package com.example.ParkingS.requests

import scala.beans.BeanProperty

class EndParkingRequest(@BeanProperty var id: String,
                        @BeanProperty var currencyCode: String) {
    def this() {
        this(null, null)
    }
}
