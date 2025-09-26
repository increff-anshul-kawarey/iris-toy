function getUploadList() {
    // Fetch actual data status from backend
    fetchDataStatus();
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
            // Fallback to mock data if API fails
            var mockData = {
                "styles": {"exists": false, "count": 0},
                "stores": {"exists": false, "count": 0},
                "skus": {"exists": false, "count": 0},
                "sales": {"exists": false, "count": 0}
            };
            window.currentStatusData = mockData;
            displayUploadList(mockData);
        }
    });
}
function displayUploadList(data) {
    // Keep the legacy table for backward compatibility (but hidden)
    $('#upload-table').append(uploadTableHead)
    var $tbody = $('#upload-table').find('tbody');
    $tbody.empty();

    // Clear and populate the new card layout
    var $cardsRow = $('#upload-cards-row');
    $cardsRow.empty();

    for (var i in uploadJson.uploadFiles) {
		var dataItem = uploadJson.uploadFiles[i];
		// Add to legacy table
		row=createTableRow(data[dataItem.id],dataItem)
		$tbody.append(row);

		// Create and add card
		var card = createUploadCard(data[dataItem.id], dataItem);
		$cardsRow.append(card);
	}
}

function createUploadCard(statusData, uploadFile) {
    var statusClass = '';
    var statusTitle = '';
    var statusIcon = '';
    var primaryMessage = '';
    var secondaryMessage = '';
    var cardClass = 'upload-card-modern';

    // Determine status based on data existence and count
    // Processing takes priority over exists status
    if (statusData && statusData.processing) {
        statusClass = 'status-processing';
        statusTitle = 'Processing in progress';
        statusIcon = '<div class="processing-dots"><div class="dot"></div><div class="dot"></div><div class="dot"></div></div>';
        primaryMessage = 'Your file is being processed';
        secondaryMessage = 'Estimated time remaining: ~2 minutes';
        cardClass += ' card-processing';
    } else if (statusData && statusData.failed) {
        statusClass = 'status-error';
        statusTitle = 'Processing paused';
        statusIcon = '<i class="fa fa-exclamation-triangle status-icon-large"></i>';
        primaryMessage = 'Processing paused due to error';
        secondaryMessage = 'Please check your file and try again';
        cardClass += ' card-error';
    } else if (statusData && statusData.exists) {
        statusClass = 'status-success';
        statusTitle = 'Processing completed';
        statusIcon = '<i class="fa fa-check-circle status-icon-large"></i>';
        primaryMessage = 'Your file is ready';
        secondaryMessage = statusData.count + ' records uploaded successfully';
        cardClass += ' card-success';
    } else {
        statusClass = 'status-pending';
        statusTitle = 'Ready for upload';
        statusIcon = '<i class="fa fa-upload status-icon-large"></i>';
        primaryMessage = 'Upload your data file';
        secondaryMessage = 'Click upload to get started';
        cardClass += ' card-pending';
    }

    var cardHtml = '<div class="col-md-6 mb-4">' +
        '<div class="card ' + cardClass + '">' +
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
                    '<button type="button" class="btn btn-primary-modern" onclick="uploadModal(\''+uploadFile.id+'\',\''+uploadFile["Display Name"]+'\')">' +
                        (statusData && statusData.failed ? 'Retry Upload' : 'Upload File') +
                    '</button>' +
                    '<button type="button" class="btn btn-secondary-modern" onclick="downloadDataFile(\''+uploadFile.id+'\')" ' + (statusData && statusData.exists ? '' : 'disabled') + '>' +
                        'Download Data' +
                    '</button>' +
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
	url=getUploadUrl()+"/download/"+id
	window.open(url, '_blank').focus();
}

$(document).ready(getUploadList);