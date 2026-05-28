CREATE DATABASE IF NOT EXISTS musictick;
USE musictick;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS post_reports;
DROP TABLE IF EXISTS forum_posts;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS waitlist_entries;
DROP TABLE IF EXISTS refunds;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_tickets;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS ticket_types;
DROP TABLE IF EXISTS concert_artists;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS concerts;
DROP TABLE IF EXISTS artists;
DROP TABLE IF EXISTS venues;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ORGANIZER', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'REJECTED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE venues (
    venue_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    capacity INT NOT NULL
);

CREATE TABLE seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    venue_id INT NOT NULL,
    section_name VARCHAR(50),
    row_label VARCHAR(20),
    seat_number VARCHAR(20),
    seat_type ENUM('REGULAR', 'VIP') NOT NULL,

    UNIQUE (venue_id, section_name, row_label, seat_number),

    FOREIGN KEY (venue_id)
        REFERENCES venues(venue_id)
        ON DELETE CASCADE
);

CREATE TABLE artists (
    artist_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    genre VARCHAR(100),
    bio TEXT
);

CREATE TABLE concerts (
    concert_id INT AUTO_INCREMENT PRIMARY KEY,
    organizer_id INT NOT NULL,
    venue_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    concert_date DATETIME NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    average_rating DECIMAL(3,2) DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES users(user_id),
    FOREIGN KEY (venue_id) REFERENCES venues(venue_id)
);

CREATE TABLE concert_artists (
    concert_id INT NOT NULL,
    artist_id INT NOT NULL,
    PRIMARY KEY (concert_id, artist_id),
    FOREIGN KEY (concert_id) REFERENCES concerts(concert_id) ON DELETE CASCADE,
    FOREIGN KEY (artist_id) REFERENCES artists(artist_id) ON DELETE CASCADE
);

CREATE TABLE ticket_types (
    ticket_type_id INT AUTO_INCREMENT PRIMARY KEY,
    concert_id INT NOT NULL,
    name ENUM('REGULAR', 'VIP') NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (concert_id) REFERENCES concerts(concert_id) ON DELETE CASCADE
);

