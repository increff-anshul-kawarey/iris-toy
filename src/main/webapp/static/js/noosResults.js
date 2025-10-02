/**
 * NOOS Results Dashboard JavaScript
 * 
 * Handles displaying NOOS algorithm results in card format
 * Similar to upload cards but for algorithm results
 */

var noosData = {};

/**
 * Load NOOS results data from API
 */
function loadNoosResults() {
    console.log('Loading NOOS results...');
    
    // Show loading state
    $('#refresh-noos-btn').html('<i class="fa fa-spinner fa-spin"></i> Loading...');
    $('#refresh-noos-btn').prop('disabled', true);
    
    $.ajax({
        url: getRunUrl() + '/results/noos/dashboard',
        type: 'GET',
        success: function(data) {
            console.log('NOOS data loaded:', data);
            noosData = data;
            displayNoosResults(data);
            
            // Reset button
            $('#refresh-noos-btn').html('<i class="fa fa-refresh"></i> Refresh');
            $('#refresh-noos-btn').prop('disabled', false);
            
            // Enable download if results exist
            if (data.hasResults) {
                $('#download-noos-btn').prop('disabled', false);
            }
        },
        error: function(err) {
            console.error('Error loading NOOS results:', err);
            displayNoResultsMessage();
            
            // Reset button
            $('#refresh-noos-btn').html('<i class="fa fa-refresh"></i> Refresh');
            $('#refresh-noos-btn').prop('disabled', false);
        }
    });
}

/**
 * Display NOOS results in card format
 */
function displayNoosResults(data) {
    var $container = $('#noos-cards-container');
    $container.empty();
    
    if (!data.hasResults) {
        displayNoResultsMessage();
        return;
    }
    
    var summary = data.summary || {};
    var percentages = data.percentages || {};
    var totalResults = data.totalResults || 0;
    
    // Create cards for each classification type
    var cardTypes = [
        {
            type: 'core',
            displayName: 'Core Styles',
            color: 'primary',
            icon: 'fa-star',
            description: 'Consistent performers with stable demand'
        },
        {
            type: 'bestseller',
            displayName: 'Bestseller Styles', 
            color: 'success',
            icon: 'fa-trophy',
            description: 'High-performing styles exceeding benchmarks'
        },
        {
            type: 'fashion',
            displayName: 'Fashion Styles',
            color: 'warning',
            icon: 'fa-tint',
            description: 'Trend-driven styles with variable performance'
        }
    ];
    
    // Add total summary card
    var totalCard = createNoosCard(        {
            type: 'total',
            displayName: 'Total Styles',
            color: 'info',
            icon: 'fa-database',
            description: 'All classified styles from latest run',
            count: totalResults,
            percentage: 100,
            lastRunDate: data.lastRunDate
        });
    $container.append(totalCard);
    
    // Add individual type cards
    cardTypes.forEach(function(cardType) {
        var count = summary[cardType.type] || 0;
        var percentage = percentages[cardType.type] || 0;
        
        var card = createNoosCard({
            type: cardType.type,
            displayName: cardType.displayName,
            color: cardType.color,
            icon: cardType.icon,
            description: cardType.description,
            count: count,
            percentage: percentage,
            lastRunDate: data.lastRunDate
        });
        $container.append(card);
    });
}

/**
 * Create a NOOS result card
 */
function createNoosCard(config) {
    var cardClass = 'noos-card-modern card-' + config.color;
    var lastRunText = config.lastRunDate ? 
        'Last run: ' + formatDate(new Date(config.lastRunDate)) : 
        'No recent runs';
    
    var cardHtml = '<div class="col-xl-3 col-lg-4 col-md-6 col-sm-12 mb-4">' +
        '<div class="card ' + cardClass + ' h-100">' +
            // Header
            '<div class="card-header-modern bg-' + config.color + ' text-white">' +
                '<span class="status-title">' + config.displayName + '</span>' +
            '</div>' +
            // Body
            '<div class="card-body-modern text-center">' +
                '<div class="noos-icon-container">' +
                    '<i class="fa ' + config.icon + ' noos-icon"></i>' +
                '</div>' +
                '<div class="noos-count">' + config.count.toLocaleString() + '</div>' +
                '<div class="noos-percentage">' + config.percentage + '%</div>' +
                '<div class="noos-description">' + config.description + '</div>' +
            '</div>' +
            // Footer
            '<div class="card-footer-modern">' +
                '<div class="noos-last-run">' + lastRunText + '</div>' +
                '<div class="action-buttons mt-2">' +
                    '<button type="button" class="btn btn-sm btn-outline-' + config.color + '" onclick="viewNoosDetails(\'' + config.type + '\')">' +
                        'View Details' +
                    '</button>' +
                '</div>' +
            '</div>' +
        '</div>' +
    '</div>';
    
    return cardHtml;
}

/**
 * Display message when no results are available
 */
function displayNoResultsMessage() {
    var $container = $('#noos-cards-container');
    $container.empty();
    
    var messageHtml = '<div class="col-12">' +
        '<div class="card border-secondary">' +
            '<div class="card-body text-center py-5">' +
                '<i class="fa fa-chart-bar fa-3x text-muted mb-3"></i>' +
                '<h5 class="text-muted">No NOOS Results Available</h5>' +
                '<p class="text-muted">Run the NOOS algorithm to see classification results here.</p>' +
                '<button type="button" class="btn btn-primary" onclick="runNoosAlgorithm()">' +
                    '<i class="fa fa-play"></i> Run NOOS Algorithm' +
                '</button>' +
            '</div>' +
        '</div>' +
    '</div>';
    
    $container.append(messageHtml);
    $('#download-noos-btn').prop('disabled', true);
}

/**
 * View details for a specific NOOS type
 */
