package com.example.ParkingS.repository

import java.lang.Long
import java.time.LocalDate
import java.util

import com.example.ParkingS.model.Ticket
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

trait TicketRepository extends CrudRepository[Ticket, Long] {
    @Query(value = "select * from tickets where date(end) = :date", nativeQuery = true)
    def findByDate(@Param("date") date: LocalDate): util.List[Ticket]
}