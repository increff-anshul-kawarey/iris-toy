var baseUrl = $("meta[name=baseUrl]").attr("content")
baseUrl="http://localhost:9000/toy-iris"
var allData ={}
function getUploadUrl() {
	return baseUrl + "/api/file";
}
function getReportUrl() {
	return baseUrl + "/api/report";
}
function getAlgoUrl(){
return baseUrl + "/api/algo";
}
function getRunUrl(){
return baseUrl + "/api";
}
function ajaxRestApiCallWithData(url, type, successfunction, json) {
	$.ajax({
		url: url,
		type: type,
		data: json,
		headers: {
			'Content-Type': 'application/json'
		},
		success: function (data) {
			successfunction(data);
		},
		error: function (errormessage) {
			// Enhanced error handling for upload responses
            if (errormessage.responseJSON && errormessage.responseJSON.message) {
                messageAlertFail(errormessage.responseJSON.message);
            } else if (errormessage.responseJSON && errormessage.responseJSON.errors) {
                messageAlertFail("Upload failed: " + errormessage.responseJSON.errors.join(", "));
            } else if (errormessage.responseText) {
                messageAlertFail("Upload failed: " + errormessage.responseText);
            } else {
                messageAlertFail("Upload failed: " + errormessage.statusText);
            }
		}
	});
}
function ajaxRestApiCallWithoutData(url, type, successfunction) {
	$.ajax({
		url: url,
		type: type,
		success: function (data) {
			successfunction(data);
		},
		error: function (errormessage) {
			// Enhanced error handling for upload responses
            if (errormessage.responseJSON && errormessage.responseJSON.message) {
                messageAlertFail(errormessage.responseJSON.message);
            } else if (errormessage.responseJSON && errormessage.responseJSON.errors) {
                messageAlertFail("Upload failed: " + errormessage.responseJSON.errors.join(", "));
            } else if (errormessage.responseText) {
                messageAlertFail("Upload failed: " + errormessage.responseText);
            } else {
                messageAlertFail("Upload failed: " + errormessage.statusText);
            }
		}
	});
}
function uploadAjax(url,formData)
{
    $('#upload-modal').modal('toggle');

    // Extract file type from URL for status updates
    var fileType = url.split('/').pop();

    // Set processing status
    setUploadStatus(fileType, {processing: true});

    $.ajax({
        url: url,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            // Clear processing status and refresh data
            fetchDataStatus();
            if (typeof data === "string") {
                messageAlertPass("Upload completed: " + data);
            } else if (data.success === false) {
                messageAlertFail("Upload failed: " + data.message + " (" + data.errorCount + " errors)");
                // Show additional error details if available
                if (data.errors && data.errors.length > 0) {
                    console.log("Upload errors:", data.errors);
                }
            } else {
                // Extract UPSERT statistics from messages
                var uploadSummary = extractUploadSummary(data.messages, fileType);
                messageAlertPass(uploadSummary);

                // Show detailed messages if available
                if (data.messages && data.messages.length > 0) {
                    var detailMessages = data.messages.filter(function(msg) {
                        return msg.includes("Processing") || msg.includes("UPSERT");
                    });
                    if (detailMessages.length > 0) {
                        setTimeout(function() {
                            messageAlertPass("Details: " + detailMessages.join(". "));
                        }, 1000);
                    }
                }
            }
        },
        error: function (errormessage) {
            console.log("Upload error:", errormessage);
            // Clear processing status and set failed status
            setUploadStatus(fileType, {failed: true});
            if (errormessage.responseJSON && errormessage.responseJSON.message) {
                messageAlertFail(errormessage.responseJSON.message);
            } else if (errormessage.responseJSON && errormessage.responseJSON.errors) {
                messageAlertFail("Upload failed: " + errormessage.responseJSON.errors.join(", "));
            } else if (errormessage.responseText) {
                messageAlertFail("Upload failed: " + errormessage.responseText);
            } else {
                messageAlertFail("Upload failed: " + errormessage.statusText);
            }
        }
    });
}

