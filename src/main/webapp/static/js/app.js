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
                messageAlertPass(data.message || "Upload completed successfully");

                // Show processing messages if available
                if (data.messages && data.messages.length > 0) {
                    // Show key messages to user
                    var keyMessages = data.messages.filter(function(msg) {
                        return msg.includes("completed") || msg.includes("Saving") || msg.includes("Clearing");
                    });
                    if (keyMessages.length > 0) {
                        setTimeout(function() {
                            messageAlertPass("Details: " + keyMessages.join(". "));
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

    // Set processing status
    setUploadStatus(fileType, {processing: true});

    $.ajax({
        url: url,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (task) {
            console.log("Async upload started:", task);
            
            if (task.status === "FAILED") {
                setUploadStatus(fileType, {failed: true});
                messageAlertFail("Upload failed: " + (task.errorMessage || "Unknown error"));
                return;
            }
            
            // Show success message for task creation
            messageAlertPass("Upload started successfully! Processing in background...");
            
            // Start polling for task status
            if (task.id) {
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
            setUploadStatus(fileType, {failed: true});
            
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
    var maxPolls = 60; // Maximum 5 minutes (60 * 5 seconds)
    var pollCount = 0;
    
    function checkStatus() {
        pollCount++;
        
        $.ajax({
            url: getRunUrl() + '/tasks/' + taskId,
            type: 'GET',
            success: function(task) {
                console.log("Task status poll " + pollCount + ":", task);
                
                if (task.status === "COMPLETED") {
                    setUploadStatus(fileType, {processing: false});
                    messageAlertPass("Upload completed successfully! " + (task.progressMessage || ""));
                    fetchDataStatus(); // Refresh the data status
                } else if (task.status === "FAILED") {
                    setUploadStatus(fileType, {failed: true, processing: false});
                    messageAlertFail("Upload failed: " + (task.errorMessage || "Unknown error"));
                } else if (task.status === "PENDING" || task.status === "IN_PROGRESS") {
                    // Update progress message if available
                    if (task.progressMessage) {
                        console.log("Progress: " + task.progressMessage);
                    }
                    
                    // Continue polling if not exceeded max attempts
                    if (pollCount < maxPolls) {
                        setTimeout(checkStatus, 5000); // Poll every 5 seconds
                    } else {
                        messageAlertWarn("Upload is taking longer than expected. Please check the status later.");
                        setUploadStatus(fileType, {processing: false});
                    }
                } else {
                    // Unknown status, stop polling
                    messageAlertWarn("Upload status unknown. Please refresh the page to check current status.");
                    setUploadStatus(fileType, {processing: false});
                }
            },
            error: function(err) {
                console.log("Error polling task status:", err);
                if (pollCount < maxPolls) {
                    setTimeout(checkStatus, 5000); // Retry after 5 seconds
                } else {
                    messageAlertWarn("Unable to check upload status. Please refresh the page.");
                    setUploadStatus(fileType, {processing: false});
                }
            }
        });
    }
    
    // Start polling after a short delay
    setTimeout(checkStatus, 2000);
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
    var maxPolls = 60; // Maximum 5 minutes (60 * 5 seconds)
    var pollCount = 0;
    
    function checkDownloadStatus() {
        pollCount++;
        
        $.ajax({
            url: getRunUrl() + '/tasks/' + taskId,
            type: 'GET',
            success: function(task) {
                console.log("Download task status poll " + pollCount + ":", task);
                
                if (task.status === "COMPLETED") {
                    messageAlertPass("Download ready! Starting file download...");
                    // Trigger the actual file download
                    if (task.resultUrl) {
                        // Use the task result endpoint to stream the file
                        var downloadUrl = getRunUrl() + '/tasks/' + taskId + '/result';
                        window.open(downloadUrl, '_blank').focus();
                    } else {
                        messageAlertFail("Download completed but file not available. Please try again.");
                    }
                } else if (task.status === "FAILED") {
                    messageAlertFail("Download failed: " + (task.errorMessage || "Unknown error"));
                } else if (task.status === "PENDING" || task.status === "IN_PROGRESS") {
                    // Update progress message if available
                    if (task.progressMessage) {
                        console.log("Download progress: " + task.progressMessage);
                    }
                    
                    // Continue polling if not exceeded max attempts
                    if (pollCount < maxPolls) {
                        setTimeout(checkDownloadStatus, 5000); // Poll every 5 seconds
                    } else {
                        messageAlertWarn("Download is taking longer than expected. Please try again later.");
                    }
                } else {
                    // Unknown status, stop polling
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

function setUploadStatus(fileType, status) {
    // Update the status in the current data and refresh display
    if (window.currentStatusData) {
        window.currentStatusData[fileType] = Object.assign(window.currentStatusData[fileType] || {}, status);
        // Trigger a re-render of the upload list
        displayUploadList(window.currentStatusData);
    }
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








