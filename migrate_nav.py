import os
import re

top_nav_html = """
    <!-- Desktop Top Navigation -->
    <nav class="top-nav">
        <a href="dashboard.html" class="nav-brand">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M16 4H24V16H36V24H24V36H16V24H4V16H16V4Z" fill="#2E6CEB"/>
                <path d="M10 24L18 16L24 22L32 12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            SymptoTrack
        </a>
        <div class="nav-links">
            <a href="dashboard.html"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg> Dashboard</a>
            <a href="symptoms.html"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg> Symptoms</a>
            <a href="nearby.html"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg> Nearby Clinics</a>
            <a href="history.html"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> Records</a>
            <a href="profile.html"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg> Profile</a>
        </div>
    </nav>
"""

# HTML files to apply the desktop layout to
dashboard_pages = [
    "dashboard.html", "symptoms.html", "history.html", "nearby.html", 
    "profile.html", "alerts.html", "scan.html", "voice.html", 
    "result.html", "history_result.html"
]

for filename in dashboard_pages:
    filepath = os.path.join("d:\\symtoweb", filename)
    if not os.path.exists(filepath):
        continue
    
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    
    # 1. Remove <nav class="bottom-nav"> ... </nav>
    # Using regex dotall to remove the whole nav block
    content = re.sub(r'<!-- Bottom Navigation -->\s*<nav class="bottom-nav">.*?</nav>', '', content, flags=re.DOTALL)
    content = re.sub(r'<nav class="bottom-nav">.*?</nav>', '', content, flags=re.DOTALL)
    
    # 2. Inject top_nav_html right after <body ...>
    if '<!-- Desktop Top Navigation -->' not in content:
        content = re.sub(r'(<body[^>]*>)', r'\1\n' + top_nav_html, content, count=1)
    
    # Save back
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Updated {filename}")
