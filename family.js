let defaultFamilyMembers = [
    { name: "Self (Primary)", age: 28, gender: "Male", blood: "O+ Positive", relation: "Self", conditions: "None", phone: "+91-9876543210", meds: "None", healthScore: "95% Excellent", isPrimary: true },
    { name: "Yasaswini Kuchi (Self)", age: 25, gender: "Female", blood: "B+ Positive", relation: "Self", conditions: "None", phone: "", meds: "Vitamin D3", healthScore: "95% Excellent", isPrimary: false }
];

function getStoredMembers() {
    try {
        const data = localStorage.getItem('symtotrack_family_members');
        if (data) return JSON.parse(data);
    } catch(e) {}
    return defaultFamilyMembers;
}

function saveMembers(members) {
    localStorage.setItem('symtotrack_family_members', JSON.stringify(members));
}

let activeEditIndex = -1;

function renderFamilyList() {
    const members = getStoredMembers();
    const listDiv = document.getElementById('memberList');
    if (!listDiv) return;

    listDiv.innerHTML = members.map((m, idx) => {
        if (m.isPrimary) {
            return `
            <div class="member-card">
                <div class="member-card-top">
                    <div class="member-info-left">
                        <div class="avatar-circle-white">
                            <svg class="icon-blue" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                        </div>
                        <div>
                            <div class="member-name">${m.name}</div>
                            <div class="member-details-text">Age: ${m.age || 28} • ${m.gender || 'Male'} • ${m.blood || 'O+ Positive'}</div>
                        </div>
                    </div>
                    <button class="btn-active-badge">Active</button>
                </div>
            </div>`;
        }

        const ageText = m.age ? `Age: ${m.age}` : 'Age: —';
        const genderText = m.gender ? m.gender : '—';
        const bloodText = m.blood ? `Blood: ${m.blood}` : 'Blood: —';

        return `
        <div class="member-card" onclick="editMember(${idx})">
            <div class="member-card-top">
                <div class="member-info-left">
                    <div class="avatar-circle-white">
                        <svg class="icon-purple" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </div>
                    <div>
                        <div class="member-name">${m.name}</div>
                        <div class="member-details-text">${ageText} • ${genderText} • ${bloodText}</div>
                    </div>
                </div>
                <button class="btn-settings-gear">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
                </button>
            </div>
            <div class="edit-hint">
                <span>✏️ Tap anywhere on card to edit</span>
            </div>
        </div>`;
    }).join('');
}

function openMemberModal() {
    activeEditIndex = -1;
    document.getElementById('modalTitle').innerText = "Add Family Member";
    document.getElementById('memName').value = "";
    document.getElementById('memAge').value = "";
    document.getElementById('memConditions').value = "";
    document.getElementById('memPhone').value = "";
    document.getElementById('memMeds').value = "";
    document.getElementById('memberModal').classList.add('open');
}

function editMember(idx) {
    activeEditIndex = idx;
    const members = getStoredMembers();
    const target = members[idx];
    if (!target) return;

    document.getElementById('modalTitle').innerText = `Edit: ${target.name}`;
    document.getElementById('memName').value = target.name || "";
    document.getElementById('memAge').value = target.age || "";
    if (target.gender) document.getElementById('memGender').value = target.gender;
    if (target.blood) document.getElementById('memBlood').value = target.blood;
    if (target.relation) document.getElementById('memRelation').value = target.relation;
    if (target.conditions) document.getElementById('memConditions').value = target.conditions;
    if (target.phone) document.getElementById('memPhone').value = target.phone;
    if (target.meds) document.getElementById('memMeds').value = target.meds;
    if (target.healthScore) document.getElementById('memHealthScore').value = target.healthScore;

    document.getElementById('memberModal').classList.add('open');
}

function closeMemberModal() {
    document.getElementById('memberModal').classList.remove('open');
}

function saveFamilyMember() {
    const name = document.getElementById('memName').value.trim();
    const age = document.getElementById('memAge').value.trim();
    const gender = document.getElementById('memGender').value;
    const blood = document.getElementById('memBlood').value;
    const relation = document.getElementById('memRelation').value;
    const conditions = document.getElementById('memConditions').value.trim();
    const phone = document.getElementById('memPhone').value.trim();
    const meds = document.getElementById('memMeds').value.trim();
    const healthScore = document.getElementById('memHealthScore').value;

    if (!name) {
        alert("Please enter full name.");
        return;
    }

    let members = getStoredMembers();
    const newObj = {
        name, age, gender, blood, relation, conditions, phone, meds, healthScore,
        isPrimary: activeEditIndex >= 0 ? members[activeEditIndex].isPrimary : false
    };

    if (activeEditIndex >= 0 && members[activeEditIndex]) {
        members[activeEditIndex] = newObj;
    } else {
        members.push(newObj);
    }

    saveMembers(members);
    renderFamilyList();
    closeMemberModal();
}

document.addEventListener('DOMContentLoaded', () => {
    renderFamilyList();
});
