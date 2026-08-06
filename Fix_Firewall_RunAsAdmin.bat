@echo off
echo Adding Windows Firewall rules for XAMPP Apache...

:: Remove old rules if any
netsh advfirewall firewall delete rule name="XAMPP Apache HTTP Port 80" >nul 2>&1
netsh advfirewall firewall delete rule name="XAMPP Apache HTTPS Port 443" >nul 2>&1

:: Add inbound rules for port 80 and 443 on all profiles
netsh advfirewall firewall add rule name="XAMPP Apache HTTP Port 80" dir=in action=allow protocol=TCP localport=80 profile=any
netsh advfirewall firewall add rule name="XAMPP Apache HTTPS Port 443" dir=in action=allow protocol=TCP localport=443 profile=any

echo.
echo Done! Your Android phone can now reach XAMPP on port 80.
pause
