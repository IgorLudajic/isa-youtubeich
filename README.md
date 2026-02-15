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

## Monitoring (Prometheus & Grafana)

Aplikacija uključuje integrisano praćenje performansi pomoću Prometheus-a i Grafane.

### Pristup alatima

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (podrazumevani kredencijali: admin/admin)
- **Actuator metrike**: http://localhost:8080/actuator/prometheus

### Dostupne metrike

1. **HikariCP konekcije ka bazi**
   - `hikaricp_connections_active` - broj aktivnih konekcija
   - `hikaricp_connections_idle` - broj idle konekcija
   - `hikaricp_connections_pending` - broj zahteva koji čekaju konekciju

2. **CPU korišćenje**
   - `process_cpu_usage` - procenat CPU korišćenja JVM procesa
   - `system_cpu_usage` - ukupno sistemsko CPU korišćenje

3. **Aktivni korisnici**
   - `active_users_24h` - broj jedinstvenih korisnika aktivnih u poslednjih 24 sata

### Grafana Dashboard

Pre-konfigurisani dashboard "Youtubeich Application Monitoring" automatski se učitava i prikazuje:
- Graf aktivnih i idle konekcija ka bazi
- CPU korišćenje u realnom vremenu sa prosekom kroz 5 minuta
- Broj aktivnih korisnika (stat i timeline)
- Request rate vs connection pool (za detekciju visokog opterećenja 200+ req/sec)

