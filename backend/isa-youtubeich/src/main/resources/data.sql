-- Inicijalni podaci za dev profil

-- Lozinke za user-e: 123

INSERT INTO USERS (username, password, first_name, last_name, email, enabled, created_at, address)
VALUES (
    'user1',
    '$2a$04$Vbug2lwwJGrvUXTj6z7ff.97IzVBkrJ1XfApfGNl.Z695zqcnPYra',
    'Marko',
    'Markovic',
    'user1@example.com',
    true,
    '2017-10-01 21:58:58',
    '{"city": "Belgrade", "street": "Kolarceva"}'
);

INSERT INTO USERS (username, password, first_name, last_name, email, enabled, created_at, address)
VALUES (
    'user2',
    '$2a$04$Vbug2lwwJGrvUXTj6z7ff.97IzVBkrJ1XfApfGNl.Z695zqcnPYra',
    'Petar',
    'Petrovic',
    'user2@example.com',
    true,
    '2019-10-01 21:58:58',
    '{"city": "Novi Sad", "street": "Balzakova"}'
);

INSERT INTO ROLE (name) VALUES ('ROLE_USER');

INSERT INTO USER_ROLE (user_id, role_id) VALUES (1, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (2, 1);