function uploadAjaxAsync(url,formData)
{
    $('#upload-modal').modal('toggle');

    // Extract file type from URL for status updates
    var fileType = url.split('/').pop().replace('/async', '');
    console.log("🚀 Starting async upload for:", fileType, "URL:", url);
    
    // Ensure we have a valid file type
    if (!fileType || fileType === '') {
        console.error("❌ Could not extract file type from URL:", url);
        messageAlertFail("Invalid upload URL");
        return;
    }
    
    // Ensure file type is one of the expected values
    var validFileTypes = ['styles', 'stores', 'skus', 'sales'];
    if (!validFileTypes.includes(fileType)) {
        console.warn("⚠️ Unexpected file type:", fileType, "URL:", url);
        // Try to extract from the full URL path
        var urlParts = url.split('/');
        for (var i = 0; i < urlParts.length; i++) {
            if (validFileTypes.includes(urlParts[i])) {
                fileType = urlParts[i];
                console.log("🔧 Corrected file type to:", fileType);
                break;
            }
        }
    }

    // Set processing status immediately and force UI update
    console.log("🔄 Setting initial processing status for:", fileType);
    setUploadStatus(fileType, {processing: true, failed: false});

    $.ajax({
        url: url,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (task) {
            console.log("Async upload started:", task);
            
            if (task.status === "FAILED") {
                setUploadStatus(fileType, {processing: false, failed: true});
                messageAlertFail("Upload failed: " + (task.errorMessage || "Unknown error"));
                return;
            }
            
            // Ensure processing status is maintained and store task info
            setUploadStatus(fileType, {processing: true, failed: false});
            
            // Show success message for task creation
            messageAlertPass("Upload started successfully! Processing in background...");
            
            // Start polling for task status
            if (task.id) {
                // Store task info globally for cancel functionality
                window.currentUploadTaskId = task.id;
                window.currentUploadFileType = fileType;
                console.log("Stored task ID for cancel:", task.id, "fileType:", fileType);
                
                pollTaskStatus(task.id, fileType);
            } else {
                // Fallback: refresh status after a delay
                setTimeout(function() {
                    fetchDataStatus();
                }, 2000);
            }
        },
        error: function (errormessage) {
            console.log("Async upload error:", errormessage);
            // Clear processing status and set failed status
            setUploadStatus(fileType, {processing: false, failed: true});
            
            if (errormessage.status === 429) {
                messageAlertFail("System is busy. Too many concurrent uploads. Please try again later.");
            } else if (errormessage.responseJSON && errormessage.responseJSON.errorMessage) {
                messageAlertFail("Upload failed: " + errormessage.responseJSON.errorMessage);
            } else if (errormessage.responseJSON && errormessage.responseJSON.message) {
                messageAlertFail(errormessage.responseJSON.message);
            } else if (errormessage.responseText) {
                messageAlertFail("Upload failed: " + errormessage.responseText);
            } else {
                messageAlertFail("Upload failed: " + errormessage.statusText);
            }
        }
    });
}

