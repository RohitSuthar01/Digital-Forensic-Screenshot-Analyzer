const axios = require('axios');

async function test() {
  try {
    // 1. Login
    const loginRes = await axios.post('http://localhost:8091/api/auth/login', 'username=admin&password=Admin@123', {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    });
    const cookies = loginRes.headers['set-cookie'];
    console.log('Login successful. Cookies:', cookies);
    
    // 2. Fetch dashboard
    const res = await axios.get('http://localhost:8091/api/dashboard', {
      headers: {
        Cookie: cookies.join('; ')
      }
    });
    console.log('Dashboard response:', res.status, res.data);
  } catch (err) {
    if (err.response) {
      console.error('API Error:', err.response.status, err.response.data);
    } else {
      console.error('Error:', err.message);
    }
  }
}

test();
