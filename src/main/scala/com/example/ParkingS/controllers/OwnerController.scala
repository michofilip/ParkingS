package com.example.ParkingS.controllers

import java.time.LocalDate
import java.time.format.DateTimeParseException

import com.example.ParkingS.exceptions._
import com.example.ParkingS.repository.{CurrencyRepository, TicketRepository}
import com.example.ParkingS.requests.ReportRequest
import com.example.ParkingS.responses.{ErrorResponse, ReportResponse}
import com.example.ParkingS.service.report.IncomeCalculator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

@RestController
@RequestMapping(Array("/owner"))
class OwnerController @Autowired()(private val ticketRepository: TicketRepository,
                                   private val currencyRepository: CurrencyRepository) {
    
    @PostMapping(Array("/report"))
    def report(@RequestBody reportRequest: ReportRequest): ReportResponse = {
        verify(reportRequest)
        
        val currency = currencyRepository.findByCode(reportRequest.currencyCode)
        val localDate = LocalDate.parse(reportRequest.date)
        val tickets = ticketRepository.findByDate(localDate)
        
        val reportResponse = new ReportResponse()
        reportResponse.date = localDate.toString
        reportResponse.currencyCode = currency.code
        reportResponse.income = IncomeCalculator.calculate(tickets, currency).toString
        
        reportResponse
    }
    
    @ExceptionHandler()
    def handleException(ex: Exception): ErrorResponse = {
        ex match {
            case _: IncorrectReportRequestException => new ErrorResponse("Incorrect ReportRequest format")
            case _: UnknownCurrencyException => new ErrorResponse("Unknown currency")
            case _ => new ErrorResponse("Unknown error")
        }
    }
    
    private def verify(reportRequest: ReportRequest): Unit = {
        val dateStr = reportRequest.date
        val currencyCode = reportRequest.currencyCode
        
        if (dateStr == null) {
            throw new IncorrectReportRequestException()
        } else {
            try {
                LocalDate.parse(dateStr)
            } catch {
                case _: DateTimeParseException => throw new IncorrectReportRequestException();
            }
        }
        
        if (currencyCode == null) {
            throw new IncorrectReportRequestException()
        } else if (currencyRepository.findByCode(currencyCode) == null) {
            throw new UnknownCurrencyException()
        }
    }
    
}
