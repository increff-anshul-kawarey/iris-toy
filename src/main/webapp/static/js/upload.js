function getUploadList() {
    // Fetch actual data status from backend
    fetchDataStatus();
}

function fetchDataStatus() {
    console.log("🔄 Fetching data status from:", getUploadUrl() + '/status');
    $.ajax({
        url: getUploadUrl() + '/status',
        type: 'GET',
        success: function(data) {
            console.log("📊 Received status data:", data);
            window.currentStatusData = data;
            displayUploadList(data);
        },
        error: function(err) {
            console.error("❌ Error fetching data status:", err);
            // Fallback to mock data if API fails
            var mockData = {
                "styles": {"exists": false, "count": 0},
                "stores": {"exists": false, "count": 0},
                "skus": {"exists": false, "count": 0},
                "sales": {"exists": false, "count": 0}
            };
            console.log("🔄 Using fallback mock data:", mockData);
            window.currentStatusData = mockData;
            displayUploadList(mockData);
        }
    });
}
function displayUploadList(data) {
    console.log("🎨 displayUploadList called with data:", data);
    // Keep the legacy table for backward compatibility (but hidden)
    $('#upload-table').append(uploadTableHead)
    var $tbody = $('#upload-table').find('tbody');
    $tbody.empty();

    // Clear and populate the new card layout
    var $cardsRow = $('#upload-cards-row');
    $cardsRow.empty();
    console.log("🧹 Cleared cards container");

    for (var i in uploadJson.uploadFiles) {
		var dataItem = uploadJson.uploadFiles[i];
		console.log("🃏 Creating card for:", dataItem.id, "with status:", data[dataItem.id]);
		// Add to legacy table
		row=createTableRow(data[dataItem.id],dataItem)
		$tbody.append(row);

		// Create and add card
		var card = createUploadCard(data[dataItem.id], dataItem);
		$cardsRow.append(card);
		console.log("✅ Card added for:", dataItem.id);
	}
	console.log("🎨 UI update complete");
}

