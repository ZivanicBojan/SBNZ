// Configuration
const API_BASE_URL = 'http://localhost:8080/api';
const ENDPOINTS = {
    forward: {
        demo: `${API_BASE_URL}/forward-chaining/demo`,
        analyze: `${API_BASE_URL}/forward-chaining/analyze`,
        classifySingle: `${API_BASE_URL}/forward-chaining/classify-single`
    },
    backward: {
        demo: `${API_BASE_URL}/backward-chaining/demo`,
        fullAnalysis: `${API_BASE_URL}/backward-chaining/full-analysis`
    },
    cep: {
        demo: `${API_BASE_URL}/cep/demo`,
        coordinated: `${API_BASE_URL}/cep/demo-coordinated`
    }
};

// Tab Management
function showTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remove active class from all buttons
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById(tabName).classList.add('active');
    
    // Add active class to clicked button
    event.target.classList.add('active');
}

// Loading Management
function showLoading() {
    document.getElementById('loading').classList.remove('hidden');
}

function hideLoading() {
    document.getElementById('loading').classList.add('hidden');
}

// Forward Chaining Demo
async function runForwardDemo() {
    const resultsDiv = document.getElementById('forward-results');
    showLoading();
    
    try {
        const response = await fetch(ENDPOINTS.forward.demo);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        
        const data = await response.json();
        displayForwardResults(data, resultsDiv);
    } catch (error) {
        console.error('Forward Demo Error:', error);
        resultsDiv.innerHTML = `<div class="error">❌ Greška: ${error.message}</div>`;
    } finally {
        hideLoading();
    }
}

function displayForwardResults(data, container) {
    let html = '<h3>📊 Forward Chaining Rezultati</h3>';
    
    // Statistics
    html += `
        <div class="stats">
            <div class="stat-card">
                <h3>${data.totalCount}</h3>
                <p>Ukupno vijesti</p>
            </div>
            <div class="stat-card">
                <h3>${data.trustedCount}</h3>
                <p>Pouzdane</p>
            </div>
            <div class="stat-card">
                <h3>${data.suspiciousCount}</h3>
                <p>Sumnjive</p>
            </div>
            <div class="stat-card">
                <h3>${data.potentiallyFakeCount}</h3>
                <p>Pot. lažne</p>
            </div>
        </div>
    `;
    

    
    // News breakdown by category
    html += '<h4>📰 Klasifikacija po kategorijama:</h4>';
    
    if (data.trustedNews && data.trustedNews.length > 0) {
        html += '<h5 style="color: #27ae60;">✅ Pouzdane vijesti:</h5>';
        data.trustedNews.forEach(news => {
            html += createNewsCard(news, 'trusted');
        });
    }
    
    if (data.suspiciousNews && data.suspiciousNews.length > 0) {
        html += '<h5 style="color: #e74c3c;">⚠️ Sumnjive vijesti:</h5>';
        data.suspiciousNews.forEach(news => {
            html += createNewsCard(news, 'suspicious');
        });
    }
    
    if (data.potentiallyFakeNews && data.potentiallyFakeNews.length > 0) {
        html += '<h5 style="color: #f39c12;">⚡ Potencijalno lažne vijesti:</h5>';
        data.potentiallyFakeNews.forEach(news => {
            html += createNewsCard(news, 'potentially-fake');
        });
    }
    
    if (data.unclassifiedNews && data.unclassifiedNews.length > 0) {
        html += '<h5 style="color: #95a5a6;">❓ Neklasifikovane vijesti:</h5>';
        data.unclassifiedNews.forEach(news => {
            html += createNewsCard(news, 'unclassified');
        });
    }
    
    container.innerHTML = html;
}

// Backward Chaining Demo
async function runBackwardDemo() {
    const resultsDiv = document.getElementById('backward-results');
    showLoading();
    
    try {
        const response = await fetch(ENDPOINTS.backward.demo);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        
        const data = await response.json();
        displayBackwardResults(data, resultsDiv);
    } catch (error) {
        console.error('Backward Demo Error:', error);
        resultsDiv.innerHTML = `<div class="error">❌ Greška: ${error.message}</div>`;
    } finally {
        hideLoading();
    }
}

