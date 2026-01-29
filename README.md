## Movie Ticket Booking App

---

An online movie ticket booking platform to cater B2B(theatre partners) and B2C(end customers) clients.

### Features:

#### Read Scenario
***API Endpoint: /v1/movies/{movieId}/shows?city={city}&date={date}***
1. Browse theatres currently running the show (movie selected) in the town, including show timing by a chosen date.

#### Write Scenario
***API Endpoint: /v1/bookings***
1. Book movie tickets by selecting a theatre, timing, and preferred seats for the day.

---

### API Endpoints

**Get Movie Shows**

GET /v1/movies/1/shows?city=kolkata&date=2026-02-01

Response:
```
{
    "movieId": 1,
    "movieName": "Inception",
    "date": "2026-02-01",
    "city": "kolkata",
    "theatres": [
        {
            "theatreId": 1,
            "theatreName": "PVR City Centre",
            "shows": [
                {
                    "showId": 1,
                    "startTime": "18:00:00",
                    "endTime": "21:00:00"
                },
                {
                    "showId": 5,
                    "startTime": "15:00:00",
                    "endTime": "17:30:00"
                }
            ]
        }
    ]
}
```



**Book Tickets**

POST /v1/bookings

```
{
    "showId": 1,
    "seatIds": [1, 2, 3]
}
```
Response:

```
{
    "bookingId": 15,
    "showId": 1,
    "seatIds": [
        1,
        2
    ],
    "bookedAt": "2026-01-29T03:14:50.35751"
}
```


---

**ERD**

![ERD-Ticket Booking App](resources/static/movie-ticket-booking-app-erd.png)

Relative Path: resources/static/movie-ticket-booking-app-erd.png

---

**High Level System Diagram**

URL: https://excalidraw.com/#json=848s9oBewapcNeSxpOGd6,jba7xo_JwLqYXSAeDJ92BQ

---

## License

This project is provided as-is for evaluation purposes.

**Project Version:** 1.0.0
**Last Updated:** January 2026
**Status:** Under Development