CREATE TABLE tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    concert_id INT NOT NULL,
    user_id INT NOT NULL,
    ticket_type_id INT NOT NULL,
    seat_id INT,
    status ENUM('ACTIVE', 'CANCELLED', 'TRANSFERRED', 'UPGRADED', 'TEMPORARY') NOT NULL DEFAULT 'ACTIVE',
    qr_code VARCHAR(255) UNIQUE,
    purchase_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (concert_id, seat_id),
    FOREIGN KEY (concert_id) REFERENCES concerts(concert_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(ticket_type_id),
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
);

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'PAID', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE order_tickets (
    order_id INT NOT NULL,
    ticket_id INT NOT NULL,
    PRIMARY KEY (order_id, ticket_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE CASCADE
);

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL,
    transaction_reference VARCHAR(255),
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE refunds (
    refund_id INT AUTO_INCREMENT PRIMARY KEY,
    payment_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    refund_status ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL,
    refund_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE waitlist_entries (
    waitlist_id INT AUTO_INCREMENT PRIMARY KEY,
    concert_id INT NOT NULL,
    user_id INT NOT NULL,
    priority_order INT NOT NULL,
    status ENUM('WAITING', 'NOTIFIED', 'RESERVED', 'CANCELLED') NOT NULL DEFAULT 'WAITING',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (concert_id, user_id),
    FOREIGN KEY (concert_id) REFERENCES concerts(concert_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id INT,
    user_id INT NOT NULL,
    organizer_id INT,
    description TEXT NOT NULL,
    status ENUM('OPEN', 'REVIEWED', 'REFUND_APPROVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (organizer_id) REFERENCES users(user_id)
);

CREATE TABLE reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    concert_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (concert_id, user_id),
    FOREIGN KEY (concert_id) REFERENCES concerts(concert_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE forum_posts (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    concert_id INT NOT NULL,
    user_id INT NOT NULL,
    parent_post_id INT,
    title VARCHAR(200),
    content TEXT NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (concert_id) REFERENCES concerts(concert_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_post_id) REFERENCES forum_posts(post_id) ON DELETE CASCADE
);

CREATE TABLE post_reports (
    post_report_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    reason TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_concerts_date ON concerts(concert_date);
CREATE INDEX idx_tickets_user ON tickets(user_id);
CREATE INDEX idx_tickets_concert ON tickets(concert_id);
CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX idx_reviews_concert ON reviews(concert_id);
CREATE INDEX idx_forum_concert ON forum_posts(concert_id);

-- ─── Venues & Seats Seed Data ─────────────────────────────────────────────────

INSERT INTO venues (venue_id, name, city, address, capacity) VALUES
(1, 'Athens Arena',       'Athens',       'Pireos 180, Athens',         60),
(2, 'Metropolis Hall',    'Thessaloniki',  'Egnatia 100, Thessaloniki',  48),
(3, 'Municipal Theater',  'Patras',        'Georgiou Sq. 1, Patras',     40);

-- Athens Arena (venue_id=1) — 2 VIP rows (A,B) × 5 seats + 4 Regular rows (C–F) × 5 seats = 30
INSERT INTO seats (venue_id, section_name, row_label, seat_number, seat_type) VALUES
(1,'VIP','A','1','VIP'),(1,'VIP','A','2','VIP'),(1,'VIP','A','3','VIP'),(1,'VIP','A','4','VIP'),(1,'VIP','A','5','VIP'),
(1,'VIP','B','1','VIP'),(1,'VIP','B','2','VIP'),(1,'VIP','B','3','VIP'),(1,'VIP','B','4','VIP'),(1,'VIP','B','5','VIP'),
(1,'REGULAR','C','1','REGULAR'),(1,'REGULAR','C','2','REGULAR'),(1,'REGULAR','C','3','REGULAR'),(1,'REGULAR','C','4','REGULAR'),(1,'REGULAR','C','5','REGULAR'),
(1,'REGULAR','D','1','REGULAR'),(1,'REGULAR','D','2','REGULAR'),(1,'REGULAR','D','3','REGULAR'),(1,'REGULAR','D','4','REGULAR'),(1,'REGULAR','D','5','REGULAR'),
(1,'REGULAR','E','1','REGULAR'),(1,'REGULAR','E','2','REGULAR'),(1,'REGULAR','E','3','REGULAR'),(1,'REGULAR','E','4','REGULAR'),(1,'REGULAR','E','5','REGULAR'),
(1,'REGULAR','F','1','REGULAR'),(1,'REGULAR','F','2','REGULAR'),(1,'REGULAR','F','3','REGULAR'),(1,'REGULAR','F','4','REGULAR'),(1,'REGULAR','F','5','REGULAR');

-- Metropolis Hall (venue_id=2) — 2 VIP rows × 4 + 4 Regular rows × 4 = 24
INSERT INTO seats (venue_id, section_name, row_label, seat_number, seat_type) VALUES
(2,'VIP','A','1','VIP'),(2,'VIP','A','2','VIP'),(2,'VIP','A','3','VIP'),(2,'VIP','A','4','VIP'),
(2,'VIP','B','1','VIP'),(2,'VIP','B','2','VIP'),(2,'VIP','B','3','VIP'),(2,'VIP','B','4','VIP'),
(2,'REGULAR','C','1','REGULAR'),(2,'REGULAR','C','2','REGULAR'),(2,'REGULAR','C','3','REGULAR'),(2,'REGULAR','C','4','REGULAR'),
(2,'REGULAR','D','1','REGULAR'),(2,'REGULAR','D','2','REGULAR'),(2,'REGULAR','D','3','REGULAR'),(2,'REGULAR','D','4','REGULAR'),
(2,'REGULAR','E','1','REGULAR'),(2,'REGULAR','E','2','REGULAR'),(2,'REGULAR','E','3','REGULAR'),(2,'REGULAR','E','4','REGULAR'),
(2,'REGULAR','F','1','REGULAR'),(2,'REGULAR','F','2','REGULAR'),(2,'REGULAR','F','3','REGULAR'),(2,'REGULAR','F','4','REGULAR');

-- Municipal Theater (venue_id=3) — 1 VIP row × 4 + 4 Regular rows × 4 = 20
INSERT INTO seats (venue_id, section_name, row_label, seat_number, seat_type) VALUES
(3,'VIP','A','1','VIP'),(3,'VIP','A','2','VIP'),(3,'VIP','A','3','VIP'),(3,'VIP','A','4','VIP'),
(3,'REGULAR','B','1','REGULAR'),(3,'REGULAR','B','2','REGULAR'),(3,'REGULAR','B','3','REGULAR'),(3,'REGULAR','B','4','REGULAR'),
(3,'REGULAR','C','1','REGULAR'),(3,'REGULAR','C','2','REGULAR'),(3,'REGULAR','C','3','REGULAR'),(3,'REGULAR','C','4','REGULAR'),
(3,'REGULAR','D','1','REGULAR'),(3,'REGULAR','D','2','REGULAR'),(3,'REGULAR','D','3','REGULAR'),(3,'REGULAR','D','4','REGULAR'),
(3,'REGULAR','E','1','REGULAR'),(3,'REGULAR','E','2','REGULAR'),(3,'REGULAR','E','3','REGULAR'),(3,'REGULAR','E','4','REGULAR');

-- ─── Users Seed Data ─────────────────────────────────────────────────────────
INSERT INTO users (user_id, first_name, last_name, email, password_hash, role, status) VALUES
(1, 'John', 'Customer', 'user@musictick.com', '04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb', 'CUSTOMER', 'ACTIVE'),
(2, 'Bob', 'Organizer', 'organizer@musictick.com', '0bf7ab78559a04941f158a11b00afaf6a8b22f90cff387edbc8e1d7a9b99cca0', 'ORGANIZER', 'ACTIVE'),
(3, 'Alice', 'Admin', 'admin@musictick.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN', 'ACTIVE');

-- ─── Concerts Seed Data ──────────────────────────────────────────────────────
INSERT INTO concerts (concert_id, organizer_id, venue_id, title, description, concert_date, status, average_rating) VALUES
(1, 2, 1, 'Athens Rock Fest 2026', 'Big rock night at Athens Arena', '2026-06-20 20:00:00', 'APPROVED', 4.80),
(2, 2, 2, 'Metropolis Electro Wave', 'Synthwave vibe at Metropolis Hall', '2026-07-15 21:30:00', 'APPROVED', 4.50),
(3, 2, 3, 'Patras Classical Gala', 'Beautiful symphony gala', '2026-08-05 19:00:00', 'APPROVED', 4.90);

-- ─── Ticket Types Seed Data ──────────────────────────────────────────────────
INSERT INTO ticket_types (ticket_type_id, concert_id, name, price, quantity) VALUES
(1, 1, 'REGULAR', 35.00, 20),
(2, 1, 'VIP', 75.00, 10),
(3, 2, 'REGULAR', 35.00, 16),
(4, 2, 'VIP', 75.00, 8),
(5, 3, 'REGULAR', 35.00, 16),
(6, 3, 'VIP', 75.00, 4);

-- ─── Forum Posts Seed Data ───────────────────────────────────────────────────
INSERT INTO forum_posts (post_id, concert_id, user_id, title, content) VALUES
(201, 1, 1, 'Rock Fest Question', 'Are cameras allowed at the Athens Rock Fest?'),
(202, 1, 2, 'Re: Rock Fest Question', 'Yes, small digital cameras are allowed, but no professional gear.');