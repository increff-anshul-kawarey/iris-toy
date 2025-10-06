/**
 * Algorithm Parameters Management
 * Handles NOOS algorithm parameter configuration and updates
 */

// API endpoints
const API_BASE = getApiUrl();
const ALGO_PARAMS_API = `${API_BASE}/algo`;
const RUN_ALGORITHM_API = `${API_BASE}/run/noos/async`;

// DOM elements
let form, saveBtn, loadDefaultsBtn, loadCurrentBtn, runAlgorithmBtn;
let activateSetBtn;
let currentEditingSetName = null; // Tracks which set is loaded for editing
let parameterSetsContainer;
let parameterSetsSearch; // new: search input

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeElements();
    loadCurrentParameters();
    loadParameterSets();
    attachEventListeners();
});

/**
 * Initialize DOM elements
 */
function initializeElements() {
    form = document.getElementById('algorithmParamsForm');
    saveBtn = document.getElementById('saveParamsBtn');
    saveNewBtn = document.getElementById('saveNewParamsBtn');
    loadDefaultsBtn = document.getElementById('loadDefaultsBtn');
    loadCurrentBtn = document.getElementById('loadCurrentBtn');
    runAlgorithmBtn = document.getElementById('runAlgorithmBtn');
    activateSetBtn = document.getElementById('activateSetBtn');
    parameterSetsContainer = document.getElementById('parameterSetsContainer');
    parameterSetsSearch = document.getElementById('parameterSetsSearch');
}

/**
 * Attach event listeners
 */
function attachEventListeners() {
    saveBtn.addEventListener('click', saveParameters);
    loadDefaultsBtn.addEventListener('click', loadDefaultParameters);
    loadCurrentBtn.addEventListener('click', loadCurrentParameters);
    runAlgorithmBtn.addEventListener('click', runAlgorithmWithCurrentParams);
    saveNewBtn.addEventListener('click', saveAsNewConfig);
    activateSetBtn.addEventListener('click', activateCurrentEditingSet);
    
    // Form validation
    form.addEventListener('input', validateForm);

    // Search filter
    if (parameterSetsSearch) {
        parameterSetsSearch.addEventListener('input', () => {
            // Re-render from cached list when we add caching; for now, reload
            loadParameterSets();
        });
    }
}

/**
 * Load current parameters from the server
 */
