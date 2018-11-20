package com.example.ParkingS.model

import java.math.BigDecimal
import java.time.LocalDateTime

import javax.persistence._

import scala.beans.BeanProperty

@Entity
@Table(name = "tickets")
class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @BeanProperty
    var id: Long = _
    
    @BeanProperty
    var disabled: Boolean = _
    
    @BeanProperty
    var begin: LocalDateTime = _
    
    @BeanProperty
    var end: LocalDateTime = _
    
    @ManyToOne
    @BeanProperty
    var currency: Currency = _
    
    @BeanProperty
    var value: BigDecimal = _
}
