// heatmap.js – Handles SVG body map, data storage, filters, tooltip, side panel, and add‑symptom modal

// Utility to format date strings
function fmtDate(date) {
  const d = new Date(date);
  return d.toISOString().split('T')[0];
}

// Load symptom entries from localStorage – fallback to sample data
function loadSymptoms() {
  const raw = localStorage.getItem('symptotrack_body_symptoms');
  if (raw) return JSON.parse(raw);
  // Sample data covering multiple regions
  return [
    { region: 'head', symptom: 'Headache', date: new Date().setDate(new Date().getDate() - 1), severity: 6 },
    { region: 'chest', symptom: 'Chest tightness', date: new Date().setDate(new Date().getDate() - 3), severity: 5 },
    { region: 'leftArm', symptom: 'Itchy rash', date: new Date().setDate(new Date().getDate() - 5), severity: 4 },
    { region: 'abdomen', symptom: 'Stomach ache', date: new Date().setDate(new Date().getDate() - 2), severity: 7 },
    { region: 'rightLeg', symptom: 'Muscle soreness', date: new Date().setDate(new Date().getDate() - 7), severity: 3 }
  ];
}

function saveSymptoms(data) {
  localStorage.setItem('symptotrack_body_symptoms', JSON.stringify(data));
}

// Filter data based on selected range (days or all)
function filterData(range) {
  const all = loadSymptoms();
  if (range === 'all') return all;
  const days = Number(range);
  const cutoff = new Date();
  cutoff.setDate(cutoff.getDate() - days);
  return all.filter(item => new Date(item.date) >= cutoff);
}

// Compute statistics for the top row
function computeStats(data) {
  const total = data.length;
  const regionCounts = {};
  data.forEach(item => {
    regionCounts[item.region] = (regionCounts[item.region] || 0) + 1;
  });
  const mostAffected = Object.entries(regionCounts).sort((a, b) => b[1] - a[1])[0]?.[0] || '—';
  // Chronic area = region with reports spanning >= 21 days (3 weeks)
  let chronic = '—';
  for (const [region, count] of Object.entries(regionCounts)) {
    const dates = data.filter(i => i.region === region).map(i => fmtDate(i.date));
    const uniqueDays = new Set(dates).size;
    if (uniqueDays >= 21) {
      chronic = region;
      break;
    }
  }
  return { total, mostAffected, chronic };
}

function renderStats(stats) {
  document.getElementById('totalSymptoms').textContent = stats.total;
  document.getElementById('mostAffected').textContent = stats.mostAffected;
  document.getElementById('chronicArea').textContent = stats.chronic;
}

// Map region to color based on count
function getRegionColor(count) {
  if (count === 0) return 'rgba(30,41,59,0.8)'; // dark
  if (count <= 2) return 'rgba(59,130,246,0.4)'; // light blue
  if (count <= 5) return 'rgba(245,158,11,0.6)'; // amber
  return 'rgba(239,68,68,0.8)'; // red
}

function updateRegionColors(data) {
  const counts = {};
  data.forEach(item => {
    counts[item.region] = (counts[item.region] || 0) + 1;
  });
  const svg = document.getElementById('bodyMap');
  const regions = svg.querySelectorAll('.region');
  regions.forEach(el => {
    const cnt = counts[el.id] || 0;
    el.setAttribute('fill', getRegionColor(cnt));
    el.dataset.count = cnt; // store for tooltip
  });
}

// Tooltip handling
function initTooltip() {
  const tooltip = document.getElementById('tooltip');
  const svg = document.getElementById('bodyMap');
  svg.addEventListener('mousemove', e => {
    const target = e.target;
    if (target.classList.contains('region')) {
      const rect = target.getBoundingClientRect();
      const count = target.dataset.count || 0;
      tooltip.style.display = 'block';
      tooltip.style.left = `${e.clientX + 12}px`;
      tooltip.style.top = `${e.clientY + 12}px`;
      tooltip.textContent = `${target.id.charAt(0).toUpperCase() + target.id.slice(1)}: ${count} report(s)`;
    } else {
      tooltip.style.display = 'none';
    }
  });
  svg.addEventListener('mouseleave', () => { tooltip.style.display = 'none'; });
}

