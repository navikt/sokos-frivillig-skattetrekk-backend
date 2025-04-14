FROM gcr.io/distroless/nodejs22-debian12
ENV NODE_ENV=production

WORKDIR /app
USER nonroot

COPY ./skattetrekk-frontend/package.json .
COPY ./skattetrekk-frontend/package-lock.json .
COPY ./skattetrekk-frontend/node_modules/ node_modules/
COPY ./skattetrekk-frontend/server server/
COPY ./skattetrekk-frontend/dist/ dist/

ENTRYPOINT ["/nodejs/bin/node", "./server/app.js"]