async function loadCurrentParameters() {
    try {
        showLoading(loadCurrentBtn);
        
        const response = await fetch(`${ALGO_PARAMS_API}/current`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const params = await response.json();
        populateForm(params);
        setCurrentEditingSet(null);
        showSuccess('Current parameters loaded successfully');
        
    } catch (error) {
        console.error('Error loading current parameters:', error);
        showError('Failed to load current parameters: ' + error.message);
    } finally {
        hideLoading(loadCurrentBtn);
    }
}

/**
 * Load default parameters
 */
async function loadDefaultParameters() {
    try {
        showLoading(loadDefaultsBtn);
        
        const response = await fetch(`${ALGO_PARAMS_API}/defaults`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const params = await response.json();
        populateForm(params);
        setCurrentEditingSet('default');
        showSuccess('Default parameters loaded successfully');
        
    } catch (error) {
        console.error('Error loading default parameters:', error);
        showError('Failed to load default parameters: ' + error.message);
    } finally {
        hideLoading(loadDefaultsBtn);
    }
}

/**
 * Save parameters to the server
 */
async function saveParameters() {
    if (!validateForm()) {
        showError('Please fix validation errors before saving');
        return;
    }
    
    try {
        showLoading(saveBtn);
        
        const formData = getFormData();
        const url = currentEditingSetName
            ? `${ALGO_PARAMS_API}/set/${encodeURIComponent(currentEditingSetName)}`
            : `${ALGO_PARAMS_API}/update`;
        const method = currentEditingSetName ? 'PUT' : 'POST';
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }
        
        const result = await response.json();
        showSuccess('Parameters saved successfully!');
        loadParameterSets(); // Refresh the parameter sets list
        
    } catch (error) {
        console.error('Error saving parameters:', error);
        showError('Failed to save parameters: ' + error.message);
    } finally {
        hideLoading(saveBtn);
    }
}

/**
 * Save form parameters as a NEW parameter set (uses algorithmLabel as name by default)
 */
async function saveAsNewConfig() {
    if (!validateForm()) {
        showError('Please fix validation errors before saving');
        return;
    }

    try {
        showLoading(saveNewBtn);

        const formData = getFormData();
        let proposedName = (formData.algorithmLabel || '').trim();
        // Allow user to override name
        const inputName = prompt('Enter a name for this NOOS config', proposedName || 'noos_config');
        const finalName = (inputName || proposedName || ('run_' + Date.now())).trim();

        const url = `${ALGO_PARAMS_API}/create?name=${encodeURIComponent(finalName)}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        showSuccess(`Saved new config "${result.parameterSetName || finalName}" and activated it`);
        loadParameterSets();
    } catch (error) {
        console.error('Error saving as new config:', error);
        showError('Failed to save new config: ' + error.message);
    } finally {
        hideLoading(saveNewBtn);
    }
}

/**
 * Run algorithm with current form parameters
 */
async function runAlgorithmWithCurrentParams() {
    if (!validateForm()) {
        showError('Please fix validation errors before running algorithm');
        return;
    }
    
    try {
        showLoading(runAlgorithmBtn);
        
        const formData = getFormData();
        const response = await fetch(RUN_ALGORITHM_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }
        
        const task = await response.json();
        showSuccess(`Algorithm started successfully! Task ID: ${task.id}`);
        
        // Optionally redirect to dashboard to monitor progress
        setTimeout(() => {
            window.location.href = '/toy-iris/ui/dashboard';
        }, 2000);
        
    } catch (error) {
        console.error('Error running algorithm:', error);
        showError('Failed to start algorithm: ' + error.message);
    } finally {
        hideLoading(runAlgorithmBtn);
    }
}

/**
 * Load parameter sets history
 */
async function loadParameterSets() {
    try {
        // Prefer recent sets (active+inactive) for history view
        const response = await fetch(`${ALGO_PARAMS_API}/sets/recent?limit=10`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        let parameterSets = await response.json();
        // Apply simple client-side filter by name/label
        const q = (parameterSetsSearch && parameterSetsSearch.value || '').toLowerCase();
        if (q) {
            parameterSets = parameterSets.filter(p => {
                const name = (p.parameterSetName || '').toLowerCase();
                const label = (p.algorithmLabel || '').toLowerCase();
                return name.includes(q) || label.includes(q);
            });
        }
        displayParameterSets(parameterSets);
        
    } catch (error) {
        console.error('Error loading parameter sets:', error);
        parameterSetsContainer.innerHTML = `
            <div class="text-center text-danger">
                <i class="fa fa-exclamation-triangle"></i>
                Failed to load parameter sets
            </div>
        `;
    }
}

/**
 * Display parameter sets in the UI
 */
function displayParameterSets(parameterSets) {
    if (!parameterSets || parameterSets.length === 0) {
        parameterSetsContainer.innerHTML = `
            <div class="text-center text-muted">
                <i class="fa fa-info-circle"></i>
                No parameter sets found
            </div>
        `;
        return;
    }
    
    // Sort parameter sets: active first, then by last updated date (most recent first)
    parameterSets.sort((a, b) => {
        if (a.isActive && !b.isActive) return -1;
        if (!a.isActive && b.isActive) return 1;
        
        const dateA = a.lastUpdated ? new Date(a.lastUpdated) : new Date(0);
        const dateB = b.lastUpdated ? new Date(b.lastUpdated) : new Date(0);
        return dateB - dateA;
    });
    
    // Limit to last 5 parameter sets (most relevant ones)
    const recentSets = parameterSets.slice(0, 5);
    
    const html = recentSets.map((paramSet, index) => `
        <div class="parameter-set-card ${paramSet.isActive ? 'border-success' : ''}">
            <div class="row align-items-center">
                <div class="col-md-4">
                    <div class="parameter-set-name">
                        ${paramSet.parameterSetName || paramSet.algorithmLabel || 'Unnamed Set'}
                        ${paramSet.isActive ? '<span class="badge badge-success ml-2">Current</span>' : ''}
                    </div>
                    <small class="text-muted">
                        ${paramSet.lastUpdated ? 'Updated: ' + formatDateShort(new Date(paramSet.lastUpdated)) : 'No date'}
                    </small>
                </div>
                <div class="col-md-5">
                    <div class="parameter-set-details">
                        <div class="mb-1">
                            <strong>LT:</strong> ${paramSet.liquidationThreshold?.toFixed(2) || 'N/A'} | 
                            <strong>BM:</strong> ${paramSet.bestsellerMultiplier?.toFixed(1) || 'N/A'}
                        </div>
                        <div>
                            <strong>MV:</strong> ${paramSet.minVolumeThreshold?.toFixed(0) || 'N/A'} | 
                            <strong>CT:</strong> ${paramSet.consistencyThreshold?.toFixed(2) || 'N/A'}
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="parameter-set-actions">
                        <button class="btn-secondary-modern btn-sm" onclick="loadParameterSet('${(paramSet.parameterSetName || paramSet.algorithmLabel || '').replace(/'/g, "\\'")}')">
                            <i class="fa fa-download"></i> Load
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
    
    parameterSetsContainer.innerHTML = html;
}

/**
 * Load a specific parameter set
 */
async function loadParameterSet(parameterSetName) {
    try {
        const response = await fetch(`${ALGO_PARAMS_API}/set/${encodeURIComponent(parameterSetName)}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const params = await response.json();
        populateForm(params);
        setCurrentEditingSet(parameterSetName);
        showSuccess(`Parameter set "${parameterSetName}" loaded successfully`);
        
    } catch (error) {
        console.error('Error loading parameter set:', error);
        showError('Failed to load parameter set: ' + error.message);
    }
}

/**
 * Populate form with parameter data
 */
function populateForm(params) {
    document.getElementById('liquidationThreshold').value = params.liquidationThreshold || '';
    document.getElementById('bestsellerMultiplier').value = params.bestsellerMultiplier || '';
    document.getElementById('minVolumeThreshold').value = params.minVolumeThreshold || '';
    document.getElementById('consistencyThreshold').value = params.consistencyThreshold || '';
    document.getElementById('algorithmLabel').value = params.algorithmLabel || '';
    
    // Handle dates
    if (params.analysisStartDate) {
        document.getElementById('analysisStartDate').value = formatDateForInput(params.analysisStartDate);
    }
    if (params.analysisEndDate) {
        document.getElementById('analysisEndDate').value = formatDateForInput(params.analysisEndDate);
    }
    
    document.getElementById('coreDurationMonths').value = params.coreDurationMonths || '';
    document.getElementById('bestsellerDurationDays').value = params.bestsellerDurationDays || '';
}

/**
 * Update UI to reflect which set is being edited
 */
function setCurrentEditingSet(parameterSetName) {
    currentEditingSetName = parameterSetName; // null means current active set
    const labelEl = document.getElementById('currentParameterSetName');
    if (labelEl) {
        labelEl.textContent = parameterSetName === null ? 'Current' : parameterSetName;
    }
}

/**
 * Activate the currently editing named set
 */
async function activateCurrentEditingSet() {
    if (!currentEditingSetName) {
        showError('Load a specific set to activate it.');
        return;
    }
    try {
        showLoading(activateSetBtn);
        const response = await fetch(`${ALGO_PARAMS_API}/set/${encodeURIComponent(currentEditingSetName)}/activate`, {
            method: 'POST'
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }
        const result = await response.json();
        showSuccess(`Activated parameter set "${result.parameterSetName || currentEditingSetName}"`);
        setCurrentEditingSet(null);
        loadCurrentParameters();
        loadParameterSets();
    } catch (error) {
        console.error('Error activating set:', error);
        showError('Failed to activate set: ' + error.message);
    } finally {
        hideLoading(activateSetBtn);
    }
}

/**
 * Get form data as object
 */
function getFormData() {
    return {
        liquidationThreshold: parseFloat(document.getElementById('liquidationThreshold').value),
        bestsellerMultiplier: parseFloat(document.getElementById('bestsellerMultiplier').value),
        minVolumeThreshold: parseFloat(document.getElementById('minVolumeThreshold').value),
        consistencyThreshold: parseFloat(document.getElementById('consistencyThreshold').value),
        algorithmLabel: document.getElementById('algorithmLabel').value,
        analysisStartDate: document.getElementById('analysisStartDate').value,
        analysisEndDate: document.getElementById('analysisEndDate').value,
        coreDurationMonths: parseInt(document.getElementById('coreDurationMonths').value),
        bestsellerDurationDays: parseInt(document.getElementById('bestsellerDurationDays').value)
    };
}

/**
 * Validate form inputs
 */
function validateForm() {
    let isValid = true;
    
    // Clear previous validation states
    document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    
    // Validate liquidation threshold (0-1)
    const liquidationThreshold = document.getElementById('liquidationThreshold');
    if (!liquidationThreshold.value || liquidationThreshold.value < 0 || liquidationThreshold.value > 1) {
        liquidationThreshold.classList.add('is-invalid');
        isValid = false;
    }
    
    // Validate bestseller multiplier (1-5)
    const bestsellerMultiplier = document.getElementById('bestsellerMultiplier');
    if (!bestsellerMultiplier.value || bestsellerMultiplier.value < 1 || bestsellerMultiplier.value > 5) {
        bestsellerMultiplier.classList.add('is-invalid');
        isValid = false;
    }
    
    // Validate min volume threshold (1-1000)
    const minVolumeThreshold = document.getElementById('minVolumeThreshold');
    if (!minVolumeThreshold.value || minVolumeThreshold.value < 1 || minVolumeThreshold.value > 1000) {
        minVolumeThreshold.classList.add('is-invalid');
        isValid = false;
    }
    
    // Validate consistency threshold (0-1)
    const consistencyThreshold = document.getElementById('consistencyThreshold');
    if (!consistencyThreshold.value || consistencyThreshold.value < 0 || consistencyThreshold.value > 1) {
        consistencyThreshold.classList.add('is-invalid');
        isValid = false;
    }
    
    // Validate algorithm label
    const algorithmLabel = document.getElementById('algorithmLabel');
    if (!algorithmLabel.value.trim()) {
        algorithmLabel.classList.add('is-invalid');
        isValid = false;
    }
    
    // Validate dates
    const startDate = document.getElementById('analysisStartDate');
    const endDate = document.getElementById('analysisEndDate');
    if (!startDate.value || !endDate.value) {
        if (!startDate.value) startDate.classList.add('is-invalid');
        if (!endDate.value) endDate.classList.add('is-invalid');
        isValid = false;
    } else if (new Date(startDate.value) >= new Date(endDate.value)) {
        startDate.classList.add('is-invalid');
        endDate.classList.add('is-invalid');
        isValid = false;
    }
    
    // Validate durations
    const coreDuration = document.getElementById('coreDurationMonths');
    if (!coreDuration.value || coreDuration.value < 1 || coreDuration.value > 24) {
        coreDuration.classList.add('is-invalid');
        isValid = false;
    }
    
    const bestsellerDuration = document.getElementById('bestsellerDurationDays');
    if (!bestsellerDuration.value || bestsellerDuration.value < 1 || bestsellerDuration.value > 365) {
        bestsellerDuration.classList.add('is-invalid');
        isValid = false;
    }
    
    return isValid;
}

/**
 * Format date for HTML input
 */
function formatDateForInput(dateValue) {
    if (!dateValue) return '';
    
    let date;
    if (typeof dateValue === 'string') {
        date = new Date(dateValue);
    } else if (typeof dateValue === 'number') {
        date = new Date(dateValue);
    } else {
        date = dateValue;
    }
    
    if (isNaN(date.getTime())) return '';
    
    return date.toISOString().split('T')[0];
}

/**
 * Show loading state on button
 */
function showLoading(button) {
    button.disabled = true;
    const originalText = button.innerHTML;
    button.setAttribute('data-original-text', originalText);
    button.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Loading...';
}

/**
 * Hide loading state on button
 */
function hideLoading(button) {
    button.disabled = false;
    const originalText = button.getAttribute('data-original-text');
    if (originalText) {
        button.innerHTML = originalText;
    }
}

/**
 * Show success message
 */
function showSuccess(message) {
    console.log('✅ Success:', message);
    if (typeof $ !== 'undefined' && $.toast) {
        $.toast({
            heading: 'Success',
            text: message,
            icon: 'success',
            loader: true,
            loaderBg: '#28a745',
            showHideTransition: 'fade',
            hideAfter: 4000,
            position: 'top-right'
        });
    } else {
        alert('Success: ' + message);
    }
}

/**
 * Show error message
 */
function showError(message) {
    console.error('❌ Error:', message);
    if (typeof $ !== 'undefined' && $.toast) {
        $.toast({
            heading: 'Error',
            text: message,
            icon: 'error',
            loader: true,
            loaderBg: '#dc3545',
            showHideTransition: 'fade',
            hideAfter: 5000,
            position: 'top-right'
        });
    } else {
        alert('Error: ' + message);
    }
}

/**
 * Format date for short display
 */
function formatDateShort(date) {
    if (!date || isNaN(date.getTime())) return 'Unknown';
    
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.ceil(diffDays/7)} weeks ago`;
    
    return date.toLocaleDateString();
}

/**
 * Get API base URL (reuse from app.js)
 */
function getApiUrl() {
    const baseUrl = window.location.protocol + "//" + window.location.host;
    const contextPath = window.location.pathname.split('/')[1]; // Get 'toy-iris' from '/toy-iris/ui/algorithm_params'
    return baseUrl + "/" + contextPath + "/api";
}
