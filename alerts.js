document.addEventListener('DOMContentLoaded', async () => {
    const alertsContainer = document.getElementById('alertsContainer');
    
    // Check if user is logged in
    const userStr = localStorage.getItem('user');
    let userId = 1; // Default fallback for mock
    if (userStr) {
        try {
            const user = JSON.parse(userStr);
            if (user.id) {
                userId = user.id;
            }
        } catch (e) {
            console.error("Failed to parse user", e);
        }
    }

    try {
        const response = await fetch(`../app_backend/api/get_appointments.php?user_id=${userId}`);
        const data = await response.json();
        
        if (data.success && data.data && data.data.length > 0) {
            alertsContainer.style.justifyContent = 'flex-start';
            alertsContainer.style.padding = '0 24px 80px 24px';
            alertsContainer.innerHTML = '';
            
            data.data.forEach(appointment => {
                const card = document.createElement('div');
                card.style.background = '#FFFFFF';
                card.style.borderRadius = '16px';
                card.style.padding = '16px';
                card.style.marginBottom = '16px';
                card.style.width = '100%';
                card.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)';
                card.style.border = '1px solid #E2E8F0';
                
                card.innerHTML = `
                    <div style="font-weight: 700; color: #0F172A; margin-bottom: 4px; font-size: 16px;">${appointment.hospital_name}</div>
                    <div style="color: #64748B; font-size: 14px; margin-bottom: 8px;">${appointment.problem_description || 'General Checkup'}</div>
                    <div style="color: #2E6CEB; font-size: 14px; font-weight: 600;">Date: ${appointment.appointment_date}</div>
                    <div style="color: #2E6CEB; font-size: 14px; font-weight: 600;">Time: ${appointment.time_slot}</div>
                `;
                alertsContainer.appendChild(card);
            });
        } else {
            alertsContainer.innerHTML = '<p style="color: #6B7280; font-size: 15px; text-align: center;">No active alerts or appointments.</p>';
        }
    } catch (error) {
        console.error('Failed to fetch alerts:', error);
        alertsContainer.innerHTML = '<p style="color: #6B7280; font-size: 15px; text-align: center;">No active alerts or appointments.</p>';
    }
});
