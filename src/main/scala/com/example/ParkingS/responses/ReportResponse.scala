package com.example.ParkingS.responses

import scala.beans.BeanProperty

class ReportResponse(@BeanProperty var date: String,
                     @BeanProperty var currencyCode: String,
                     @BeanProperty var income: String) {
    def this() {
        this(null, null, null)
    }
}
