import os
import socket

# Automatically determine the target directory relative to this script
base_dir = os.path.dirname(os.path.abspath(__file__))
directory = os.path.join(base_dir, "app", "src", "main", "java", "com", "symtotrack")

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Doesn't need to actually connect/send data to discover route
        s.connect(('10.255.255.255', 1))
        ip = s.getsockname()[0]
    except Exception:
        ip = '127.0.0.1'
    finally:
        s.close()
    return ip

new_ip = get_local_ip()
print(f"[IP Update] Local PC IP address detected: {new_ip}")

# Old hardcoded IPs that might reside in config or code files
old_ips = ["10.205.187.109", "10.48.101.109", "10.0.2.2", "127.0.0.1"]

if os.path.exists(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith((".kt", ".java", ".xml")):
                filepath = os.path.join(root, file)
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.read()
                
                updated = False
                for old_ip in old_ips:
                    if old_ip in content and old_ip != new_ip:
                        content = content.replace(old_ip, new_ip)
                        updated = True
                
                if updated:
                    with open(filepath, "w", encoding="utf-8") as f:
                        f.write(content)
                    print(f"[IP Update] Updated IP inside: {filepath}")
    print("[IP Update] Finished check.")
else:
    print(f"[IP Update] Warning: Directory {directory} not found.")
