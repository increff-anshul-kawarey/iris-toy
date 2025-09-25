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
return baseUrl + "/api/run";
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
	messageTag ='<div class="modal-body" id="upload-form-body">'
	           +'<div><strong>Upload : </strong>'+name+ '</div>'
               +'<input type="file" id="file" name="file" onclick="resetButtons()" required><br>'
               +'<a href="#" onclick="downloadInputFileTemplate(\''+id+'\')">Download Template</a><br>'
               +'</div>'
               +'<div class="modal-footer" >'
               +'<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>'
               +'<button type="submit" class="btn btn-primary" id="upload-sales" onclick="upload(\''+id+'\')">Upload</button>'
               +'<div id="modal-footer"></div>'
	modal.append(messageTag)
	$('#upload-modal').modal('toggle');
}
function downloadReportData(reportName)
{
	var url = getReportUrl()+"/download/"+reportName; and 
	window.open(url, '_blank').focus();
}
function downloadErrorFile(id)
{
	url=getUploadUrl()+"/errors/"+id
	window.open(url, '_blank').focus();
}
function downloadInputFile(id)
{
	url=getUploadUrl()+"/input/"+id
	window.open(url, '_blank').focus();
}
function downloadInputFileTemplate(id)
{
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
        url=getUploadUrl() + '/upload/'+id;
        uploadAjax(url,formData)
		event.preventDefault();
        $('#upload-message-modal').on('hidden.bs.modal')
}