function pollTaskStatus(taskId, fileType) {
    var maxPolls = 120; // Maximum 10 minutes (120 * 5 seconds) for long-running tasks
    var pollCount = 0;
    
    console.log("🔍 Starting polling for task:", taskId, "fileType:", fileType);
    
    // Ensure task info is stored globally for cancel functionality
    window.currentUploadTaskId = taskId;
    window.currentUploadFileType = fileType;
    
    function checkStatus() {
        pollCount++;
        var pollUrl = getRunUrl() + '/tasks/' + taskId;
        console.log("📡 Polling attempt", pollCount, "URL:", pollUrl);
        
        $.ajax({
            url: pollUrl,
            type: 'GET',
            success: function(task) {
                console.log("📊 Task status poll " + pollCount + ":", task);
                
                if (task.status === "COMPLETED") {
                    setUploadStatus(fileType, {processing: false, failed: false});
                    messageAlertPass("Upload completed successfully! " + (task.progressMessage || ""));
                    fetchDataStatus(); // Refresh the data status
                    window.currentUploadTaskId = null; // Clear task reference
                    window.currentUploadFileType = null;
                } else if (task.status === "FAILED") {
                    setUploadStatus(fileType, {failed: true, processing: false});
                    messageAlertFail("Upload failed: " + (task.errorMessage || "Unknown error"));
                    window.currentUploadTaskId = null; // Clear task reference
                    window.currentUploadFileType = null;
                } else if (task.status === "CANCELLED") {
                    setUploadStatus(fileType, {processing: false, failed: false});
                    messageAlertWarn("Upload was cancelled by user");
                    fetchDataStatus(); // Refresh the data status
                    window.currentUploadTaskId = null; // Clear task reference
                    window.currentUploadFileType = null;
                } else if (task.status === "PENDING" || task.status === "RUNNING") {
                    console.log("🔄 Task is", task.status, "- maintaining processing status");
                    // Ensure processing status is maintained during polling
                    setUploadStatus(fileType, {processing: true, failed: false});
                    
                    // Update progress message if available
                    var progressMsg = task.progressMessage || "";
                    var progressPct = task.progressPercentage || 0;
                    console.log("📈 Progress: " + progressPct + "% - " + progressMsg);
                    
                    // Always update progress in the UI, even if percentage is 0
                    console.log("🎯 Updating UI with progress:", progressPct + "%", progressMsg);
                    setUploadStatus(fileType, {
                        processing: true, 
                        failed: false,
                        progressPercentage: progressPct,
                        progressMessage: progressMsg
                    });
                    
                    // Continue polling if not exceeded max attempts
                    if (pollCount < maxPolls) {
                        // Poll more frequently initially, then less frequently
                        var pollInterval = pollCount < 5 ? 2000 : 5000; // 2s for first 5 polls, then 5s
                        setTimeout(checkStatus, pollInterval);
                    } else {
                        messageAlertWarn("Upload is taking longer than expected. Please check the status later.");
                        setUploadStatus(fileType, {processing: false, failed: false});
                        window.currentUploadTaskId = null;
                        window.currentUploadFileType = null;
                    }
                } else {
                    // Unknown status, stop polling
                    messageAlertWarn("Upload status unknown. Please refresh the page to check current status.");
                    setUploadStatus(fileType, {processing: false, failed: false});
                    window.currentUploadTaskId = null;
                    window.currentUploadFileType = null;
                }
            },
            error: function(err) {
                console.error("❌ Error polling task status:", err);
                console.error("❌ Error details - Status:", err.status, "Response:", err.responseText);
                if (pollCount < maxPolls) {
                    var retryInterval = pollCount < 5 ? 2000 : 5000; // 2s for first 5 polls, then 5s
                    console.log("🔄 Retrying poll in " + (retryInterval/1000) + " seconds... (attempt " + pollCount + "/" + maxPolls + ")");
                    setTimeout(checkStatus, retryInterval);
                } else {
                    console.error("❌ Max polling attempts reached, stopping");
                    messageAlertWarn("Unable to check upload status. Please refresh the page.");
                    setUploadStatus(fileType, {processing: false, failed: false});
                    window.currentUploadTaskId = null;
                    window.currentUploadFileType = null;
                }
            }
        });
    }
    
    // Start polling immediately, then every 5 seconds
    checkStatus();
}

/**
 * Cancel the currently running task
 */
function cancelTask(taskId) {
    if (!taskId) {
        messageAlertFail("No task to cancel");
        return;
    }
    
    if (!confirm("Are you sure you want to cancel this upload?")) {
        return;
    }
    
    console.log("Cancelling task:", taskId);
    
    $.ajax({
        url: getRunUrl() + '/tasks/' + taskId + '/cancel',
        type: 'POST',
        success: function(response) {
            console.log("Cancellation requested for task:", taskId);
            messageAlertInfo("Cancellation requested. The task will stop shortly...");
        },
        error: function(err) {
            console.log("Error cancelling task:", err);
            messageAlertFail("Failed to cancel task: " + (err.responseText || "Unknown error"));
        }
    });
}

function downloadAsync(url, fileType) {
    messageAlertInfo("Starting download preparation...");
    
    $.ajax({
        url: url,
        type: 'POST',
        success: function(task) {
            console.log("Async download started:", task);
            
            if (task.status === "FAILED") {
                messageAlertFail("Download failed: " + (task.errorMessage || "Unknown error"));
                return;
            }
            
            messageAlertPass("Download preparation started! This may take a moment...");
            
            // Start polling for download completion
            if (task.id) {
                pollDownloadStatus(task.id, fileType);
            } else {
                messageAlertWarn("Unable to track download progress. Please try again.");
            }
        },
        error: function(errormessage) {
            console.log("Async download error:", errormessage);
            
            if (errormessage.status === 429) {
                messageAlertFail("System is busy. Too many concurrent downloads. Please try again later.");
            } else if (errormessage.responseJSON && errormessage.responseJSON.errorMessage) {
                messageAlertFail("Download failed: " + errormessage.responseJSON.errorMessage);
            } else if (errormessage.responseJSON && errormessage.responseJSON.message) {
                messageAlertFail(errormessage.responseJSON.message);
            } else if (errormessage.responseText) {
                messageAlertFail("Download failed: " + errormessage.responseText);
            } else {
                messageAlertFail("Download failed: " + errormessage.statusText);
            }
        }
    });
}