function displayBackwardResults(data, container) {
    let html = '<h3>🔍 Backward Chaining Rezultati</h3>';
    html += '<p><strong>Backward chaining analizira RAZLOGE zašto su vijesti klasifikovane na određeni način.</strong></p>';
    // Prikaz analiziranih vijesti (trusted/suspicious)
    if (data.trustedNews && data.trustedNews.length > 0) {
        html += '<h4 style="color: #27ae60;">✅ Pouzdane vijesti (razlozi):</h4>';
        data.trustedNews.forEach(news => {
            html += createNewsCard(news, 'trusted');
        });
    }
    if (data.suspiciousNews && data.suspiciousNews.length > 0) {
        html += '<h4 style="color: #e74c3c;">⚠️ Sumnjive vijesti (razlozi):</h4>';
        data.suspiciousNews.forEach(news => {
            html += createNewsCard(news, 'suspicious');
        });
    }
    if (data.potentiallyFakeNews && data.potentiallyFakeNews.length > 0) {
        html += '<h4 style="color: #f39c12;">⚡ Potencijalno lažne vijesti:</h4>';
        data.potentiallyFakeNews.forEach(news => {
            html += createNewsCard(news, 'potentially-fake');
        });
    }
    if (data.unclassifiedNews && data.unclassifiedNews.length > 0) {
        html += '<h4 style="color: #95a5a6;">❓ Neklasifikovane vijesti:</h4>';
        data.unclassifiedNews.forEach(news => {
            html += createNewsCard(news, 'unclassified');
        });
    }
    container.innerHTML = html;
}

// CEP Demo
async function runCEPDemo() {
    const resultsDiv = document.getElementById('cep-results');
    showLoading();
    
    try {
        const response = await fetch(ENDPOINTS.cep.demo);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        
        const data = await response.json();
        displayCEPResults(data, resultsDiv, 'CEP Analysis Rezultati');
    } catch (error) {
        console.error('CEP Demo Error:', error);
        resultsDiv.innerHTML = `<div class="error">❌ Greška: ${error.message}</div>`;
    } finally {
        hideLoading();
    }
}

async function runCoordinatedDemo() {
    const resultsDiv = document.getElementById('cep-results');
    showLoading();
    
    try {
        const response = await fetch(ENDPOINTS.cep.coordinated);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        
        const data = await response.json();
        displayCEPResults(data, resultsDiv, 'Koordinisano širenje - CEP');
    } catch (error) {
        console.error('Coordinated Demo Error:', error);
        resultsDiv.innerHTML = `<div class="error">❌ Greška: ${error.message}</div>`;
    } finally {
        hideLoading();
    }
}

function displayCEPResults(data, container, title) {
    let html = `<h3>📊 ${title}</h3>`;
    html += '<p><strong>Complex Event Processing analizira patterns širenja vijesti kroz vrijeme.</strong></p>';
    
    // Statistics
    html += `
        <div class="stats">
            <div class="stat-card">
                <h3>${data.totalEvents}</h3>
                <p>Ukupno evenata</p>
            </div>
            <div class="stat-card">
                <h3>${data.coordinatedCount}</h3>
                <p>Koordinisano širenje</p>
            </div>
            <div class="stat-card">
                <h3>${data.explosiveCount}</h3>
                <p>Eksplozivno širenje</p>
            </div>
            <div class="stat-card">
                <h3>${data.normalCount}</h3>
                <p>Normalno širenje</p>
            </div>
        </div>
    `;
    
    // Analyzed news
    if (data.analyzedNews && data.analyzedNews.length > 0) {
        html += '<h4>📈 Analiza širenja po vijestima:</h4>';
        data.analyzedNews.forEach(news => {
            let cardType = 'normal';
            if (news.distributionType === 'COORDINATED') cardType = 'suspicious';
            else if (news.distributionType === 'EXPLOSIVE') cardType = 'potentially-fake';
            
            html += createNewsCard(news, cardType);
        });
    }
    
    // Distribution type breakdown
    if (data.coordinatedNews && data.coordinatedNews.length > 0) {
        html += '<h5 style="color: #e74c3c;">🕸️ Koordinisano širenje:</h5>';
        data.coordinatedNews.forEach(news => {
            html += createNewsCard(news, 'suspicious');
        });
    }
    
    if (data.explosiveNews && data.explosiveNews.length > 0) {
        html += '<h5 style="color: #f39c12;">💥 Eksplozivno širenje:</h5>';
        data.explosiveNews.forEach(news => {
            html += createNewsCard(news, 'potentially-fake');
        });
    }
    
    if (data.normalNews && data.normalNews.length > 0) {
        html += '<h5 style="color: #27ae60;">📊 Normalno širenje:</h5>';
        data.normalNews.forEach(news => {
            html += createNewsCard(news, 'trusted');
        });
    }
    
    container.innerHTML = html;
}

// Manual Testing
async function testForwardManual() {
    const news = getManualFormData();
    if (!news) return;
    
    const resultsDiv = document.getElementById('manual-results');
    showLoading();
    
    try {
        const response = await fetch(ENDPOINTS.forward.classifySingle, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(news)
        });
        
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        
        const result = await response.json();
        
        let html = '<h3>🚀 Forward Chaining - Manual Test</h3>';
        html += createNewsCard(result, getConfidenceClass(result.confidence));
        
        resultsDiv.innerHTML = html;
    } catch (error) {
        console.error('Manual Forward Test Error:', error);
        resultsDiv.innerHTML = `<div class="error">❌ Greška: ${error.message}</div>`;
    } finally {
        hideLoading();
    }
}

