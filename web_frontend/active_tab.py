import os
import re

nav_files = {
    "dashboard.html": "dashboard.html",
    "symptoms.html": "symptoms.html",
    "nearby.html": "nearby.html",
    "history.html": "history.html",
    "profile.html": "profile.html"
}

for filename, active_link in nav_files.items():
    filepath = os.path.join("d:\\symtoweb", filename)
    if not os.path.exists(filepath):
        continue
        
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
        
    # Replace `<a href="active_link">` with `<a href="active_link" class="active">`
    content = content.replace(f'<a href="{active_link}">', f'<a href="{active_link}" class="active">')
    
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Set active tab for {filename}")