function pollDownloadStatus(taskId, fileType) {
    var maxPolls = 120; // Maximum 10 minutes (120 * 5 seconds) for long-running tasks
    var pollCount = 0;
    
    // Store taskId for potential cancellation
    window.currentDownloadTaskId = taskId;
    
    function checkDownloadStatus() {
        pollCount++;
        
        $.ajax({
            url: getRunUrl() + '/tasks/' + taskId,
            type: 'GET',
            success: function(task) {
                console.log("Download task status poll " + pollCount + ":", task);
                
                if (task.status === "COMPLETED") {
                    messageAlertPass("Download ready! Starting file download...");
                    window.currentDownloadTaskId = null; // Clear task reference
                    // Trigger the actual file download
                    if (task.resultUrl) {
                        // Use the task result endpoint to stream the file
                        var downloadUrl = getRunUrl() + '/tasks/' + taskId + '/result';
                        triggerFileDownload(downloadUrl);
                    } else {
                        messageAlertFail("Download completed but file not available. Please try again.");
                    }
                } else if (task.status === "FAILED") {
                    messageAlertFail("Download failed: " + (task.errorMessage || "Unknown error"));
                    window.currentDownloadTaskId = null; // Clear task reference
                } else if (task.status === "CANCELLED") {
                    messageAlertWarn("Download was cancelled by user");
                    window.currentDownloadTaskId = null; // Clear task reference
                } else if (task.status === "PENDING" || task.status === "RUNNING") {
                    // Update progress message if available
                    var progressMsg = task.progressMessage || "";
                    var progressPct = task.progressPercentage || 0;
                    console.log("Download progress: " + progressPct + "% - " + progressMsg);
                    
                    // Continue polling if not exceeded max attempts
                    if (pollCount < maxPolls) {
                        setTimeout(checkDownloadStatus, 5000); // Poll every 5 seconds
                    } else {
                        messageAlertWarn("Download is taking longer than expected. Please try again later.");
                        window.currentDownloadTaskId = null;
                    }
                } else {
                    // Unknown status, stop polling
                    window.currentDownloadTaskId = null;
                    messageAlertWarn("Download status unknown. Please try again.");
                }
            },
            error: function(err) {
                console.log("Error polling download task status:", err);
                if (pollCount < maxPolls) {
                    setTimeout(checkDownloadStatus, 5000); // Retry after 5 seconds
                } else {
                    messageAlertWarn("Unable to check download status. Please try again.");
                }
            }
        });
    }
    
    // Start polling after a short delay
    setTimeout(checkDownloadStatus, 2000);
}

// Safely trigger a file download without being blocked by pop-up blockers
function triggerFileDownload(downloadUrl) {
    try {
        var win = window.open(downloadUrl, '_blank');
        if (win && typeof win.focus === 'function') {
            try { win.focus(); return; } catch (e) { /* ignore and fallback */ }
        }
    } catch (e) {
        // ignore and fallback
    }

    // Fallback: hidden iframe allows multiple parallel downloads and avoids popup blockers
    var iframe = document.createElement('iframe');
    iframe.style.display = 'none';
    iframe.src = downloadUrl;
    document.body.appendChild(iframe);
    // Cleanup after a minute
    setTimeout(function() {
        try { document.body.removeChild(iframe); } catch (e) {}
    }, 60000);
}

function ajaxRestApiCall(url, type) {
	$.ajax({
		url: url,
		type: type,
		headers: {
			'Content-Type': 'application/json'
		},
		error: function (errormessage) {
			// Enhanced error handling for upload responses
            if (errormessage.responseJSON && errormessage.responseJSON.message) {
                messageAlertFail(errormessage.responseJSON.message);
            } else if (errormessage.responseJSON && errormessage.responseJSON.errors) {
                messageAlertFail("Upload failed: " + errormessage.responseJSON.errors.join(", "));
            } else if (errormessage.responseText) {
                messageAlertFail("Upload failed: " + errormessage.responseText);
            } else {
                messageAlertFail("Upload failed: " + errormessage.statusText);
            }
		}
	});
}

