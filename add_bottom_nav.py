import os
import re

nav_template = """
    <!-- Mobile App Bottom Navigation Bar -->
    <nav class="bottom-nav">
        <a href="dashboard.html" class="nav-item {home}">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            <span>Home</span>
        </a>
        <a href="symptoms.html" class="nav-item {symptoms}">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
            <span>Symptoms</span>
        </a>
        <a href="history.html" class="nav-item {history}">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span>History</span>
        </a>
        <a href="nearby.html" class="nav-item {nearby}">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <span>Nearby</span>
        </a>
        <a href="profile.html" class="nav-item {profile}">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <span>Profile</span>
        </a>
    </nav>
"""

files = ['symptoms.html', 'history.html', 'nearby.html', 'profile.html', 'reminders.html', 'family.html', 'heatmap.html', 'timeline.html', 'chatbot.html', 'symptobot.html', 'scan.html', 'voice.html', 'alerts.html']

for fname in files:
    if not os.path.exists(fname): continue
    with open(fname, 'r', encoding='utf-8') as f:
        content = f.read()
    if '<nav class="bottom-nav">' in content: continue

    h = 'active' if fname in ['dashboard.html', 'index.html'] else ''
    s = 'active' if fname in ['symptoms.html', 'scan.html', 'voice.html'] else ''
    hi = 'active' if fname in ['history.html', 'timeline.html', 'history_result.html'] else ''
    n = 'active' if fname in ['nearby.html'] else ''
    p = 'active' if fname in ['profile.html', 'family.html', 'reminders.html', 'alerts.html'] else ''

    nav = nav_template.format(home=h, symptoms=s, history=hi, nearby=n, profile=p)
    content = re.sub(r'(</body>)', nav + r'\n\1', content, flags=re.IGNORECASE)
    with open(fname, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f'Added bottom-nav to {fname}')
