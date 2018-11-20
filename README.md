# ParkingS (Scala version)

## Description

This application helps managing a parking lot. It keeps track for long vehicles were parked, 
calculates charge and prepares income report for the owner. 

## Usage

To initiate a parking meter send a POST request to /driver/start of form </br>
```
{
  "disabled": "false" (or "true")
}
```
Example of response </br>
```
{
    "id": "123",
    "disabled": "false",
    "begin": "2018-11-20T16:55:49.826"
}
```
To stop a parking meter send a POST request to /driver/stop of form </br>
```
{
  "id": "123",
  "currencyCode": "PLN"
}
```
Example of response </br>
```
{
    "id": "123",
    "disabled": "false",
    "begin": "2018-11-20T16:55:49.826",
    "end": "2018-11-20T16:55:53.189",
    "currencyCode": "PLN",
    "value": "1.00"
}
```
To get an income report send a POST request to /owner/report of form </br>
```
{
  "date": "2018-11-20",
  "currencyCode": "PLN"
}
```
Example of response </br>
```
{
    "date": "2018-11-20",
    "currencyCode": "PLN",
    "income": "123.45"
}
```

To start the application use start.sh file in run/bin folder
