const https = require('https');
const fs = require('fs');

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
            const names = data.models.map(m => m.name).join('\n');
            fs.writeFileSync('clean_models.txt', names, 'utf8');
            console.log('Written to clean_models.txt');
        }
    });
});

req.on('error', (error) => {
    console.error('Error:', error);
});

req.end();