function viewNoosDetails(type) {
    console.log('Viewing details for type:', type);
    
    if (type === 'total') {
        // Show overall summary
        showNoosModal('NOOS Algorithm Summary', createSummaryContent());
    } else {
        // Show specific type details
        showNoosModal(type.charAt(0).toUpperCase() + type.slice(1) + ' Styles', 
                     'Loading detailed results for ' + type + ' styles...');
        
        // Load detailed data
        loadNoosTypeDetails(type);
    }
}

/**
 * Create summary content for modal
 */
function createSummaryContent() {
    if (!noosData.hasResults) {
        return '<p>No results available.</p>';
    }
    
    var summary = noosData.summary || {};
    var percentages = noosData.percentages || {};
    var totalResults = noosData.totalResults || 0;
    
    var content = '<div class="noos-summary">' +
        '<div class="row">' +
            '<div class="col-md-6">' +
                '<h6>Classification Breakdown:</h6>' +
                '<ul class="list-unstyled">' +
                    '<li><strong>Core:</strong> ' + (summary.core || 0) + ' styles (' + (percentages.core || 0) + '%)</li>' +
                    '<li><strong>Bestseller:</strong> ' + (summary.bestseller || 0) + ' styles (' + (percentages.bestseller || 0) + '%)</li>' +
                    '<li><strong>Fashion:</strong> ' + (summary.fashion || 0) + ' styles (' + (percentages.fashion || 0) + '%)</li>' +
                '</ul>' +
            '</div>' +
            '<div class="col-md-6">' +
                '<h6>Summary:</h6>' +
                '<ul class="list-unstyled">' +
                    '<li><strong>Total Styles:</strong> ' + totalResults.toLocaleString() + '</li>' +
                    '<li><strong>Last Run:</strong> ' + formatDate(new Date(noosData.lastRunDate)) + '</li>' +
                '</ul>' +
            '</div>' +
        '</div>' +
    '</div>';
    
    return content;
}

/**
 * Load detailed data for a specific NOOS type
 */
function loadNoosTypeDetails(type) {
    $.ajax({
        url: getRunUrl() + '/results/noos/' + type,
        type: 'GET',
        success: function(results) {
            var content = createTypeDetailsContent(type, results);
            updateNoosModalContent(content);
        },
        error: function(err) {
            console.error('Error loading type details:', err);
            updateNoosModalContent('<p class="text-danger">Error loading details. Please try again.</p>');
        }
    });
}

/**
 * Create detailed content for a specific type
 */
function createTypeDetailsContent(type, results) {
    if (!results || results.length === 0) {
        return '<p>No ' + type + ' styles found.</p>';
    }
    
    var content = '<div class="noos-type-details">' +
        '<p><strong>' + results.length + '</strong> ' + type + ' styles found:</p>' +
        '<div class="table-responsive" style="max-height: 400px; overflow-y: auto;">' +
            '<table class="table table-sm table-striped">' +
                '<thead class="thead-light">' +
                    '<tr>' +
                        '<th>Style Code</th>' +
                        '<th>Category</th>' +
                        '<th>ROS</th>' +
                        '<th>Revenue Contribution</th>' +
                    '</tr>' +
                '</thead>' +
                '<tbody>';
    
    results.slice(0, 50).forEach(function(result) { // Show first 50 results
        content += '<tr>' +
            '<td>' + (result.styleCode || '-') + '</td>' +
            '<td>' + (result.category || '-') + '</td>' +
            '<td>' + (result.styleROS ? result.styleROS.toFixed(2) : '-') + '</td>' +
            '<td>' + (result.styleRevContribution ? result.styleRevContribution.toFixed(2) + '%' : '-') + '</td>' +
        '</tr>';
    });
    
    content += '</tbody></table></div>';
    
    if (results.length > 50) {
        content += '<p class="text-muted"><small>Showing first 50 results. Download TSV for complete data.</small></p>';
    }
    
    content += '</div>';
    return content;
}

/**
 * Show NOOS modal with content using existing message modal
 */
function showNoosModal(title, content) {
    // Use existing message modal from mysnippets.html
    var modal = $('#message-modal');
    if (modal.length > 0) {
        var titleContent = '<h5><i class="fa fa-chart-bar"></i> ' + title + '</h5>';
        var fullContent = titleContent + '<hr>' + content;
        $('#message-modal-body').html(fullContent);
        modal.modal('show');
    } else {
        console.error('Message modal not found!');
        // Fallback: show alert
        alert(title + '\\n\\n' + $(content).text() || content);
    }
}

/**
 * Update modal content
 */
function updateNoosModalContent(content) {
    var titleContent = '<h5><i class="fa fa-chart-bar"></i> NOOS Details</h5>';
    var fullContent = titleContent + '<hr>' + content;
    $('#message-modal-body').html(fullContent);
}

/**
 * Download NOOS results as TSV (Async)
 */
function downloadNoosResults() {
    console.log('Downloading NOOS results...');
    // Use async download endpoint
    var url = getUploadUrl() + '/download/noos/async';
    downloadAsync(url, 'noos');
}

/**
 * Run NOOS algorithm (redirect to algorithm section)
 */
function runNoosAlgorithm() {
    // You can either trigger the algorithm modal or redirect
    // For now, let's trigger the existing run algo functionality
    if (typeof runAlgo === 'function') {
        runAlgo('noos');
    } else {
        alert('Please use the "Run Algo" button above to execute the NOOS algorithm.');
    }
}

/**
 * Format date for display
 */
function formatDate(date) {
    if (!date) return 'Unknown';
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

/**
 * Initialize NOOS results on page load
 */
$(document).ready(function() {
    // Load NOOS results when dashboard loads
    setTimeout(loadNoosResults, 1000); // Small delay to let other dashboard elements load first
});
