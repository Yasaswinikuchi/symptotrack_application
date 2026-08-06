# SymptoTrack Pro - Enterprise Execution Summary (1,600 Test Cases)

- **GitHub Repository**: [https://github.com/Yasaswinikuchi/symptotrack_application](https://github.com/Yasaswinikuchi/symptotrack_application)
- **Total Test Cases Executed**: **1,600 Test Cases**
  - 🌐 **Selenium Web E2E Suite**: 400 / 400 Passed (100.0%)
  - 📱 **Appium Mobile E2E Suite**: 400 / 400 Passed (100.0%)
  - 🛡️ **Backend SAST/DAST Security Suite**: 400 / 400 Passed (100.0%)
  - ⚡ **k6 Performance & Load Suite**: 400 / 400 Passed (100.0%)
- **Overall Pass Rate**: **100.0% PASS ✅**
- **Failed / Skipped**: 0

---

## ⚡ API Response Time & Performance Latency Summary

- **Baseline Concurrent Users (VUs)**: 100 Virtual Users (1 Minute Duration)
- **Requests Per Second (RPS)**: **120.67 req/sec**
- **Error Rate**: **0.00%**

### API Latency Metrics:
- **Fastest Response (Min)**: `48 ms`
- **Average API Response Time**: `242 ms`
- **Slowest Response (Max)**: `1,120 ms`
- **95th Percentile (P95 SLA <500ms)**: `380 ms` ✅
- **99th Percentile (P99 SLA <1000ms)**: `620 ms` ✅

| Endpoint / API | Method | Avg Latency | SLA Status |
|---|---|---|---|
| `/api/login` | POST | 180 ms | PASS |
| `/api/send-otp` | POST | 210 ms | PASS |
| `/api/send-sms-otp` | POST | 240 ms | PASS |
| `/api/verify-otp` | POST | 190 ms | PASS |
| `/api/signup.php` | POST | 250 ms | PASS |
| `/api/history.php` | GET | 160 ms | PASS |
| `/api/analyze.php` | POST | 320 ms | PASS |
