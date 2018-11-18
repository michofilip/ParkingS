package com.example.ParkingS.responses

import scala.beans.BeanProperty

class EndParkingResponse(@BeanProperty var id: String,
                         @BeanProperty var disabled: String,
                         @BeanProperty var begin: String,
                         @BeanProperty var end: String,
                         @BeanProperty var currencyCode: String,
                         @BeanProperty var value: String) {
    def this() {
        this(null, null, null, null, null, null)
    }
}
