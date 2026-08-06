function switchPayTab(type, elem) {
    document.querySelectorAll('.pay-tab').forEach(t => t.classList.remove('active'));
    elem.classList.add('active');

    document.getElementById('tabCardView').style.display = type === 'card' ? 'block' : 'none';
    document.getElementById('tabUpiView').style.display = type === 'upi' ? 'block' : 'none';
    document.getElementById('tabNetView').style.display = type === 'netbank' ? 'block' : 'none';
}

function updateCardPreview() {
    const num = document.getElementById('inputCardNum').value || '4532 8812 9012 3456';
    const exp = document.getElementById('inputCardExp').value || '12/28';
    const name = document.getElementById('inputCardName').value || 'YASASWINI KUCHI';

    document.getElementById('cardDisplayNum').innerText = num;
    document.getElementById('cardDisplayExp').innerText = exp;
    document.getElementById('cardDisplayName').innerText = name.toUpperCase();
}

function payViaUPIApp(appName) {
    alert(`Opening ${appName} app for UPI payment authentication...`);
    openBankOTPModal();
}

function openBankOTPModal() {
    document.getElementById('bankOtpModal').style.display = 'flex';
}

async function executePayment() {
    const btn = document.getElementById('btnConfirmBankOtp');
    btn.innerText = '⏳ Processing Bank Payment...';
    btn.disabled = true;

    const hospital = localStorage.getItem('booked_hospital') || 'Metro City General Hospital';
    const fee = localStorage.getItem('booked_fee') || '500';
    const txnId = 'TXN-' + Math.floor(100000 + Math.random() * 900000);
    const dateToday = new Date().toISOString().split('T')[0];

    try {
        await fetch('../app_backend/api/save_appointment.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                hospital_name: hospital,
                payment_id: txnId,
                problem_description: 'General OPD Queue Token',
                date: dateToday,
                time_slot: '10:30 AM'
            })
        });
    } catch (e) {
        console.warn('Backend save_appointment offline, recording locally.', e);
    }

    setTimeout(() => {
        document.getElementById('bankOtpModal').style.display = 'none';

        // Save appointment token & VIP status in localStorage for Dashboard
        localStorage.setItem('has_active_appointment', 'true');
        localStorage.setItem('your_token', '18');
        localStorage.setItem('now_serving', '14');
        localStorage.setItem('is_vip_pro', 'true');

        document.getElementById('txnIdDisplay').innerText = txnId;
        document.getElementById('receiptTokenDisplay').innerText = '#18';
        document.getElementById('receiptAmountDisplay').innerText = `₹${fee}`;

        document.getElementById('successOverlay').style.display = 'flex';
    }, 1200);
}

function goToDashboard() {
    window.location.href = "dashboard.html";
}

window.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const hospital = params.get('hospital') || localStorage.getItem('booked_hospital') || 'Metro City General Hospital - Live Token';
    const fee = params.get('fee') || localStorage.getItem('booked_fee') || '500';
    const customer = localStorage.getItem('user_name') || 'Yasaswini Kuchi';

    localStorage.setItem('booked_hospital', hospital);
    localStorage.setItem('booked_fee', fee);

    if (document.getElementById('summaryHospital')) document.getElementById('summaryHospital').innerText = hospital;
    if (document.getElementById('summaryFee')) document.getElementById('summaryFee').innerText = `₹${fee}`;
    if (document.getElementById('summaryTotal')) document.getElementById('summaryTotal').innerText = `₹${fee}`;
    if (document.getElementById('summaryCustomer')) document.getElementById('summaryCustomer').innerText = customer;
    if (document.getElementById('btnPayNow')) document.getElementById('btnPayNow').querySelector('span').innerText = `PAY ₹${fee} SECURELY NOW`;
});
