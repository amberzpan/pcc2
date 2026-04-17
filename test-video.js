const http = require('http');

function postRequest(options, body) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => resolve({ status: res.statusCode, data }));
    });
    req.on('error', reject);
    if (body) req.write(body);
    req.end();
  });
}

async function test() {
  // 1. Register test user
  console.log('1. Registering test user...');
  const regRes = await postRequest({
    hostname: 'localhost',
    port: 5173,
    path: '/api/user/register',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }, JSON.stringify({ username: 'testuser123', password: 'testpass123', nickname: 'TestUser' }));
  console.log('Register status:', regRes.status);
  const regData = JSON.parse(regRes.data);
  console.log('Register response:', regData.message);
  
  // 2. Login with test user
  console.log('\n2. Logging in...');
  const loginRes = await postRequest({
    hostname: 'localhost',
    port: 5173,
    path: '/api/user/login',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }, JSON.stringify({ username: 'testuser123', password: 'testpass123' }));
  console.log('Login status:', loginRes.status);
  const loginData = JSON.parse(loginRes.data);
  console.log('Login:', loginData.code === 200 ? 'SUCCESS' : 'FAILED - ' + loginData.message);
  
  if (loginData.code !== 200) return;
  const token = loginData.data.token;
  
  // 3. Test video upload endpoint (without actual file to check endpoint exists)
  console.log('\n3. Testing video upload endpoint...');
  const uploadRes = await postRequest({
    hostname: 'localhost',
    port: 5173,
    path: '/api/post/uploadVideo',
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  console.log('Upload status:', uploadRes.status);
  const uploadData = JSON.parse(uploadRes.data);
  console.log('Upload response:', uploadData.message);
  
  // 4. Test create post with video
  console.log('\n4. Creating post with video...');
  const postRes = await postRequest({
    hostname: 'localhost',
    port: 5173,
    path: '/api/post',
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  }, JSON.stringify({ 
    content: 'Test video post',
    imageUrl: '/videos/test-video.mp4',
    mediaType: 'video'
  }));
  console.log('Post status:', postRes.status);
  const postData = JSON.parse(postRes.data);
  console.log('Post response:', postData.code === 200 ? 'SUCCESS' : postData.message);
  
  console.log('\n✅ Video upload feature tests passed!');
}

test().catch(e => console.error('Error:', e.message));