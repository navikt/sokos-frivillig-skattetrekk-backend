import dotenv from "dotenv";

dotenv.config()

const tokenxConfig = {
    discoveryUrl: process.env.TOKEN_X_WELL_KNOWN_URL || null,
    clientID: process.env.TOKEN_X_CLIENT_ID || null,
    privateJwk: process.env.TOKEN_X_PRIVATE_JWK || null,
    endpoint: process.env.TOKEN_X_TOKEN_ENDPOINT || null,
    tokenEndpointAuthMethod: 'private_key_jwt',
    tokenEndpointAuthSigningAlg: 'RS256'
};

export default { tokenxConfig }