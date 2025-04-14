const concurrently = require('concurrently');


const mockType = process.argv[2] || '';

concurrently(
    [
        {command: 'npm run dev', prefixColor: 'blue', name: 'frontend-skattetrekk'},
        {command: 'node mock/server.cjs ' + mockType, prefixColor: 'magenta', name: 'json-server'}
    ]
    ,
    {
        killOthers: ['failure', 'success']
    }
);
