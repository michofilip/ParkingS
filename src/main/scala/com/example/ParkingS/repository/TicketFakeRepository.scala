package com.example.ParkingS.repository

import java.time.LocalDate
import java.util

import com.example.ParkingS.model.Ticket
import org.springframework.stereotype.Repository

import scala.collection.JavaConverters

@Repository
class TicketFakeRepository() {
    private var tickets: IndexedSeq[Ticket] = IndexedSeq.empty
    
    def findOne(id: Long): Ticket = {
        tickets.find(t => t.id == id) match {
            case Some(ticket) => ticket
            case None => null
        }
    }
    
    def exists(id: Long): Boolean = {
        tickets.exists(t => t.id == id)
    }
    
    def delete(id: Long): Unit = {
        tickets = tickets.filterNot(t => t.id == id)
    }
    
    def delete(ticket: Ticket): Unit = {
        delete(ticket.id)
    }
    
    def save(ticket: Ticket): Unit = {
        if (ticket.id == 0) {
            ticket.id = nextId()
            tickets = tickets :+ ticket
        } else {
            delete(ticket)
            tickets = tickets :+ ticket
        }
    }
    
    private def nextId(): Long = {
        def n(id: Long, ts: Seq[Ticket]): Long = {
            ts match {
                case head +: tail =>
                    if (head.id >= id) {
                        n(head.id + 1, tail)
                    } else {
                        n(id, tail)
                    }
                case Nil => id
            }
        }
        
        n(1, tickets)
    }
    
    def findByDate(date: LocalDate): util.List[Ticket] = {
        val result = tickets.filter(t => t.end != null && t.end.toLocalDate.equals(date))
        
        JavaConverters.seqAsJavaList(result)
    }
}