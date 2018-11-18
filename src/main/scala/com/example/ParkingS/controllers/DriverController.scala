package com.example.ParkingS.controllers

import java.time.LocalDateTime

import com.example.ParkingS.exceptions.{IncorrectBeginParkingRequestException, _}
import com.example.ParkingS.model.Ticket
import com.example.ParkingS.repository.{CurrencyRepository, TicketRepository}
import com.example.ParkingS.requests.{BeginParkingRequest, EndParkingRequest}
import com.example.ParkingS.responses.{BeginParkingResponse, EndParkingResponse, ErrorResponse}
import com.example.ParkingS.service.price.PriceCalculator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

@RestController
@RequestMapping(Array("/driver"))
class DriverController @Autowired()(private val ticketRepository: TicketRepository,
                                    private val currencyRepository: CurrencyRepository,
                                    private val priceCalculator: PriceCalculator) {
    
    @PostMapping(Array("/start"))
    def beginParking(@RequestBody beginParkingRequest: BeginParkingRequest): BeginParkingResponse = {
        verify(beginParkingRequest)
        
        val now = LocalDateTime.now()
        
        val ticket = new Ticket()
        ticket.disabled = "true".equals(beginParkingRequest.disabled)
        ticket.begin = now
        
        ticketRepository.save(ticket)
        
        val beginParkingResponse = new BeginParkingResponse
        beginParkingResponse.id = ticket.id.toString
        beginParkingResponse.disabled = ticket.disabled.toString
        beginParkingResponse.begin = ticket.begin.toString
        
        beginParkingResponse
    }
    
    @PostMapping(Array("/stop"))
    def endParking(@RequestBody endParkingRequest: EndParkingRequest): EndParkingResponse = {
        verify(endParkingRequest)
        
        val now = LocalDateTime.now()
        val id = endParkingRequest.id.toLong
        val currency = currencyRepository.findByCode(endParkingRequest.currencyCode)
        
        val ticket = ticketRepository.findOne(id)
        
        ticket.end = now
        ticket.currency = currency
        ticket.value = priceCalculator.calculate(ticket)
        
        ticketRepository.save(ticket)
        
        val endParkingResponse = new EndParkingResponse()
        endParkingResponse.id = ticket.id.toString
        endParkingResponse.disabled = ticket.disabled.toString
        endParkingResponse.begin = ticket.begin.toString
        endParkingResponse.end = ticket.end.toString
        endParkingResponse.currencyCode = currency.code
        endParkingResponse.value = ticket.value.toString
        
        endParkingResponse
    }
    
    @ExceptionHandler()
    def handleException(ex: Exception): ErrorResponse = {
        ex match {
            case _: IncorrectBeginParkingRequestException => new ErrorResponse("Incorrect BeginParkingRequest format")
            case _: IncorrectEndParkingRequestException => new ErrorResponse("Incorrect EndParkingRequest format")
            case _: UnknownCurrencyException => new ErrorResponse("Unknown currency")
            case _: UnknownTickedIdException => new ErrorResponse("Unknown ticked ID")
            case _: TicketAlreadyPaidException => new ErrorResponse("Ticket with this ID has already been paid")
            case _ => new ErrorResponse("Unknown error")
        }
    }
    
    private def verify(beginParkingRequest: BeginParkingRequest): Unit = {
        val disabled = beginParkingRequest.disabled
        if (disabled == null || (!disabled.equals("true") && !disabled.equals("false"))) {
            throw new IncorrectBeginParkingRequestException()
        }
    }
    
    private def verify(endParkingRequest: EndParkingRequest): Unit = {
        val idStr = endParkingRequest.id
        val currencyCode = endParkingRequest.currencyCode
        
        val id: Long =
            if (idStr == null) {
                throw new IncorrectEndParkingRequestException()
            } else {
                try {
                    idStr.toLong
                } catch {
                    case _: NumberFormatException => throw new IncorrectEndParkingRequestException()
                }
            }
        
        if (!ticketRepository.exists(id)) {
            throw new UnknownTickedIdException()
        } else {
            val ticket = ticketRepository.findOne(id)
            if (ticket.end != null) {
                throw new TicketAlreadyPaidException()
            }
        }
        
        if (currencyCode == null) {
            throw new IncorrectEndParkingRequestException()
        } else if (currencyRepository.findByCode(currencyCode) == null) {
            throw new UnknownCurrencyException()
        }
    }
}
