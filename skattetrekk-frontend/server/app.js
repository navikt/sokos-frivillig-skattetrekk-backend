import express from "express";
import tokenx from "./tokenx.js";
import dotenv from "dotenv"
import path from "path";
import {fileURLToPath} from "url";

const basePath = "/utbetaling/skattetrekk";

const app = express();

const PORT = process.env.PORT || 8080;

dotenv.config()

let client = await tokenx.client();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const buildPath = path.resolve(__dirname, "../dist")
app.use(basePath, express.static(buildPath));

app.get(
     basePath + '/api/initSkattetrekk',
     async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken);
        let newHeaders = req.headers;
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        const response = await fetch(process.env.SKATTETREKK_BACKEND_URL + "/api/skattetrekk", {
             method: req.method,
             headers: newHeaders
         });

        const body = await response.json();

        const statuskode = response.status
        res.status(statuskode).send(body)
    }
);

async function getTokenValue(idToken) {
    return await tokenx.getTokenExchangeAccessToken(
        client,
        idToken,
        process.env.SKATTETREKK_BACKEND_AUDIENCE
    );
}

app.get('/internal/health/liveness', (req, res) => {
    res.send({
        "status":"UP"
    });
});

app.get('/internal/health/readiness', (req, res) => {
    res.send({
        "status":"UP"
    });
});

app.get('*', (req, res) => {
    res.sendFile(path.resolve(__dirname, '../dist', 'index.html'));
});

app.listen(PORT, () => console.log("Server started"));