// Side panel – show detailed symptoms for a region
function openSidePanel(region, data) {
  const panel = document.getElementById('sidePanel');
  const title = document.getElementById('panelTitle');
  const content = document.getElementById('panelContent');
  title.textContent = `${region.charAt(0).toUpperCase() + region.slice(1)} Details`;
  content.innerHTML = '';
  const filtered = data.filter(item => item.region === region);
  if (filtered.length === 0) {
    content.innerHTML = '<p style="color:#9CA3AF;">No symptoms reported for this area in the selected range.</p>';
  } else {
    filtered.sort((a, b) => new Date(b.date) - new Date(a.date));
    filtered.forEach(item => {
      const div = document.createElement('div');
      div.className = 'symptom-item';
      div.innerHTML = `
        <strong>${item.symptom}</strong><br/>
        <span style="font-size:12px;color:#9CA3AF;">${fmtDate(item.date)} • Severity ${item.severity}</span>
      `;
      content.appendChild(div);
    });
  }
  panel.classList.add('open');
}

function initRegionClicks(filteredData) {
  const svg = document.getElementById('bodyMap');
  svg.addEventListener('click', e => {
    const target = e.target;
    if (target.classList.contains('region')) {
      openSidePanel(target.id, filteredData);
    }
  });
  // Close button
  document.getElementById('btnClosePanel').addEventListener('click', () => {
    document.getElementById('sidePanel').classList.remove('open');
  });
}

// Modal for adding new symptom
function initModal() {
  const modal = document.getElementById('symptomModal');
  const openBtn = document.getElementById('btnAddSymptom');
  const cancelBtn = document.getElementById('btnCancelSymptom');
  const saveBtn = document.getElementById('btnSaveSymptom');

  openBtn.addEventListener('click', () => modal.classList.add('open'));
  cancelBtn.addEventListener('click', () => modal.classList.remove('open'));

  saveBtn.addEventListener('click', () => {
    const region = document.getElementById('symRegion').value;
    const symptom = document.getElementById('symDescription').value.trim();
    const dateVal = document.getElementById('symDate').value;
    const severity = document.getElementById('symSeverity').value;
    if (!region || !symptom || !dateVal) {
      alert('Please fill all required fields');
      return;
    }
    const newEntry = {
      region,
      symptom,
      date: new Date(dateVal).getTime(),
      severity: Number(severity)
    };
    const all = loadSymptoms();
    all.push(newEntry);
    saveSymptoms(all);
    modal.classList.remove('open');
    // Refresh UI according to current filter
    const activeBtn = document.querySelector('#filterBar button.active');
    const filtered = filterData(activeBtn.dataset.range);
    refreshUI(filtered);
  });
}

// Filter bar handling
function initFilterBar() {
  const bar = document.getElementById('filterBar');
  bar.addEventListener('click', e => {
    if (e.target.tagName !== 'BUTTON') return;
    const range = e.target.dataset.range;
    Array.from(bar.querySelectorAll('button')).forEach(btn => btn.classList.toggle('active', btn === e.target));
    const filtered = filterData(range);
    refreshUI(filtered);
  });
}

// Central UI refresh
function refreshUI(filteredData) {
  renderStats(computeStats(filteredData));
  updateRegionColors(filteredData);
  // re‑attach click listener with fresh filtered data
  initRegionClicks(filteredData);
}

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
  initTooltip();
  initModal();
  initFilterBar();
  const activeBtn = document.querySelector('#filterBar button.active');
  const initData = filterData(activeBtn.dataset.range);
  refreshUI(initData);
});
