const https = require('https');

const options = {
    hostname: 'generativelanguage.googleapis.com',
    path: '/v1beta/models?key=AIzaSyCMI8PCgPe9S7OE0iviR41tjR8fEMEOt8I',
    method: 'GET'
};

const req = https.request(options, (res) => {
    let body = '';
    res.on('data', (chunk) => body += chunk);
    res.on('end', () => {
        console.log(`Status Code: ${res.statusCode}`);
        const data = JSON.parse(body);
        if (data.models) {
            data.models.forEach(model => {
                console.log(`- ${model.name}`);
                if (model.supportedGenerationMethods) {
                    console.log(`  Methods: ${model.supportedGenerationMethods.join(', ')}`);
                }
            });
        } else {
            console.log('Response:', body);
        }
    });
});

req.on('error', (error) => {
    console.error('Error:', error);
});

req.end();
