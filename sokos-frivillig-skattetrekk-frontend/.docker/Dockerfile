FROM gcr.io/distroless/nodejs22-debian12
ENV NODE_ENV=production

WORKDIR /app
USER nonroot

COPY ./sokos-frivillig-skattetrekk-frontend/package.json .
COPY ./sokos-frivillig-skattetrekk-frontend/package-lock.json .
COPY ./sokos-frivillig-skattetrekk-frontend/node_modules/ node_modules/
COPY ./sokos-frivillig-skattetrekk-frontend/server server/
COPY ./sokos-frivillig-skattetrekk-frontend/dist/ dist/

ENTRYPOINT ["/nodejs/bin/node", "./server/app.js"]
