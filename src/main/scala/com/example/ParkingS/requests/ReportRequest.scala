package com.example.ParkingS.requests

import scala.beans.BeanProperty

class ReportRequest(@BeanProperty var date: String,
                    @BeanProperty var currencyCode: String) {
    def this() {
        this(null, null)
    }
}
