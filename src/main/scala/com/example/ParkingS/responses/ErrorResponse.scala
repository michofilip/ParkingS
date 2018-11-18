package com.example.ParkingS.responses

import scala.beans.BeanProperty

class ErrorResponse(@BeanProperty var errorMessage: String) {
    def this() {
        this(null)
    }
}