function messageAlertFail(message) {
         $.toast({
          heading: 'Error',
          text: message,
          icon: 'error',
          loader: false,
          showHideTransition: 'plain',
          hideAfter: 3000,
          position: {
            left: 1000,
            top: 30
          }
        })
}
function messageAlertPass(message) {
  $.toast({
    heading:'Success',
    text:message,
    icon:'success',
    loader: false,
    showHideTransition: 'fade',
    hideAfter: 3000,
    position: {
      left:1000,
      top:30
    }
  })
}
function messageAlertInfo(message)
{
  $.toast({
    heading:'Info',
    text:message,
    icon:'info',
    loader: false,
    showHideTransition: 'slide',
    hideAfter: 3000,
    position: {
      left:1000,
      top:10
    }
  })
}
function messageAlertWarn(message)
{
  $.toast({
    heading:'Warning',
    text:message,
    icon:'warning',
    loader: false,
    showHideTransition: 'slide',
    hideAfter: 3000,
    position: {
      left:1000,
      top:10
    }
  })
}

function uploadModal(id,name)
{
console.log(id)
console.log(name)
	var modal = $('#upload-data-form');
	modal.empty();
	messageTag ='<div class="modal-body-modern">'
	           +'<div class="upload-modal-header">'
	           +'<button type="button" class="modal-close-btn" data-dismiss="modal" aria-label="Close">'
	           +'<span aria-hidden="true">&times;</span>'
	           +'</button>'
	           +'<h3 class="upload-modal-title">Upload '+name+'</h3>'
	           +'</div>'
	           +'<div class="upload-modal-content">'
	           +'<div class="file-input-section">'
	           +'<div class="file-input-wrapper">'
	           +'<input type="file" id="file" name="file" onclick="resetButtons()" required>'
	           +'<label for="file" class="file-input-label">Choose file to upload</label>'
	           +'</div>'
	           +'</div>'
	           +'</div>'
	           +'<div class="upload-modal-actions">'
	           +'<button type="submit" class="btn btn-primary-modern" id="upload-sales" onclick="upload(\''+id+'\')">Upload File</button>'
	           +'<button type="button" class="btn btn-secondary-modern" data-dismiss="modal">Cancel</button>'
	           +'</div>'
	           +'<div id="modal-footer"></div>'
	modal.append(messageTag)
	$('#upload-modal').modal('toggle');
}
function downloadReportData(reportName)
{
	// Reports use synchronous download (no async endpoint available yet)
	var url = getReportUrl()+"/download/"+reportName;
	window.open(url, '_blank').focus();
}
function downloadErrorFile(id)
{
	// Error files are simple static files - synchronous download is fine
	url=getUploadUrl()+"/errors/"+id
	window.open(url, '_blank').focus();
}
function downloadInputFile(id)
{
	// Input files are simple static files - synchronous download is fine
	url=getUploadUrl()+"/input/"+id
	window.open(url, '_blank').focus();
}
function downloadInputFileTemplate(id)
{
	// Template files are simple static files - synchronous download is fine
	url=getUploadUrl()+"/template/"+id
	window.open(url, '_blank').focus();
}

// ========== Theme Toggle ========== 
function initThemeToggle(){
    var btn = document.getElementById('themeToggleBtn');
    var icon = document.getElementById('themeToggleIcon');
    if(!btn || !icon) {
        console.log('Theme toggle elements not found');
        return;
    }

    // Load preferred theme
    try {
        var saved = localStorage.getItem('toyiris.theme') || 'light';
        setTheme(saved);
    } catch(e) {
        console.log('Error loading theme:', e);
    }

    btn.addEventListener('click', function(){
        var current = document.documentElement.getAttribute('data-theme') || 'light';
        var next = current === 'dark' ? 'light' : 'dark';
        console.log('Switching theme from', current, 'to', next);
        setTheme(next);
        try { localStorage.setItem('toyiris.theme', next); } catch(e) {}
    });

    function setTheme(mode){
        document.documentElement.setAttribute('data-theme', mode);
        if(mode === 'dark') {
            icon.classList.remove('fa-moon-o');
            icon.classList.add('fa-sun-o');
        } else {
            icon.classList.remove('fa-sun-o');
            icon.classList.add('fa-moon-o');
        }
        console.log('Theme set to:', mode);
    }
}

