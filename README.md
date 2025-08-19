# Frivillig Skattetrekk

En webapplikasjon for å administrere frivillig skattetrekk på et utvalg pengestøtter fra NAV.
Backend integrerer mot https://github.com/navikt/sokos-oppdrag-proxy som er inngang til utbetaling hvor forvaltning
av frivillig skattetrekk skjer. Denne applikasjonen presenterer registrert data til bruker i selvbetjening og lar bruker
endre ønsket trekk.

### Forutsetninger
- Node.js 18+ og npm
- Java 21+ og Maven 3.8+

### Arkitektur
- **Frontend**: React 19 + TypeScript + Vite med aksel designsystem komponenter
- **Backend**: Spring Boot (Kotlin) med Maven

### Backend API-endepunkter
- `GET /api/skattetrekk` - Hent skattetrekk og ytelsesinfo
- `POST /api/skattetrekk` - Registrer/endre/stopp frivillig skattetrekk

### Miljøvariabler
- `VITE_FRIVILLIG_SKATTETREKK_INFO_URL` - URL til informasjonsside

### Frontend-utvikling

```bash
cd skattetrekk-frontend

# Installer avhengigheter og start med mock backend
npm install
npm run dev-local

npm run dev                 # Frontend uten mock
npm run build:production    # Produksjonsbygg
npm run lint               # Linting
```

### Backend-utvikling

```bash
cd skattetrekk-backend

mvn clean install    # Bygg
mvn test             # Tester  
mvn spring-boot:run  # Start
```

## Mock Server

For frontend-utvikling uten å kjøre backend, brukes en mock server som simulerer API-endepunktene.

### Konfigurasjon
Mock serveren kjører på port 3000 og serverer testdata fra JSON-filer:

- **Server**: `mock/server.cjs` - Express server med CORS-konfigurasjon
- **Testdata**: `mock/skattetrekkInitResponse.json` - Eksempeldata for API-responser
- **Starter automatisk**: Med `npm run dev-local` startes både frontend og mock samtidig

### Tilpasse testdata
Rediger `mock/skattetrekkInitResponse.json` for å teste ulike scenarier:

```json
{
  "data": {
    "tilleggstrekk": null,
    "fremtidigTilleggstrekk": {
      "sats": 50,
      "satsType": "PROSENT",
      "gyldigFraOgMed": "2023-10-08T00:00:00Z"
    },
    "skattetrekk": {"tabellNr": null, "prosentsats": 50},
    "maxBelop": 5000,
    "maxProsent": 50
  },
  "messages": []
}
```