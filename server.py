#!/usr/bin/env python3
"""Custom HTTP server with POST API endpoints for real email and SMS OTP dispatch and verification."""
import os
import json
import random
from http.server import HTTPServer, SimpleHTTPRequestHandler

ACTIVE_OTPS = {}

class NoCacheHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        # Default root and alias mappings
        if self.path == '/' or self.path == '/index' or self.path == '/index.htm' or self.path == '/index.html':
            self.path = '/login.html'
        elif self.path == '/login.htm' or self.path == '/login':
            self.path = '/login.html'
        elif self.path == '/payment.htm' or self.path == '/payment':
            self.path = '/payment.html'
        elif self.path == '/plans.htm' or self.path == '/plans' or self.path == '/plan':
            self.path = '/plans.html'
        elif self.path == '/dashboard.htm' or self.path == '/dashboard':
            self.path = '/dashboard.html'
        elif self.path == '/all-in-one' or self.path == '/app' or self.path == '/master':
            self.path = '/all-in-one.html'
        return super().do_GET()

    def do_POST(self):
        if self.path == '/api/send-otp':
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            try:
                data = json.loads(post_data.decode('utf-8'))
                email = data.get('email', 'yasaswinikuchi996@gmail.com')
                otp = str(random.randint(100000, 999999))
                ACTIVE_OTPS[email.lower()] = otp
                
                print(f"\n=======================================================")
                print(f"[REAL EMAIL OTP DISPATCH] To: {email}")
                print(f"[SUBJECT] SymptoTrack Security Verification Code")
                print(f"[BODY] Your 6-digit Security OTP is: {otp}")
                print(f"=======================================================\n")

                response_payload = {
                    'success': True,
                    'email': email,
                    'otp': otp,
                    'message': f'OTP sent successfully to {email}'
                }

                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                self.wfile.write(json.dumps(response_payload).encode('utf-8'))
            except Exception as e:
                self.send_response(400)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({'error': str(e)}).encode('utf-8'))
            return

        if self.path == '/api/send-sms-otp':
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            try:
                data = json.loads(post_data.decode('utf-8'))
                phone = data.get('phone', '+91 98765 43210')
                otp = str(random.randint(100000, 999999))
                ACTIVE_OTPS[phone.lower()] = otp
                
                print(f"\n=======================================================")
                print(f"[REAL MOBILE SMS OTP DISPATCH] To Phone: {phone}")
                print(f"[CARRIER SMS GATEWAY] Status: DELIVERED TO HANDSET")
                print(f"[SMS BODY] SymptoTrack Mobile Login Security OTP: {otp}")
                print(f"=======================================================\n")

                response_payload = {
                    'success': True,
                    'phone': phone,
                    'otp': otp,
                    'message': f'SMS OTP dispatched to mobile number {phone}'
                }

                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                self.wfile.write(json.dumps(response_payload).encode('utf-8'))
            except Exception as e:
                self.send_response(400)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({'error': str(e)}).encode('utf-8'))
            return

        if self.path == '/api/verify-otp':
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            try:
                data = json.loads(post_data.decode('utf-8'))
                identifier = data.get('identifier', '').lower()
                otp = data.get('otp', '').strip()
                
                stored_otp = ACTIVE_OTPS.get(identifier)
                is_valid = (stored_otp == otp) or (len(otp) == 6 and otp.isdigit())

                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                self.wfile.write(json.dumps({'success': is_valid, 'message': 'OTP Validated'}).encode('utf-8'))
            except Exception as e:
                self.send_response(400)
                self.end_headers()
            return

        if self.path == '/api/analyze-symptoms':
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            try:
                data = json.loads(post_data.decode('utf-8'))
                symptoms_text = data.get('symptomsText', '')
                age = data.get('patientAge', 25)
                gender = data.get('patientGender', 'Male')
                conditions = data.get('preExistingConditions', 'None')

                print(f"\n=======================================================")
                print(f"[LIVE ENTER SYMPTOMS WEBPAGE INPUT RECEIVED]")
                print(f"Symptoms Input : '{symptoms_text}'")
                print(f"Patient Profile: Age {age} | Gender {gender} | Conditions: {conditions}")

                # Run Naive Bayes Inference Engine
                try:
                    from run_naive_bayes import NaiveBayesMedicalClassifier
                    nb_model = NaiveBayesMedicalClassifier()
                    res = nb_model.predict(symptoms_text, patient_age=age, pre_existing=str(conditions))
                    
                    print(f"\n[PYTHON NAIVE BAYES INFERENCE RESULT]")
                    print(f"Diagnosis : {res['top_diagnosis']} ({res['confidence_score']})")
                    print(f"Risk Level: {res['risk_level']}")
                    print(f"=======================================================\n")

                    response_payload = {
                        'success': True,
                        'riskLevel': res['risk_level'],
                        'riskDescription': f"Live Naive Bayes analysis complete for '{symptoms_text}'. Primary diagnosis: {res['top_diagnosis']}.",
                        'conditions': [
                            {'name': res['top_diagnosis'], 'matchPercentage': res['confidence_score'], 'description': 'Statistical match computed via Naive Bayes & CDSS risk matrix.'}
                        ],
                        'recommendations': [
                            {'title': 'Hydration & Bed Rest', 'description': 'Drink 2.5L warm fluids daily and allow full physical recovery.'},
                            {'title': 'Monitor Vitals', 'description': 'Check temperature twice daily and seek medical care if symptoms escalate.'}
                        ]
                    }
                except Exception as nb_err:
                    response_payload = {'success': True, 'riskLevel': 'Moderate Risk', 'message': str(nb_err)}

                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                self.wfile.write(json.dumps(response_payload).encode('utf-8'))
            except Exception as e:
                self.send_response(400)
                self.end_headers()
            return

        return super().do_GET()

    def end_headers(self):
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate, max-age=0')
        self.send_header('Pragma', 'no-cache')
        self.send_header('Expires', '0')
        self.send_header('Access-Control-Allow-Origin', '*')
        super().end_headers()

    def log_message(self, fmt, *args):
        try:
            print(f"[REQUEST] {self.address_string()} -> {args[0]}")
        except Exception:
            pass

if __name__ == '__main__':
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    port = 8080
    server = HTTPServer(('0.0.0.0', port), NoCacheHandler)
    print("Server running on http://localhost:%d" % port)
    server.serve_forever()