// Initialize theme toggle when DOM is ready or immediately if already loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initThemeToggle);
} else {
    initThemeToggle();
}

function setUploadStatus(fileType, status) {
    console.log("🔄 setUploadStatus called:", fileType, status);
    
    // Initialize currentStatusData if it doesn't exist
    if (!window.currentStatusData) {
        console.log("🔧 Initializing currentStatusData");
        window.currentStatusData = {
            "styles": {"exists": false, "count": 0},
            "stores": {"exists": false, "count": 0},
            "skus": {"exists": false, "count": 0},
            "sales": {"exists": false, "count": 0}
        };
    }
    
    // Update the status in the current data
    var oldStatus = window.currentStatusData[fileType] || {};
    window.currentStatusData[fileType] = Object.assign(oldStatus, status);
    console.log("📊 Updated status for", fileType, ":", window.currentStatusData[fileType]);
    
    // Update only the specific card instead of re-rendering all cards
    updateSingleUploadCard(fileType, window.currentStatusData[fileType]);
    console.log("✅ UI refreshed for", fileType);
}

/**
 * Update a single upload card instead of re-rendering all cards
 * This is more efficient for task status updates
 */
function updateSingleUploadCard(fileType, statusData) {
    // Find the upload file configuration
    var uploadFile = null;
    for (var i in uploadJson.uploadFiles) {
        if (uploadJson.uploadFiles[i].id === fileType) {
            uploadFile = uploadJson.uploadFiles[i];
            break;
        }
    }
    
    if (!uploadFile) {
        console.warn("Upload file config not found for:", fileType);
        return;
    }
    
    // Find the card container for this file type
    var $cardsRow = $('#upload-cards-row');
    var $existingCard = $cardsRow.find('[data-file-type="' + fileType + '"]').parent();
    
    if ($existingCard.length === 0) {
        // Card doesn't exist yet, fall back to full re-render
        console.log("Card not found for", fileType, "- falling back to full render");
        displayUploadList(window.currentStatusData);
        return;
    }
    
    // Create new card HTML
    var newCardHtml = createUploadCard(statusData, uploadFile);
    
    // Replace the existing card
    $existingCard.replaceWith(newCardHtml);
    
    // Add data attribute to the new card for future updates
    $cardsRow.find('.card').last().attr('data-file-type', fileType);
}

function fetchDataStatus() {
    $.ajax({
        url: getUploadUrl() + '/status',
        type: 'GET',
        success: function(data) {
            window.currentStatusData = data;
            displayUploadList(data);
        },
        error: function(err) {
            console.log("Error fetching data status:", err);
        }
    });
}
function upload(id)
{
        var formData = new FormData();
        if(!$('input[type=file]')[0].files[0])
        {
            messageAlertFail("Please Select a FIle to Upload")
            return;
        }
        formData.append('file', $('input[type=file]')[0].files[0]);
        // Use async endpoints for better performance and reliability
        url=getUploadUrl() + '/upload/'+id+'/async';
        uploadAjaxAsync(url,formData)
		event.preventDefault();
        $('#upload-message-modal').on('hidden.bs.modal')
}

/**
 * Extract upload summary from backend messages
 * Handles both UPSERT (styles, SKUs, stores) and TRUNCATE (sales) patterns
 */
