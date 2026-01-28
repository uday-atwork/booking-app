CREATE DATABASE movie_booking;

-- Populate Data
INSERT INTO public.theatre(
    id, city, movies_running, theatre_name)
VALUES (?, ?, ?, ?);

INSERT INTO theatre (id, name, city)
VALUES
    (1, 'PVR City Centre', 'Kolkata'),
    (2, 'INOX Bellandur', 'Bangalore'),
    (3, 'INOX CP', 'Delhi');

INSERT INTO show (
    id,
    movie_id,
    theatre_id,
    show_date,
    start_time,
    end_time
)
VALUES
    -- Inception in Bangalore
    (1, 1, 1, '2026-02-01', '10:00', '12:30'),
    (2, 1, 1, '2026-02-01', '18:00', '20:30'),
    (3, 1, 2, '2026-02-01', '16:00', '18:30'),

    -- Same movie, different city (should NOT appear)
    (4, 1, 3, '2026-02-01', '20:00', '22:30'),

    -- Different movie, same city (should NOT appear)
    (5, 2, 1, '2026-02-01', '21:00', '23:30');


INSERT INTO seat (id, seat_number, seat_type, theatre_id)
VALUES
    -- PVR Koramangala
    (1, 'A1', 'REGULAR', 1),
    (2, 'A2', 'REGULAR', 1),
    (3, 'B1', 'PREMIUM', 1),

    -- INOX Malleshwaram
    (4, 'A1', 'REGULAR', 2),
    (5, 'A2', 'REGULAR', 2),
    (6, 'B1', 'PREMIUM', 2);



INSERT INTO seat_availability (
    id,
    show_id,
    seat_id,
    status
)
VALUES
    -- Show 1 (PVR Koramangala - 10 AM)
    (1, 1, 1, 'AVAILABLE'),
    (2, 1, 2, 'AVAILABLE'),
    (3, 1, 3, 'BOOKED'),

    -- Show 2 (PVR Koramangala - 6 PM)
    (4, 2, 1, 'AVAILABLE'),
    (5, 2, 2, 'LOCKED'),
    (6, 2, 3, 'AVAILABLE'),

    -- Show 3 (INOX Malleshwaram - 4 PM)
    (7, 3, 4, 'AVAILABLE'),
    (8, 3, 5, 'AVAILABLE'),
    (9, 3, 6, 'AVAILABLE');


INSERT INTO movie (id, name, release_date, duration)
VALUES
    (1, 'Inception', '2010-07-16', 148),
    (2, 'Interstellar', '2014-11-07', 169);



INSERT INTO movie_cast (movie_id, cast_member)
VALUES
    (1, 'Leonardo DiCaprio'),
    (1, 'Joseph Gordon-Levitt'),
    (1, 'Elliot Page'),

    (2, 'Matthew McConaughey'),
    (2, 'Anne Hathaway'),
    (2, 'Jessica Chastain');

INSERT INTO movie_genre (movie_id, genre)
VALUES
    (1, 'SCI_FI'),
    (1, 'THRILLER'),

    (2, 'SCI_FI'),
    (2, 'DRAMA');

INSERT INTO movie_language (movie_id, language)
VALUES
    (1, 'ENGLISH'),
    (1, 'HINDI'),

    (2, 'ENGLISH');




INSERT INTO show (
    movie_id,
    theatre_id,
    show_date,
    start_time,
    end_time
)
VALUES
    (
        1,              -- movieId
        1,              -- theatreId (Kolkata)
        '2026-02-01',   -- date
        '18:00',        -- start time
        '21:00'         -- end time
    ),
-- Kolkata, different theatre
    (1, 1, '2026-02-01', '15:00', '17:30'),

-- Bangalore, same movie (should NOT appear for Kolkata API)
    (1, 3, '2026-02-01', '20:00', '22:30'),

-- Different movie, same city (should NOT appear for movieId=1)
    (2, 1, '2026-02-01', '21:00', '23:45');
