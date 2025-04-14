import { Issuer } from 'openid-client';
import config from "./config.js";
import { v4 as uuid } from 'uuid';
import jwt from "jsonwebtoken";
import nodeJose from "node-jose";


const client = async () => {

    const tokenxConfig = config.tokenxConfig;
    const issuer = await Issuer.discover(tokenxConfig.discoveryUrl);
    const jwk = JSON.parse(tokenxConfig.privateJwk);
    return new issuer.Client(
        {
            client_id: tokenxConfig.clientID,
            token_endpoint_auth_method: tokenxConfig.tokenEndpointAuthMethod,
            token_endpoint_auth_signing_alg: tokenxConfig.tokenEndpointAuthSigningAlg,
        },
        { keys: [jwk] }
    );
};

const getTokenExchangeAccessToken = async (tokenxClient, bearerToken, backendAudience) => {
    let backendTokenSet = undefined;
    const now = Math.floor(Date.now() / 1000);
    const additionalClaims = {
        clientAssertionPayload: {
            nbf: now,
            aud: [config.tokenxConfig.endpoint]
        },
    };
    const audience = config.tokenxConfig.endpoint
    const assertionToken = await createClientAssertion(audience)
    backendTokenSet = await tokenxClient.grant(
        {
            grant_type: 'urn:ietf:params:oauth:grant-type:token-exchange',
            client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
            client_assertion: assertionToken,
            subject_token_type: 'urn:ietf:params:oauth:token-type:jwt',
            audience: backendAudience,
            subject_token: bearerToken,
        },
        additionalClaims
    );

    return backendTokenSet.access_token;
};

const createClientAssertion = async (audience) => {
    const now = Math.floor(Date.now() / 1000);

    const payload = {
        sub: config.tokenxConfig.clientID,
        iss: config.tokenxConfig.clientID,
        aud: audience,
        jti: uuid(),
        nbf: now,
        iat: now,
        exp: now + 60, // max 120
    };

    const key = await asKey(config.tokenxConfig.privateJwk);

    const options = {
        algorithm: 'RS256',
        header: {
            kid: key.kid,
            typ: 'JWT',
            alg: 'RS256',
        },
    };

    return jwt.sign(payload, key.toPEM(true), options);
};

const asKey = async (jwk) => {
    if (!jwk) {
        print('JWK Mangler');
        throw Error('JWK Mangler');
    }

    return nodeJose.JWK.asKey(jwk).then((key) => {
        return Promise.resolve(key);
    });
};


export default { client, getTokenExchangeAccessToken };