function createUploadCard(statusData, uploadFile) {
    var statusClass = '';
    var statusTitle = '';
    var statusIcon = '';
    var primaryMessage = '';
    var secondaryMessage = '';
    var cardClass = 'upload-card-modern';
    var dependencyInfo = '';
    var uploadEnabled = true;

    // Log for debugging
    console.log('🃏 Creating card for', uploadFile.id, 'with status:', statusData);
    console.log('🃏 Processing status:', statusData ? statusData.processing : 'undefined');

    // Check dependencies and determine if upload should be enabled
    var dependencyCheck = checkUploadDependencies(uploadFile.id, window.currentStatusData);
    uploadEnabled = dependencyCheck.enabled;
    dependencyInfo = dependencyCheck.message;

    // Determine status based on data existence and count
    // Processing takes priority over exists status
    var showCancelButton = false;
    if (statusData && statusData.processing) {
        console.log('🎬 Setting processing animation for', uploadFile.id);
        statusClass = 'status-processing';
        statusTitle = 'Processing in progress';
        statusIcon = '<div class="processing-dots"><div class="dot"></div><div class="dot"></div><div class="dot"></div></div>';
        
        // Enhanced progress display
        if (statusData.progressPercentage && statusData.progressPercentage > 0) {
            primaryMessage = 'Processing: ' + Math.round(statusData.progressPercentage) + '% complete';
            secondaryMessage = statusData.progressMessage || 'Processing your file...';
        } else {
            primaryMessage = 'Your file is being processed';
            secondaryMessage = statusData.progressMessage || 'Estimated time remaining: ~2 minutes';
        }
        
        cardClass += ' card-processing';
        showCancelButton = true; // Show cancel button during processing
        console.log('CANCEL BUTTON SHOULD SHOW for', uploadFile.id, 'Progress:', statusData.progressPercentage);
    } else if (statusData && statusData.failed) {
        statusClass = 'status-error';
        statusTitle = 'Processing failed';
        statusIcon = '<i class="fa fa-exclamation-triangle status-icon-large"></i>';
        
        // Enhanced error display with validation statistics
        if (statusData.errorSummary && statusData.errorSummary.totalErrors) {
            var validationErrors = statusData.errorSummary.validationErrors || 0;
            var skippedRows = statusData.errorSummary.skippedRows || 0;
            
            if (validationErrors > 0 && skippedRows > 0) {
                primaryMessage = 'Upload failed: ' + validationErrors + ' validation errors, ' + skippedRows + ' rows skipped';
                secondaryMessage = 'Fix validation errors and upload missing dependencies';
            } else if (validationErrors > 0) {
                primaryMessage = 'Upload failed with ' + validationErrors + ' validation errors';
                secondaryMessage = 'Please fix the errors and try again';
            } else if (skippedRows > 0) {
                primaryMessage = 'Upload completed with ' + skippedRows + ' rows skipped';
                secondaryMessage = 'Upload missing dependencies to process skipped rows';
            } else {
                primaryMessage = 'Upload failed with ' + statusData.errorSummary.totalErrors + ' errors';
                secondaryMessage = 'Please check your file and try again';
            }
        } else {
            primaryMessage = 'Processing failed due to error';
            secondaryMessage = 'Please check your file and try again';
        }
        cardClass += ' card-error';
    } else if (statusData && statusData.exists) {
        statusClass = 'status-success';
        statusTitle = 'Processing completed';
        statusIcon = '<i class="fa fa-check-circle status-icon-large"></i>';
        primaryMessage = 'Your file is ready';
        secondaryMessage = statusData.count + ' records available';
        cardClass += ' card-success';
    } else {
        if (!uploadEnabled) {
            statusClass = 'status-blocked';
            statusTitle = 'Dependencies required';
            statusIcon = '<i class="fa fa-lock status-icon-large"></i>';
            primaryMessage = 'Upload blocked';
            secondaryMessage = dependencyInfo;
            cardClass += ' card-blocked';
        } else {
            statusClass = 'status-pending';
            statusTitle = 'Ready for upload';
            statusIcon = '<i class="fa fa-upload status-icon-large"></i>';
            primaryMessage = 'Upload your data file';
            secondaryMessage = 'Click upload to get started';
            cardClass += ' card-pending';
        }
    }

    var cardHtml = '<div class="col-md-6 mb-4">' +
        '<div class="card ' + cardClass + '" data-file-type="' + uploadFile.id + '">' +
            // Header area
            '<div class="card-header-modern ' + statusClass + '">' +
                '<span class="status-title">' + statusTitle + '</span>' +
            '</div>' +
            // Main content area
            '<div class="card-body-modern">' +
                '<div class="file-type-label">' + uploadFile["Display Name"] + '</div>' +
                '<div class="status-indicator-modern">' +
                    statusIcon +
                '</div>' +
                '<div class="primary-message">' + primaryMessage + '</div>' +
                '<div class="secondary-message">' + secondaryMessage + '</div>' +
            '</div>' +
            // Action area
            '<div class="card-footer-modern">' +
                '<div class="action-buttons">' +
                    (showCancelButton ? 
                        '<button type="button" class="btn btn-danger" onclick="cancelTask(window.currentUploadTaskId)">' +
                            '<i class="fa fa-times"></i> Cancel Upload' +
                        '</button>' 
                        : 
                        '<button type="button" class="btn btn-primary-modern" onclick="uploadModal(\''+uploadFile.id+'\',\''+uploadFile["Display Name"]+'\')"' + 
                            (!uploadEnabled ? ' disabled title="' + dependencyInfo.replace(/"/g, '&quot;') + '"' : '') + '>' +
                            (statusData && statusData.failed ? 'Retry Upload' : 'Upload File') +
                        '</button>'
                    ) +
                    '<button type="button" class="btn btn-secondary-modern" onclick="downloadDataFile(\''+uploadFile.id+'\')" ' + (statusData && statusData.exists ? '' : 'disabled') + '>' +
                        'Download Data' +
                    '</button>' +
                    // Error file download buttons (show when there are errors or skipped rows)
                    (statusData && (statusData.failed || statusData.skippedCount > 0) && statusData.errorFiles ? 
                        '<div class="error-download-section mt-2">' +
                            '<div class="error-download-label">Download Files:</div>' +
                            '<div class="btn-group-sm">' +
                                // Validation errors file
                                (statusData.errorFiles.validationErrors ? 
                                    '<button type="button" class="btn btn-danger btn-sm me-1" onclick="downloadErrorFile(\'' + statusData.errorFiles.validationErrors + '\')" title="Download validation error rows for correction">' +
                                        '<i class="fa fa-exclamation-triangle"></i> Validation Errors' +
                                    '</button>' : '') +
                                // Skipped rows file
                                (statusData.errorFiles.skippedRows ? 
                                    '<button type="button" class="btn btn-warning btn-sm me-1" onclick="downloadErrorFile(\'' + statusData.errorFiles.skippedRows + '\')" title="Download skipped rows (missing dependencies)">' +
                                        '<i class="fa fa-skip-forward"></i> Skipped Rows' +
                                    '</button>' : '') +
                                // Complete file with error reasons
                                (statusData.errorFiles.allFailedRowsWithErrors ? 
                                    '<button type="button" class="btn btn-info btn-sm me-1" onclick="downloadErrorFile(\'' + statusData.errorFiles.allFailedRowsWithErrors + '\')" title="Download all failed rows with error details">' +
                                        '<i class="fa fa-list"></i> All with Errors' +
                                    '</button>' : '') +
                                // Error summary
                                (statusData.errorFiles.errorSummary ? 
                                    '<button type="button" class="btn btn-secondary btn-sm" onclick="downloadErrorFile(\'' + statusData.errorFiles.errorSummary + '\')" title="Download error summary report">' +
                                        '<i class="fa fa-chart-bar"></i> Summary' +
                                    '</button>' : '') +
                            '</div>' +
                        '</div>' : ''
                    ) +
                '</div>' +
            '</div>' +
        '</div>' +
    '</div>';

    return cardHtml;
}

function getFileIcon(fileId) {
    var icons = {
        'styles': 'fa-tags',
        'stores': 'fa-building',
        'skus': 'fa-cubes',
        'sales': 'fa-chart-line'
    };
    return icons[fileId] || 'fa-file';
}

function createTableRow(type,uploadFile)
{
        downloadError=

        row = ''
        if(type=="success")
		{
		row ='<tr>'
            +'<td scope="col" colspan="1"><i class="fa fa-check fa-fw mr-3"></i></td>'
		}
		else if(type=="failed")
		{
		row ='<tr>'
            +'<td scope="col" colspan="1"><i class="fa fa-times fa-fw mr-3" onclick="downloadErrorFile(\''+uploadFile.id+'\')"></i></td>'

		}
		else if(type=="processing")
		{
		row ='<tr>'
            +'<td scope="col" colspan="1"><i class="fa fa-spinner fa-fw mr-3"></i></td>'
		}
		else
		{
		row ='<tr>'
            +'<td scope="col" colspan="1"></td>'
		}
		row +='<td scope="col" colspan="5">'+uploadFile["Display Name"]+'</td>'
            +'<td scope="col" colspan="3"></td>'
            +'<td scope="col" colspan="3"><i class="fa fa-arrow-up fa-fw mr-3" onclick="uploadModal(\''+uploadFile.id+'\',\''+uploadFile["Display Name"]+'\')"></i><i class="fa fa-arrow-down fa-fw mr-3" onclick="downloadInputFile(\''+uploadFile.id+'\')"></i></td>'
            +'</tr>'
    return row;
}
function downloadDataFile(id)
{
	// Use async download endpoints
	url=getUploadUrl()+"/download/"+id+"/async"
	downloadAsync(url, id);
}

/**
 * Check upload dependencies and return status
 * @param {string} fileType - Type of file being uploaded (styles, stores, skus, sales)
 * @param {object} statusData - Current data status from backend
 * @returns {object} - {enabled: boolean, message: string}
 */
function checkUploadDependencies(fileType, statusData) {
    if (!statusData) {
        return {enabled: true, message: ''};
    }
    
    switch(fileType) {
        case 'styles':
        case 'stores':
            // No dependencies - can always upload
            return {enabled: true, message: ''};
            
        case 'skus':
            // SKUs require styles
            if (!statusData.styles || !statusData.styles.exists) {
                return {
                    enabled: false, 
                    message: 'Upload styles first (styles → skus → sales)'
                };
            }
            return {enabled: true, message: ''};
            
        case 'sales':
            // Sales require both SKUs and stores
            var missingDeps = [];
            if (!statusData.styles || !statusData.styles.exists) {
                missingDeps.push('styles');
            }
            if (!statusData.skus || !statusData.skus.exists) {
                missingDeps.push('skus');
            }
            if (!statusData.stores || !statusData.stores.exists) {
                missingDeps.push('stores');
            }
            
            if (missingDeps.length > 0) {
                return {
                    enabled: false,
                    message: 'Upload ' + missingDeps.join(', ') + ' first'
                };
            }
            return {enabled: true, message: ''};
            
        default:
            return {enabled: true, message: ''};
    }
}

/**
 * Download error file from server
 * @param {string} filePath - Absolute path to the error file
 */
function downloadErrorFile(filePath) {
    if (!filePath) {
        console.error('No file path provided for error file download');
        return;
    }
    
    // Create download URL
    var downloadUrl = getRunUrl() + '/download/error-file?filePath=' + encodeURIComponent(filePath);
    
    // Create temporary link and trigger download
    var link = document.createElement('a');
    link.href = downloadUrl;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    console.log('Downloading error file:', filePath);
}

$(document).ready(getUploadList);