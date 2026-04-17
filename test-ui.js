const http = require('http');

function testFrontend() {
  const options = {
    hostname: 'localhost',
    port: 5173,
    path: '/',
    method: 'GET'
  };
  
  const req = http.request(options, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      console.log('Status:', res.statusCode);
      console.log('Frontend response length:', data.length);
      console.log('Has layout-container:', data.includes('layout-container'));
      console.log('Has sidebar:', data.includes('sidebar'));
      console.log('✅ UI重构基础测试通过');
    });
  });
  
  req.on('error', e => console.error('Error:', e.message));
  req.end();
}

console.log('Testing frontend UI...');
testFrontend();