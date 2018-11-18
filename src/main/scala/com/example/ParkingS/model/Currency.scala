package com.example.ParkingS.model

import java.math.BigDecimal

import javax.persistence._

import scala.beans.BeanProperty

@Entity
@Table(name = "currencies")
class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @BeanProperty
    var id: Long = _
    
    @Column(length = 50)
    @BeanProperty
    var name: String = _
    
    @Column(length = 3)
    @BeanProperty
    var code: String = _
    
    @Column(precision = 19, scale = 9)
    @BeanProperty
    var rate: BigDecimal = _
}
