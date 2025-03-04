CREATE TABLE users (
                       CIN INT PRIMARY KEY,  -- Unique identification number for the user
                       Name VARCHAR(100) NOT NULL,  -- User's full name
                       Email VARCHAR(255) UNIQUE NOT NULL,  -- Email should be unique
                       Password VARCHAR(255) NOT NULL,  -- Hashed password (BCrypt)
                       Role ENUM('ADMIN', 'USER') NOT NULL,  -- User roles (previously had 'TRAINER', 'MANAGER' but now removed)
                       Phone VARCHAR(20) NULL,  -- User's phone number (nullable)
                       Address VARCHAR(255) NULL,  -- Address field (nullable)
                       City VARCHAR(100) NULL,  -- City of residence (nullable)
                       State VARCHAR(100) NULL,  -- State of residence (nullable)
                       Status TINYINT(1) DEFAULT 1,  -- User status (active/inactive), using TINYINT instead of BOOLEAN
                       pathtopic TEXT NULL,  -- New column to store path topic
                       birthday DATE NULL  -- New column to store user's birth date
);

CREATE TABLE parking (
                         ID INT AUTO_INCREMENT PRIMARY KEY,   -- Unique identifier for each parking
                         Name VARCHAR(255) NOT NULL,          -- Name of the parking lot
                         Localisation VARCHAR(255) NOT NULL,  -- Location details
                         Capacity INT NOT NULL                -- Number of parking slots available
);
CREATE TABLE parking_slot (
                              SlotID INT AUTO_INCREMENT PRIMARY KEY,   -- Unique identifier for parking slot
                              ParkingID INT NOT NULL,                  -- Foreign key referencing parking
                              SlotName VARCHAR(50) NOT NULL,           -- Name of the slot (e.g., A1, B2)
                              Available BOOLEAN DEFAULT TRUE,          -- Slot availability (true/false)
                              FOREIGN KEY (ParkingID) REFERENCES parking(ID) ON DELETE CASCADE
);
CREATE TABLE parking_ticket (
                                TicketID INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for ticket
                                User_ID INT NOT NULL,                    -- Foreign key referencing users
                                Parking_ID INT NOT NULL,                  -- Foreign key referencing parking
                                Parking_Slot_ID INT NOT NULL,             -- Foreign key referencing slot
                                Issuing_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Time of issue
                                Expiration_Date TIMESTAMP NOT NULL,       -- Expiry time
                                Status BOOLEAN DEFAULT TRUE,              -- Ticket status (active/inactive)
                                FOREIGN KEY (User_ID) REFERENCES users(CIN) ON DELETE CASCADE,
                                FOREIGN KEY (Parking_ID) REFERENCES parking(ID) ON DELETE CASCADE,
                                FOREIGN KEY (Parking_Slot_ID) REFERENCES parking_slot(SlotID) ON DELETE CASCADE
);

CREATE TABLE `posts` (
                         `id` int(11) NOT NULL,
                         `title` varchar(255) DEFAULT NULL,
                         `content` text NOT NULL,
                         `author_cin` varchar(255) DEFAULT NULL,
                         `created_at` datetime NOT NULL,
                         `image_url` varchar(255) DEFAULT NULL,
                         `category` varchar(50) DEFAULT NULL
)
CREATE TABLE `recu` (
                        `id` int(11) NOT NULL,
                        `facture_id` int(11) DEFAULT NULL,
                        `date_paiement` date DEFAULT NULL,
                        `montant` decimal(10,2) DEFAULT NULL
)
CREATE TABLE `facture` (
                           `id` int(11) NOT NULL,
                           `date_facture` date NOT NULL,
                           `date_limite_paiement` date NOT NULL,
                           `prix_fact` float NOT NULL,
                           `type_facture` varchar(50) NOT NULL,
                           `state` tinyint(1) DEFAULT 0,
                           `date_paiement` date DEFAULT NULL,
                           `user_cin` char(8) NOT NULL
)
CREATE TABLE `likes` (
                         `user_cin` varchar(20) NOT NULL,
                         `post_id` int(11) NOT NULL,
                         `created_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
)
CREATE TABLE `comments` (
                            `id` int(11) NOT NULL,
                            `post_id` int(11) NOT NULL,
                            `author_cin` varchar(20) NOT NULL,
                            `content` text DEFAULT NULL,
                            `created_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
)


