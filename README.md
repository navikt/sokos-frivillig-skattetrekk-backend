# sokos-frivillig-skattetrekk-backend

# Innholdsoversikt

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

* Java 21
* [Gradle >= 9](https://gradle.org/)

### Bygge prosjekt

`./gradlew build shadowJar`

### Lokal utvikling

Kjør `./setupLocalEnvironment.sh` for å sette opp prosjektet lokalt.

# 3. Programvarearkitektur

[System diagram](./dokumentasjon/system-diagram.md)

# 4. Deployment

Distribusjon av tjenesten er gjort med bruk av Github Actions.
[sokos-oppdrag CI / CD](https://github.com/navikt/sokos-frivillig-skattetrekk-backend/actions)

Push/merge til main branch direkte er ikke mulig. Det må opprettes PR og godkjennes før merge til main branch.
Når PR er merged til main branch vil Github Actions bygge og deploye til dev-fss og prod-fss.
Har også mulighet for å deploye manuelt til testmiljø ved å deploye PR.


# 5. Autentisering

Applikasjonen bruker [AzureAD](https://docs.nais.io/security/auth/azure-ad/) autentisering

# 6. Drift og støtte

### Logging

https://logs.adeo.no.

Feilmeldinger og infomeldinger som ikke innheholder sensitive data logges til [Grafana Loki](https://docs.nais.io/observability/logging/#grafana-loki).  
Sensitive meldinger logges til [Team Logs](https://doc.nais.io/observability/logging/how-to/team-logs/).

### Kubectl

For dev-gcp:

```shell script
kubectl config use-context dev-fss
kubectl get pods -n okonomi | grep sokos-frivillig-skattetrekk-backend
kubectl logs -f sokos-frivillig-skattetrekk-backend-<POD-ID> --namespace okonomi -c sokos-frivillig-skattetrekk-backend
```

For prod-gcp:

```shell script
kubectl config use-context prod-fss
kubectl get pods -n okonomi | grep sokos-frivillig-skattetrekk-backend
kubectl logs -f sokos-frivillig-skattetrekk-backend-<POD-ID> --namespace okonomi -c sokos-frivillig-skattetrekk-backend
```

### Alarmer

Applikasjonen bruker [Grafana Alerting](https://grafana.nav.cloud.nais.io/alerting/) for overvåkning og varsling.

Alarmene overvåker metrics som:

- HTTP-feilrater
- JVM-metrikker

### Grafana

- [appavn](url)

---

# 7. Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på Github.
Interne henvendelser kan sendes via Slack i kanalen [#utbetaling](https://nav-it.slack.com/archives/CKZADNFBP)
