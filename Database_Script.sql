DROP DATABASE IF EXISTS LectureHallBookingDB;

CREATE DATABASE LectureHallBookingDB;

USE LectureHallBookingDB;


CREATE TABLE User (
    UserID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Role VARCHAR(30) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Username VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,

    CHECK (Role IN ('Lecturer', 'Student', 'Non-Academic Staff'))
);



CREATE TABLE Lecture_Hall (
    HallID INT PRIMARY KEY AUTO_INCREMENT,
    HallNo VARCHAR(20) NOT NULL UNIQUE,
    Capacity INT NOT NULL,
    Location VARCHAR(100) NOT NULL,
    Status VARCHAR(30) NOT NULL DEFAULT 'Available',

    CHECK (Capacity > 0),
    CHECK (Status IN ('Available', 'Booked', 'Maintenance'))
);


CREATE TABLE Facility (
    FacilityID INT PRIMARY KEY AUTO_INCREMENT,
    FacilityName VARCHAR(100) NOT NULL UNIQUE,
    Description VARCHAR(255)
);


CREATE TABLE Hall_Facility (
    HallID INT NOT NULL,
    FacilityID INT NOT NULL,
    Quantity INT NOT NULL DEFAULT 1,

    PRIMARY KEY (HallID, FacilityID),

    FOREIGN KEY (HallID)
        REFERENCES Lecture_Hall(HallID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (FacilityID)
        REFERENCES Facility(FacilityID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CHECK (Quantity > 0)
);


CREATE TABLE Booking (
    BookingID INT PRIMARY KEY AUTO_INCREMENT,

    UserID INT NOT NULL,
    HallID INT NOT NULL,

    BookingDate DATE NOT NULL,
    StartTime TIME NOT NULL,
    EndTime TIME NOT NULL,

    Purpose VARCHAR(255) NOT NULL,

    BookingStatus VARCHAR(30)
        NOT NULL DEFAULT 'Pending',

    RequestedStudentCount INT NOT NULL,

    FOREIGN KEY (UserID)
        REFERENCES User(UserID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    FOREIGN KEY (HallID)
        REFERENCES Lecture_Hall(HallID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CHECK (EndTime > StartTime),

    CHECK (RequestedStudentCount > 0),

    CHECK (
        BookingStatus IN
        ('Pending', 'Approved', 'Cancelled', 'Completed')
    )
);


CREATE TABLE Report (
    ReportID INT PRIMARY KEY AUTO_INCREMENT,

    UserID INT NOT NULL,

    GeneratedDate DATETIME
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ReportType VARCHAR(50) NOT NULL,

    FOREIGN KEY (UserID)
        REFERENCES User(UserID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


CREATE TABLE Booking_Report (
    BookingID INT NOT NULL,
    ReportID INT NOT NULL,

    PRIMARY KEY (BookingID, ReportID),

    FOREIGN KEY (BookingID)
        REFERENCES Booking(BookingID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (ReportID)
        REFERENCES Report(ReportID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);



INSERT INTO User
(Name, Role, Email, Username, Password)
VALUES
('Rizny', 'Lecturer',
 'rizny@university.edu', 'rizny', '12345'),

('Nimrada', 'Lecturer',
 'nimrada@university.edu', 'nimrada', '12345'),

('Kaveen', 'Student',
 'kaveen@university.edu', 'kaveen', '12345'),

('Indumini', 'Non-Academic Staff',
 'indumini@university.edu', 'indumini', '12345'),

('Tharuka', 'Student',
 'tharuka@university.edu', 'tharuka', '12345'),

('Ravindu', 'Non-Academic Staff',
 'ravindu@university.edu', 'ravindu', '12345');



INSERT INTO Lecture_Hall
(HallNo, Capacity, Location, Status)
VALUES
('A101', 100, 'Science Faculty', 'Available'),

('B202', 60, 'Science Faculty', 'Available'),

('C303', 150, 'Main Building', 'Available'),

('D404', 80, 'Management Building', 'Maintenance'),

('E505', 200, 'Main Auditorium', 'Available');


INSERT INTO Facility
(FacilityName, Description)
VALUES
('Projector',
 'Multimedia projector'),

('Smart Board',
 'Interactive smart board'),

('Air Conditioner',
 'Air conditioning system'),

('Audio System',
 'Lecture hall audio system'),

('Microphone',
 'Wireless microphone'),

('Wi-Fi',
 'High speed wireless internet');



INSERT INTO Hall_Facility
(HallID, FacilityID, Quantity)
VALUES

/* A101 */
(1, 1, 1),
(1, 2, 1),
(1, 3, 1),
(1, 5, 2),
(1, 6, 1),

/* B202 */
(2, 1, 1),
(2, 3, 1),
(2, 4, 1),
(2, 6, 1),

/* C303 */
(3, 1, 2),
(3, 2, 1),
(3, 3, 2),
(3, 4, 1),
(3, 5, 4),
(3, 6, 1),

/* E505 */
(5, 2, 1),
(5, 3, 2),
(5, 4, 2),
(5, 5, 6),
(5, 6, 1);



INSERT INTO Booking
(
    UserID,
    HallID,
    BookingDate,
    StartTime,
    EndTime,
    Purpose,
    BookingStatus,
    RequestedStudentCount
)
VALUES
(
    1,
    1,
    '2026-09-10',
    '09:00:00',
    '11:00:00',
    'Database Management Systems Lecture',
    'Approved',
    80
),

(
    2,
    3,
    '2026-09-10',
    '13:00:00',
    '15:00:00',
    'Object Oriented Programming Lecture',
    'Pending',
    120
),

(
    1,
    2,
    '2026-09-11',
    '10:00:00',
    '12:00:00',
    'Programming Tutorial',
    'Approved',
    50
);




INSERT INTO Report
(UserID, ReportType)
VALUES
(4, 'Daily Booking Report'),

(6, 'Weekly Booking Report');



INSERT INTO Booking_Report
(BookingID, ReportID)
VALUES
(1, 1),
(2, 1),
(3, 2);



/*TRIGGER - PREVENT DOUBLE BOOKINGS (INSERT) */


DELIMITER $$

CREATE TRIGGER prevent_double_booking
BEFORE INSERT ON Booking
FOR EACH ROW
BEGIN

    IF EXISTS
    (
        SELECT 1
        FROM Booking
        WHERE HallID = NEW.HallID
          AND BookingDate = NEW.BookingDate

          AND BookingStatus IN
              ('Pending', 'Approved')

          AND StartTime < NEW.EndTime
          AND EndTime > NEW.StartTime
    )
    THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
        'ERROR: Lecture hall is already booked during this time.';

    END IF;

END$$

DELIMITER ;


/*TRIGGER - PREVENT DOUBLE BOOKINGS (UPDATE)*/

DELIMITER $$

CREATE TRIGGER prevent_double_booking_update
BEFORE UPDATE ON Booking
FOR EACH ROW
BEGIN

    IF EXISTS (
        SELECT 1
        FROM Booking
        WHERE HallID = NEW.HallID
          AND BookingDate = NEW.BookingDate
          AND BookingID <> NEW.BookingID
          AND BookingStatus IN ('Pending', 'Approved')
          AND StartTime < NEW.EndTime
          AND EndTime > NEW.StartTime
    ) THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
        'ERROR: Lecture hall is already booked during this time.';

    END IF;

END$$

DELIMITER ;


/*
TRIGGER - PREVENT BOOKING MAINTENANCE HALL
   */

DELIMITER $$

CREATE TRIGGER prevent_maintenance_booking
BEFORE INSERT ON Booking
FOR EACH ROW
BEGIN

    DECLARE hall_status VARCHAR(30);

    SELECT Status
    INTO hall_status
    FROM Lecture_Hall
    WHERE HallID = NEW.HallID;

    IF hall_status = 'Maintenance'
    THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
        'ERROR: This lecture hall is under maintenance.';

    END IF;

END$$

DELIMITER ;


/*  TRIGGER - PREVENT CAPACITY VIOLATION
  */

DELIMITER $$

CREATE TRIGGER check_hall_capacity
BEFORE INSERT ON Booking
FOR EACH ROW
BEGIN

    DECLARE hall_capacity INT;

    SELECT Capacity
    INTO hall_capacity
    FROM Lecture_Hall
    WHERE HallID = NEW.HallID;

    IF NEW.RequestedStudentCount > hall_capacity
    THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
        'ERROR: Number of students exceeds hall capacity.';

    END IF;

END$$

DELIMITER ;


CREATE VIEW Available_Halls AS
SELECT
    HallID,
    HallNo,
    Capacity,
    Location,
    Status
FROM Lecture_Hall
WHERE Status = 'Available';


CREATE VIEW Booking_Details AS
SELECT
    b.BookingID,
    u.Name AS UserName,
    u.Role,
    h.HallNo,
    h.Capacity,
    h.Location,
    b.BookingDate,
    b.StartTime,
    b.EndTime,
    b.Purpose,
    b.BookingStatus,
    b.RequestedStudentCount
FROM Booking b

JOIN User u
    ON b.UserID = u.UserID

JOIN Lecture_Hall h
    ON b.HallID = h.HallID;


SELECT * FROM User;

SELECT * FROM Lecture_Hall;

SELECT * FROM Facility;

SELECT * FROM Hall_Facility;

SELECT * FROM Booking;

SELECT * FROM Report;

SELECT *
FROM Available_Halls;


SELECT
    HallNo,
    Capacity,
    Location,
    Status
FROM Lecture_Hall
WHERE Status = 'Available'
AND Capacity >= 80;


/* DISPLAY FACILITIES OF A101 */

SELECT
    h.HallNo,
    f.FacilityName,
    hf.Quantity
FROM Hall_Facility hf

JOIN Lecture_Hall h
    ON hf.HallID = h.HallID

JOIN Facility f
    ON hf.FacilityID = f.FacilityID

WHERE h.HallNo = 'A101';


/*  DISPLAY COMPLETE BOOKING DETAILS*/

SELECT *
FROM Booking_Details
ORDER BY BookingDate, StartTime;


/*  DISPLAY ONLY APPROVED BOOKINGS*/

SELECT *
FROM Booking_Details
WHERE BookingStatus = 'Approved';


/*DISPLAY CANCELLED BOOKINGS*/

SELECT *
FROM Booking_Details
WHERE BookingStatus = 'Cancelled';


/* FIND BOOKINGS FOR A PARTICULAR DATE */

SELECT *
FROM Booking_Details
WHERE BookingDate = '2026-09-10';


/* FIND BOOKINGS FOR A PARTICULAR HALL*/

SELECT *
FROM Booking_Details
WHERE HallNo = 'A101';


/* COUNT BOOKINGS PER HALL*/

SELECT
    h.HallNo,
    COUNT(b.BookingID) AS TotalBookings

FROM Lecture_Hall h

LEFT JOIN Booking b
    ON h.HallID = b.HallID

GROUP BY
    h.HallID,
    h.HallNo;


/* COUNT BOOKINGS PER USER*/

SELECT
    u.UserID,
    u.Name,
    COUNT(b.BookingID) AS TotalBookings

FROM User u

LEFT JOIN Booking b
    ON u.UserID = b.UserID

GROUP BY
    u.UserID,
    u.Name;


/*  DISPLAY REPORT INFORMATION*/

SELECT
    r.ReportID,
    r.ReportType,
    r.GeneratedDate,
    u.Name AS GeneratedBy

FROM Report r

JOIN User u
    ON r.UserID = u.UserID;


/* DISPLAY REPORTS WITH THEIR BOOKINGS*/

SELECT
    r.ReportID,
    r.ReportType,
    r.GeneratedDate,
    b.BookingID,
    b.BookingDate,
    b.StartTime,
    b.EndTime,
    b.Purpose

FROM Report r

JOIN Booking_Report br
    ON r.ReportID = br.ReportID

JOIN Booking b
    ON br.BookingID = b.BookingID;


/* UPDATE BOOKING*/

UPDATE Booking
SET
    StartTime = '10:00:00',
    EndTime = '12:00:00'
WHERE BookingID = 1;


/*  CANCEL BOOKING*/

UPDATE Booking
SET BookingStatus = 'Cancelled'
WHERE BookingID = 1;


/*  UPDATE HALL STATUS */

UPDATE Lecture_Hall
SET Status = 'Maintenance'
WHERE HallID = 4;


/* MAKE HALL AVAILABLE*/

UPDATE Lecture_Hall
SET Status = 'Available'
WHERE HallID = 4;


/* TEST LOGIN*/

SELECT
    UserID,
    Name,
    Role,
    Email
FROM User
WHERE Username = 'rizny'
AND Password = '12345';

