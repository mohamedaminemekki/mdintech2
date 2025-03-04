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