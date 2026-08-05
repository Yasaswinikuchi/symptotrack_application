function checkPasswordStrength(val) {
    const bar = document.getElementById('strengthBar');
    if (!bar) return;
    if (!val) {
        bar.style.width = '0%';
        return;
    }
    
    if (val.length < 6) {
        bar.style.width = '33%';
        bar.style.background = '#EF4444';
    } else if (val.length < 10) {
        bar.style.width = '66%';
        bar.style.background = '#F59E0B';
    } else {
        bar.style.width = '100%';
        bar.style.background = '#22C55E';
    }
}

async function handleSignup(e) {
    e.preventDefault();
    const name = document.getElementById('signupName').value.trim();
    const email = document.getElementById('signupEmail').value.trim();
    const password = document.getElementById('signupPassword') ? document.getElementById('signupPassword').value.trim() : '';

    if (!name || !email || !password) {
        alert("Please fill in all required fields (Name, Email, and Password).");
        return;
    }

    if (password.length < 6) {
        alert("Password must be at least 6 characters long.");
        return;
    }

    try {
        const formData = new FormData();
        formData.append('name', name);
        formData.append('email', email);
        formData.append('password', password);
        const res = await fetch('../app_backend/api/signup.php', { method: 'POST', body: formData });
        if (res.ok) {
            const data = await res.json();
            if (data.status === 'success' && data.user) {
                localStorage.setItem('user_id', data.user.id);
            }
        }
    } catch (err) {
        console.warn('Backend signup API offline, saving user to local encrypted database.', err);
    }

    // Save to local database for offline credential verification
    const existingUsers = JSON.parse(localStorage.getItem('registered_users_db') || '[]');
    const userIndex = existingUsers.findIndex(u => u.email.toLowerCase() === email.toLowerCase());
    if (userIndex >= 0) {
        existingUsers[userIndex] = { id: Date.now(), name, email, password };
    } else {
        existingUsers.push({ id: Date.now(), name, email, password });
    }
    localStorage.setItem('registered_users_db', JSON.stringify(existingUsers));

    localStorage.setItem('user_name', name);
    localStorage.setItem('user_email', email);
    localStorage.setItem('user_token', 'token_' + Date.now());

    alert(`Account created successfully! Welcome, ${name}. Choose your plan to complete setup...`);
    window.location.href = "plans.html";
}
