# isa-youtubeich
Projekat iz predmeta Internet softverske arhitekture

## Preduslovi

- Docker
- Docker Compose

## Razvojno okruženje

Klonirajte repozitorijum i pozicionirajte se u korenski direktorijum projekta.

## Pokretanje aplikacije

### Razvojni režim

Za pokretanje razvojnog okruženja sa hot reloading-om:

```bash
docker-compose --profile dev up
```

Ovim se pokreću sledeće usluge:

- `app-dev`: Spring Boot backend sa hot reloading-om na portu 8080 (debug na 5005)
- `db`: PostgreSQL baza podataka
- `frontend-dev`: Next.js frontend sa hot reloading-om na portu 3000

### Produkcioni režim

Za pokretanje produkcionog okruženja:

```bash
docker-compose --profile prod up
```

Ovim se pokreću:

- `app`: Optimizovani Spring Boot backend na portu 8080
- `db`: PostgreSQL baza podataka
- `frontend`: Optimizovani Next.js frontend na portu 3000

