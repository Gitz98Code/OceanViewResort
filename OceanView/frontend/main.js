// main.js - shared logic for login, protection & logout

const loginForm = document.getElementById('loginForm');

if (loginForm) {
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!username || !password) {
      document.getElementById('message').textContent = 'Please fill in all fields';
      document.getElementById('message').className = 'message error';
      return;
    }

    try {
      const response = await fetch('http://localhost:8080/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      const data = await response.json();

      if (data.status === 'success') {
        document.getElementById('message').textContent = 'Login successful! Redirecting...';
        document.getElementById('message').className = 'message success';
        localStorage.setItem('loggedIn', 'true');
        localStorage.setItem('username', username);
        setTimeout(() => {
          window.location.href = 'dashboard.html';
        }, 1500);
      } else {
        document.getElementById('message').textContent = data.message || 'Login failed';
        document.getElementById('message').className = 'message error';
      }
    } catch (err) {
      document.getElementById('message').textContent = 'Cannot connect to server';
      document.getElementById('message').className = 'message error';
      console.error(err);
    }
  });
}

// Protect pages - redirect to login if not authenticated
if (!localStorage.getItem('loggedIn') && !location.pathname.includes('index.html')) {
  location.href = 'index.html';
}

// Improved Logout function
function logout() {
  // Clear all stored login data
  localStorage.removeItem('loggedIn');
  localStorage.removeItem('username');
  localStorage.clear();          // extra safety
  sessionStorage.clear();        // extra safety

  // Optional: show a quick message (looks nicer)
  const messageEl = document.getElementById('message');
  if (messageEl) {
    messageEl.textContent = 'Logging out...';
    messageEl.className = 'message success';
  }

  // Redirect to login page after a short delay (better UX)
  setTimeout(() => {
    window.location.href = 'index.html';  // ← change to your actual login filename if different
  }, 800);
}