import { custom, Issuer } from 'openid-client';
import config from "./config.js";

const client = async () => {
    let proxyAgent = null;

    const azureConfig = config.azureConfig;

    const issuer = await Issuer.discover(azureConfig.discoveryUrl);
    const jwk = JSON.parse(azureConfig.privateJwk);
    const azureClient = new issuer.Client(
        {
            client_id: azureConfig.clientID,
            token_endpoint_auth_method: azureConfig.tokenEndpointAuthMethod,
            token_endpoint_auth_signing_alg: azureConfig.tokenEndpointAuthSigningAlg,
        },
        jwk
    );

    azureClient[custom.http_options] = function (options) {
        options.agent = proxyAgent;
        return options;
    };
    return azureClient;
};

const azureTokenEndpoint = async () => {
    const azureConfig = {
        discoveryUrl: process.env.AZURE_APP_WELL_KNOWN_URL,
        clientID: process.env.AZURE_APP_CLIENT_ID,
        privateJwk: process.env.AZURE_APP_JWKS,
        tokenEndpointAuthMethod: 'private_key_jwt',
    };
    const issuer = await Issuer.discover(azureConfig.discoveryUrl);

    return issuer.token_endpoint;
};

const getOnBehalfOfAccessToken = async (azureAuthClient, bearerToken, backendScope, azureTokenURL) => {
    const backendTokenSet = await azureAuthClient.grant(
        {
            grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            client_assertion_type: 'urn:ietf:params:oauth:client-assertion-type:jwt-bearer',
            requested_token_use: 'on_behalf_of',
            scope: backendScope,
            assertion: bearerToken,
        },
        {
            clientAssertionPayload: {
                aud: [azureTokenURL],
            },
        }
    );
    return backendTokenSet.access_token;
};

export default { client, azureTokenEndpoint, getOnBehalfOfAccessToken };