async function testBackwardManual() {
    const news = getManualFormData();
    if (!news) return;
    
    // First classify with forward, then analyze with backward
    const resultsDiv = document.getElementById('manual-results');
    showLoading();
    try {
        // Step 1: Forward classification
        const forwardResponse = await fetch(ENDPOINTS.forward.classifySingle, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(news)
        });
        if (!forwardResponse.ok) throw new Error(`Forward error! status: ${forwardResponse.status}`);
        const classifiedNews = await forwardResponse.json();

        // Step 2: Backward analysis
        const backwardResponse = await fetch(ENDPOINTS.backward.fullAnalysis, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify([classifiedNews])
        });
        if (!backwardResponse.ok) throw new Error(`Backward error! status: ${backwardResponse.status}`);
        const backwardResult = await backwardResponse.json();

        let html = '<h3>🔍 Backward Chaining - Manual Test</h3>';
        html += '<p><strong>Prvo je izvršena forward klasifikacija, a zatim backward analiza razloga.</strong></p>';



            // Prikaži samo klasifikovanu vijest (koristi ažuriranu iz backward odgovora)
            html += '<h4 style="color:#2980b9;">Vaša vijest:</h4>';
            html += createNewsCard(backwardResult.inputNews, getConfidenceClass(backwardResult.inputNews.confidence));

            // Prikaži originalnu vijest iz lanca (ako postoji u response-u)
            if (backwardResult.originalTrustedNews) {
                html += `<div class="news-card trusted" style="background:#eafaf1;border:2px solid #27ae60;margin-top:8px;">
                    <div class="explanation" style="font-weight:bold;color:#27ae60;">🔗 Originalna vijest iz trusted lanca:</div>
                    ${createNewsCard(backwardResult.originalTrustedNews, 'trusted')}
                </div>`;
            }

        resultsDiv.innerHTML = html;
    } catch (error) {
        console.error('Manual Backward Test Error:', error);
        resultsDiv.innerHTML = `<div class="error">❌ Greška: ${error.message}</div>`;
    } finally {
        hideLoading();
    }
}


// Helper Functions
function getManualFormData() {
    const title = document.getElementById('news-title').value.trim();
    const content = document.getElementById('news-content').value.trim();
    const sourceName = document.getElementById('source-name').value.trim();
    const sourceReputation = document.getElementById('source-reputation').value;
    const distributionType = document.getElementById('distribution-type').value;
    
    if (!title || !sourceName) {
        alert('Molim unesite naslov i ime izvora!');
        return null;
    }
    
    return {
        id: Date.now(),
        title: title,
        content: content || null,
        source: {
            id: Date.now(),
            name: sourceName,
            reputation: sourceReputation
        },
        distributionType: distributionType
    };
}

function createNewsCard(news, cssClass) {
    const confidenceLabel = getConfidenceLabel(news.confidence);
    const distributionLabel = getDistributionLabel(news.distributionType);
    
    return `
        <div class="news-card ${cssClass}">
            <h4>${news.title}</h4>
            <div class="source">📰 Izvor: ${news.source?.name || 'N/A'} (${news.source?.reputation || 'N/A'})</div>
            <div class="source">📊 Distribucija: ${distributionLabel}</div>
            <div class="confidence ${cssClass}">${confidenceLabel}</div>
            ${news.explanation ? `<div class="explanation">💡 ${news.explanation}</div>` : ''}
        </div>
    `;
}

function getConfidenceClass(confidence) {
    switch (confidence) {
        case 'POUZDANA': return 'trusted';
        case 'SUMNJIVA': return 'suspicious';
        case 'POTENCIJALNO_LAZNA': return 'potentially-fake';
        default: return 'unclassified';
    }
}

function getConfidenceLabel(confidence) {
    switch (confidence) {
        case 'POUZDANA': return 'Pouzdana';
        case 'SUMNJIVA': return 'Sumnjiva';
        case 'POTENCIJALNO_LAZNA': return 'Potencijalno lažna';
        default: return confidence || 'Neklasifikovano';
    }
}

function getDistributionLabel(distribution) {
    switch (distribution) {
        case 'NORMAL': return 'Normalno';
        case 'COORDINATED': return 'Koordinisano';
        case 'EXPLOSIVE': return 'Eksplozivno';
        default: return distribution || 'N/A';
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('SBNZ News Analysis System loaded');
    
    // Set empty state for results divs
    document.querySelectorAll('.results').forEach(div => {
        if (!div.innerHTML.trim()) {
            div.classList.add('empty');
        }
    });
});