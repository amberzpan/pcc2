const http = require('http');

function testCommentAPI() {
  const options = {
    hostname: 'localhost',
    port: 5173,
    path: '/api/comment/post/1?sort=time_desc',
    method: 'GET'
  };
  
  const req = http.request(options, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      console.log('Status:', res.statusCode);
      console.log('Response:', data.substring(0, 500));
    });
  });
  
  req.on('error', e => console.error('Error:', e.message));
  req.end();
}

console.log('Testing comment API with sort parameter...');
testCommentAPI();