--transport


-- 2. Table badges
CREATE TABLE `badges` (
                          `id` INT(11) NOT NULL AUTO_INCREMENT,
    -- Remplacement de user_id par user_cin
                          `user_cin` INT(11) DEFAULT NULL,
                          `badge_level` INT(11) DEFAULT 1,
                          `badge_type` VARCHAR(50) DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          CONSTRAINT `badges_fk_users` FOREIGN KEY (`user_cin`) REFERENCES `users` (`CIN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Table cities (pour stocker des localisations géographiques)
CREATE TABLE `cities` (
                          `name` VARCHAR(100) NOT NULL,
                          `latitude` DOUBLE NOT NULL,
                          `longitude` DOUBLE NOT NULL,
                          PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Table payments
CREATE TABLE `payments` (
                            `payment_id` INT(11) NOT NULL AUTO_INCREMENT,
                            `amount` DECIMAL(10,2) NOT NULL,
                            `method` ENUM('credit_card','paypal','cash') NOT NULL,
                            `payment_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `reservation_id` INT(11) NOT NULL,
    -- Remplacement de user_id par user_cin
                            `user_cin` INT(11) NOT NULL,
                            PRIMARY KEY (`payment_id`),
                            KEY `reservation_id` (`reservation_id`),
                            CONSTRAINT `payments_fk_users` FOREIGN KEY (`user_cin`) REFERENCES `users` (`CIN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Table reservations
CREATE TABLE `reservations` (
                                `id` INT(11) NOT NULL AUTO_INCREMENT,
    -- Remplacement de user_id par user_cin
                                `user_cin` INT(11) NOT NULL,
                                `trip_id` INT(11) NOT NULL,
                                `reservation_time` DATETIME DEFAULT NULL,
                                `status` VARCHAR(20) DEFAULT 'Pending',
                                `transport_id` INT(11) NOT NULL,
                                `seat_number` INT(11) NOT NULL,
                                `payment_status` VARCHAR(20) DEFAULT 'Pending',
                                `seat_type` VARCHAR(20) DEFAULT 'Standard',
                                PRIMARY KEY (`id`),
                                KEY `transport_id` (`transport_id`),
                                KEY `fk_reservation_trip` (`trip_id`),
                                CONSTRAINT `reservations_fk_users` FOREIGN KEY (`user_cin`) REFERENCES `users` (`CIN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Table transport_types
CREATE TABLE `transport_types` (
                                   `transport_id` INT(50) NOT NULL AUTO_INCREMENT,
                                   `name` VARCHAR(50) NOT NULL,
                                   `description` TEXT DEFAULT NULL,
                                   `capacity` INT(11) NOT NULL,
                                   PRIMARY KEY (`transport_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Table trips
CREATE TABLE `trips` (
                         `id` INT(100) NOT NULL AUTO_INCREMENT,
                         `departure` VARCHAR(100) NOT NULL,
                         `destination` VARCHAR(100) NOT NULL,
                         `departure_time` DATETIME NOT NULL,
                         `arrival_time` DATETIME NOT NULL,
                         `price` DECIMAL(10,2) NOT NULL,
                         `transport_id` INT(50) NOT NULL,
                         `transport_name` VARCHAR(255) DEFAULT NULL,
                         `date` DATE DEFAULT NULL,
                         `distance` DOUBLE NOT NULL DEFAULT 0,
                         `capacity` INT(11) NOT NULL DEFAULT 50,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `id` (`id`),
                         KEY `trips_ibfk_1` (`transport_id`),
                         CONSTRAINT `trips_fk_transport_types` FOREIGN KEY (`transport_id`) REFERENCES `transport_types` (`transport_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Table villes
CREATE TABLE `villes` (
                          `id` INT(11) NOT NULL AUTO_INCREMENT,
                          `nom` VARCHAR(100) NOT NULL,
                          `histoire` TEXT DEFAULT NULL,
                          `anecdotes` TEXT DEFAULT NULL,
                          `activites` TEXT DEFAULT NULL,
                          `gastronomie` TEXT DEFAULT NULL,
                          `nature` TEXT DEFAULT NULL,
                          `histoire_interactive` TEXT DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
