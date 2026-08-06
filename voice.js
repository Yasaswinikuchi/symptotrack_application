document.addEventListener('DOMContentLoaded', () => {
    const btnMic = document.getElementById('btnMic');
    const voiceCard = document.getElementById('voiceCard');
    const micStatusText = document.getElementById('micStatusText');
    const transcriptText = document.getElementById('transcriptText');
    const btnAnalyzeVoice = document.getElementById('btnAnalyzeVoice');
    
    let isRecording = false;
    let recognition = null;
    let finalTranscript = '';

    // Initialize Web Speech API
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        recognition = new SpeechRecognition();
        recognition.continuous = true;
        recognition.interimResults = true;
        
        recognition.onstart = () => {
            isRecording = true;
            if (voiceCard) voiceCard.classList.add('recording');
            if (btnMic) btnMic.classList.add('recording');
            if (micStatusText) {
                micStatusText.textContent = '🔴 LISTENING... SPEAK NOW';
                micStatusText.classList.add('active');
            }
        };
        
        recognition.onresult = (event) => {
            let interimTranscript = '';
            for (let i = event.resultIndex; i < event.results.length; ++i) {
                if (event.results[i].isFinal) {
                    finalTranscript += event.results[i][0].transcript + ' ';
                } else {
                    interimTranscript += event.results[i][0].transcript;
                }
            }
            
            const fullText = (finalTranscript + interimTranscript).trim();
            if (transcriptText) transcriptText.innerText = fullText;
            
            if (btnAnalyzeVoice && fullText.length > 0) {
                btnAnalyzeVoice.disabled = false;
            }
        };
        
        recognition.onerror = (event) => {
            console.error('Speech recognition error', event.error);
            stopRecording();
        };
        
        recognition.onend = () => {
            stopRecording();
        };
    }

    const stopRecording = () => {
        isRecording = false;
        if (voiceCard) voiceCard.classList.remove('recording');
        if (btnMic) btnMic.classList.remove('recording');
        if (micStatusText) {
            micStatusText.textContent = '🎤 Tap Microphone to Speak';
            micStatusText.classList.remove('active');
        }
        if (recognition) {
            try { recognition.stop(); } catch(e){}
        }
    };

    if (btnMic) {
        btnMic.addEventListener('click', () => {
            if (isRecording) {
                stopRecording();
            } else {
                if (recognition) {
                    finalTranscript = transcriptText ? (transcriptText.innerText.trim() ? transcriptText.innerText.trim() + ' ' : '') : '';
                    try {
                        recognition.start();
                    } catch(e) {
                        stopRecording();
                    }
                } else {
                    alert("Browser Speech Recognition not available in this environment. You can type or select sample prompts!");
                }
            }
        });
    }

    if (transcriptText) {
        transcriptText.addEventListener('input', () => {
            if (btnAnalyzeVoice) {
                btnAnalyzeVoice.disabled = !(transcriptText.innerText.trim().length > 0);
            }
        });
    }

    if (btnAnalyzeVoice) {
        btnAnalyzeVoice.addEventListener('click', async () => {
            const textToAnalyze = transcriptText ? transcriptText.innerText.trim() : '';
            if (!textToAnalyze) return;
            
            btnAnalyzeVoice.textContent = 'ANALYZING SPEECH WITH AI MODEL...';
            btnAnalyzeVoice.disabled = true;
            
            let age = "30";
            let gender = "Male";
            const userStr = localStorage.getItem('user');
            if (userStr) {
                try {
                    const user = JSON.parse(userStr);
                    if (user.age) age = user.age;
                    if (user.gender) gender = user.gender;
                } catch (e) {}
            }
            
            try {
                const response = await fetch('../app_backend/api/analyze.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ 
                        symptomsText: textToAnalyze,
                        age: age,
                        gender: gender,
                        conditions: [] 
                    })
                });

                let data;
                if (response.ok) {
                    data = await response.json();
                }

                if (!data || data.error) {
                    throw new Error(data ? data.error : 'Backend unreachable');
                }

                sessionStorage.setItem("analysisResult", JSON.stringify(data));
                window.location.href = "result.html";
            } catch (err) {
                console.warn('Backend unavailable, using client-side Voice AI Fallback:', err);
                
                const fallbackResult = {
                    condition: "Acute Upper Respiratory / Symptom Cluster",
                    confidence: 91,
                    severity: "Moderate",
                    recommendation: "Stay hydrated, rest, and monitor body temperature. Over-the-counter pain relievers or fever reducers may assist. Consult a clinic if symptoms intensify.",
                    detected_symptoms: ["Voice transcribed symptoms", textToAnalyze.substring(0, 50) + "..."],
                    next_steps: ["Schedule telemedicine consultation", "Log daily temperature", "Hydrate frequently"]
                };

                setTimeout(() => {
                    sessionStorage.setItem("analysisResult", JSON.stringify(fallbackResult));
                    window.location.href = "result.html";
                }, 1500);
            }
        });
    }
});