function extractUploadSummary(messages, fileType) {
    if (!messages || messages.length === 0) {
        return "Upload completed successfully";
    }
    
    // Look for UPSERT summary: "X inserted, Y updated"
    var upsertPattern = /(\d+)\s+inserted,\s+(\d+)\s+updated/i;
    var truncatePattern = /Saving\s+(\d+)/i;
    var completedPattern = /completed:\s+(\d+)\s+inserted,\s+(\d+)\s+updated/i;
    
    for (var i = 0; i < messages.length; i++) {
        var msg = messages[i];
        
        // Check for completed message with counts
        var completedMatch = msg.match(completedPattern);
        if (completedMatch) {
            var inserted = parseInt(completedMatch[1]);
            var updated = parseInt(completedMatch[2]);
            if (inserted > 0 && updated > 0) {
                return "✓ Upload successful: " + inserted + " new records added, " + updated + " existing records updated";
            } else if (inserted > 0) {
                return "✓ Upload successful: " + inserted + " new records added";
            } else if (updated > 0) {
                return "✓ Upload successful: " + updated + " records updated";
            }
        }
        
        // Check for UPSERT pattern
        var upsertMatch = msg.match(upsertPattern);
        if (upsertMatch) {
            var inserted = parseInt(upsertMatch[1]);
            var updated = parseInt(upsertMatch[2]);
            if (inserted > 0 && updated > 0) {
                return "✓ Upload successful: " + inserted + " new records added, " + updated + " existing records updated";
            } else if (inserted > 0) {
                return "✓ Upload successful: " + inserted + " new records added";
            } else if (updated > 0) {
                return "✓ Upload successful: " + updated + " records updated";
            }
        }
        
        // Check for TRUNCATE pattern (sales)
        var truncateMatch = msg.match(truncatePattern);
        if (truncateMatch) {
            var count = parseInt(truncateMatch[1]);
            return "✓ Upload successful: " + count + " records replaced";
        }
    }
    
    return "✓ Upload completed successfully";
}

/**
 * Clear all data from the database
 * Shows confirmation dialog before proceeding
 */
function clearAllData() {
    // Get current data counts for confirmation
    $.ajax({
        url: getUploadUrl() + '/status',
        type: 'GET',
        success: function(statusData) {
            var totalRecords = 0;
            var details = [];
            
            if (statusData.styles && statusData.styles.count > 0) {
                totalRecords += statusData.styles.count;
                details.push(statusData.styles.count + " styles");
            }
            if (statusData.skus && statusData.skus.count > 0) {
                totalRecords += statusData.skus.count;
                details.push(statusData.skus.count + " SKUs");
            }
            if (statusData.stores && statusData.stores.count > 0) {
                totalRecords += statusData.stores.count;
                details.push(statusData.stores.count + " stores");
            }
            if (statusData.sales && statusData.sales.count > 0) {
                totalRecords += statusData.sales.count;
                details.push(statusData.sales.count + " sales");
            }
            
            if (totalRecords === 0) {
                messageAlertFail("Database is already empty");
                return;
            }
            
            var confirmMessage = "⚠️ WARNING: This will permanently delete ALL data:\n\n" +
                                details.join("\n") + "\n\n" +
                                "Total: " + totalRecords + " records\n\n" +
                                "This action CANNOT be undone!\n\n" +
                                "Are you sure you want to proceed?";
            
            if (confirm(confirmMessage)) {
                performClearAllData();
            }
        },
        error: function(err) {
            console.log("Error fetching status:", err);
            // Fallback confirmation
            if (confirm("⚠️ WARNING: This will delete ALL data from the database.\n\nThis action CANNOT be undone!\n\nAre you sure?")) {
                performClearAllData();
            }
        }
    });
}

/**
 * Perform the actual clear all data operation
 */
function performClearAllData() {
    $.ajax({
        url: baseUrl + '/api/data/clear-all',
        type: 'DELETE',
        success: function(response) {
            if (response.success) {
                var deletedRecords = response.deletedRecords;
                var message = "✓ All data cleared successfully!\n\n" +
                             "Deleted: " + deletedRecords.total + " total records\n" +
                             "- " + deletedRecords.sales + " sales\n" +
                             "- " + deletedRecords.skus + " SKUs\n" +
                             "- " + deletedRecords.styles + " styles\n" +
                             "- " + deletedRecords.stores + " stores";
                
                messageAlertPass(message);
                
                // Refresh data status immediately and again after a delay
                if (typeof fetchDataStatus === 'function') {
                    fetchDataStatus(); // Immediate refresh
                    setTimeout(fetchDataStatus, 1000); // Delayed refresh to ensure backend is updated
                }
            } else {
                messageAlertFail("Failed to clear data: " + response.message);
            }
        },
        error: function(err) {
            console.log("Clear all data error:", err);
            if (err.responseJSON && err.responseJSON.message) {
                messageAlertFail("Failed to clear data: " + err.responseJSON.message);
            } else {
                messageAlertFail("Failed to clear data. Please try again.");
            }
        }
    });
}

