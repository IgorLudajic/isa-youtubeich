-- Inicijalni podaci za dev profil
-- Lozinke za sve user-e (ukljucujuci admina): 123

-- 1. Insert Roles (Idempotent)
INSERT INTO ROLE (name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM ROLE WHERE name = 'ROLE_USER');

INSERT INTO ROLE (name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM ROLE WHERE name = 'ROLE_ADMIN');

-- 2. Insert Users (Idempotent)
-- User 1
INSERT INTO USERS (username, password, first_name, last_name, email, enabled, created_at, address)
SELECT 'user1', '$2a$04$Vbug2lwwJGrvUXTj6z7ff.97IzVBkrJ1XfApfGNl.Z695zqcnPYra', 'Marko', 'Markovic', 'user1@example.com', true, '2017-10-01 21:58:58', '{"city": "Belgrade", "street": "Kolarceva"}'
WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE username = 'user1');

-- User 2
INSERT INTO USERS (username, password, first_name, last_name, email, enabled, created_at, address)
SELECT 'user2', '$2a$04$Vbug2lwwJGrvUXTj6z7ff.97IzVBkrJ1XfApfGNl.Z695zqcnPYra', 'Petar', 'Petrovic', 'user2@example.com', true, '2019-10-01 21:58:58', '{"city": "Novi Sad", "street": "Balzakova"}'
WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE username = 'user2');

-- Admin User
INSERT INTO USERS (username, password, first_name, last_name, email, enabled, created_at, address)
SELECT 'admin', '$2a$04$Vbug2lwwJGrvUXTj6z7ff.97IzVBkrJ1XfApfGNl.Z695zqcnPYra', 'Admin', 'Adminovic', 'admin@example.com', true, '2023-01-01 12:00:00', '{"city": "Nis", "street": "Pobedina"}'
WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE username = 'admin');


-- 3. Insert User_Roles (Idempotent & Dynamic IDs)
-- We use subqueries to find the IDs, so we don't rely on 1, 2, 3 being fixed.

-- Link user1 to ROLE_USER
INSERT INTO USER_ROLE (user_id, role_id)
SELECT
    (SELECT id FROM USERS WHERE username = 'user1'),
    (SELECT id FROM ROLE WHERE name = 'ROLE_USER')
WHERE NOT EXISTS (
    SELECT 1 FROM USER_ROLE
    WHERE user_id = (SELECT id FROM USERS WHERE username = 'user1')
      AND role_id = (SELECT id FROM ROLE WHERE name = 'ROLE_USER')
);

-- Link user2 to ROLE_USER
INSERT INTO USER_ROLE (user_id, role_id)
SELECT
    (SELECT id FROM USERS WHERE username = 'user2'),
    (SELECT id FROM ROLE WHERE name = 'ROLE_USER')
WHERE NOT EXISTS (
    SELECT 1 FROM USER_ROLE
    WHERE user_id = (SELECT id FROM USERS WHERE username = 'user2')
      AND role_id = (SELECT id FROM ROLE WHERE name = 'ROLE_USER')
);

-- Link admin to ROLE_ADMIN
INSERT INTO USER_ROLE (user_id, role_id)
SELECT
    (SELECT id FROM USERS WHERE username = 'admin'),
    (SELECT id FROM ROLE WHERE name = 'ROLE_ADMIN')
WHERE NOT EXISTS (
    SELECT 1 FROM USER_ROLE
    WHERE user_id = (SELECT id FROM USERS WHERE username = 'admin')
      AND role_id = (SELECT id FROM ROLE WHERE name = 'ROLE_ADMIN')
);

-- Link admin to ROLE_USER (Optional: if admin should also have basic user rights)
INSERT INTO USER_ROLE (user_id, role_id)
SELECT
    (SELECT id FROM USERS WHERE username = 'admin'),
    (SELECT id FROM ROLE WHERE name = 'ROLE_USER')
WHERE NOT EXISTS (
    SELECT 1 FROM USER_ROLE
    WHERE user_id = (SELECT id FROM USERS WHERE username = 'admin')
      AND role_id = (SELECT id FROM ROLE WHERE name = 'ROLE_USER')
);