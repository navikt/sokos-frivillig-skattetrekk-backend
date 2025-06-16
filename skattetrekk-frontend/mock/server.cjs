const express = require('express')
const fs = require("fs");
const app = express()
const port = 3000
const cors = require('cors');

const whitelist = "http://localhost:8080"

app.use(cors({
    credentials: true,
    origin: function (origin, callback) {
        if (whitelist.indexOf(origin) !== -1) {
            callback(null, true)
        } else {
            callback(new Error('Not allowed by CORS'))
        }
    }
}));

let init = JSON.parse(fs.readFileSync('mock/skattetrekkInitResponse.json', 'utf8'));

app.get('/utbetaling/skattetrekk/api/skattetrekk', (req, res) => {
    console.log("Kjører initiate");
    console.log(process.argv)
    res.send(init)
})

app.post('/utbetaling/skattetrekk/api/skattetrekk', (req, res) => {
    console.log("Kjører send");
    res.status(503).send()
})


app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})