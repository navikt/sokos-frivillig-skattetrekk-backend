# sokos-frivillig-skattetrekk-backend

* [1. Funksjonelle krav](#1-funksjonelle-krav)
* [2. Utviklingsmiljø](#2-utviklingsmiljø)
* [3. Programvarearkitektur](#3-programvarearkitektur)
* [4. Deployment](#4-deployment)
* [5. Autentisering](#5-autentisering)
* [6. Drift og støtte](#6-drift-og-støtte)
* [7. Henvendelser](#7-henvendelser)

---

# 1. Funksjonelle Krav

Backend for [sokos-frivillig-skattetrekk-frontend](https://github.com/navikt/sokos-frivillig-skattetrekk-frontend).
Integrerer mot [sokos-oppdrag-proxy](https://github.com/navikt/sokos-oppdrag-proxy) som er inngang til utbetaling hvor forvaltning av frivillig skattetrekk skjer.

# 2. Utviklingsmiljø

### Forutsetninger

* Java 25
* [Gradle](https://gradle.org/)

### Bygge prosjekt

`./gradlew build installDist`

### Lokal utvikling

Kjør `./setupLocalEnvironment.sh` for å sette opp prosjektet lokalt.

# 3. Programvarearkitektur

TODO: Legg til en enkel beskrivelse av programvarearkitektur her.

# 4. Deployment

Distribusjon av tjenesten er gjort med bruk av Github Actions.
[sokos-oppdrag CI / CD](https://github.com/navikt/sokos-frivillig-skattetrekk-backend/actions)

Når main branch oppdateres vil Github Actions bygge og deploye til dev-gcp og prod-gcp.
Har også mulighet for å deploye manuelt til testmiljø ved å deploye PR.


# 5. Autentisering

Applikasjonen bruker [AzureAD](https://docs.nais.io/security/auth/azure-ad/) autentisering

# 6. Drift og støtte

### Logging

Feilmeldinger og infomeldinger som ikke innheholder sensitive data logges til [Grafana Loki](https://docs.nais.io/observability/logging/#grafana-loki).  
Sensitive meldinger logges til [Team Logs](https://doc.nais.io/observability/logging/how-to/team-logs/).

### Kubectl

For dev-gcp:

```shell script
kubectl config use-context dev-gcp
kubectl get pods -n skatt-og-trekk | grep sokos-frivillig-skattetrekk-backend
kubectl logs -f sokos-frivillig-skattetrekk-backend-<POD-ID> --namespace skatt-og-trekk -c sokos-frivillig-skattetrekk-backend
```

For prod-gcp:

```shell script
kubectl config use-context prod-gcp
kubectl get pods -n skatt-og-trekk | grep sokos-frivillig-skattetrekk-backend
kubectl logs -f sokos-frivillig-skattetrekk-backend-<POD-ID> --namespace skatt-og-trekk -c sokos-frivillig-skattetrekk-backend
```

### Alarmer

Applikasjonen bruker [Grafana Alerting](https://grafana.nav.cloud.nais.io/alerting/) for overvåkning og varsling.
Dette er konfigurert via NAIS sin [alerting-integrasjon](https://doc.nais.io/observability/alerts).

Alarmene overvåker metrics som:

- HTTP-feilrater
- JVM-metrikker

Varsler blir sendt til følgende Slack-kanaler:

- Dev-miljø: [#utbetaling-skatt-og-trekk-alarm-dev](https://nav-it.slack.com/archives/C0C047NGLDB)
- Prod-miljø: [#utbetaling-skatt-og-trekk-alarm-prod](https://nav-it.slack.com/archives/C0C08KX79R8)

### Grafana

- [sokos-frivillig-skattetrekk-backend](https://grafana.nav.cloud.nais.io/goto/sfhr27?orgId=default)

---

# 7. Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på Github.
Interne henvendelser kan sendes via Slack i kanalen [#utbetaling](https://nav-it.slack.com/archives/CKZADNFBP)
