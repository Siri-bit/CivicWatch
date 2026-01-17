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
        const data = JSON.parse(body);
        if (data.models) {
            // Log to console for capture
            data.models.forEach(model => console.log(model.name));
        }
    });
});

req.on('error', (error) => {
    console.error('Error:', error);
});

req.end();
