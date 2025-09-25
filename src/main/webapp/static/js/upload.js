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
    var statusIcon = '';
    var statusText = '';
    var cardClass = 'upload-card';

    // Determine status based on data existence and count
    if (statusData && statusData.exists) {
        statusClass = 'status-success';
        statusIcon = '<i class="fa fa-check status-icon"></i>';
        statusText = statusData.count + ' records';
        cardClass += ' card-success';
    } else if (statusData && statusData.processing) {
        statusClass = 'status-processing';
        statusIcon = '<i class="fa fa-spinner fa-spin status-icon"></i>';
        statusText = 'Processing...';
        cardClass += ' card-processing';
    } else if (statusData && statusData.failed) {
        statusClass = 'status-failed';
        statusIcon = '<i class="fa fa-exclamation status-icon" onclick="downloadErrorFile(\''+uploadFile.id+'\')"></i>';
        statusText = 'Upload Failed';
        cardClass += ' card-failed';
    } else {
        statusClass = 'status-pending';
        statusIcon = '<i class="fa fa-circle-o status-icon"></i>';
        statusText = 'No data';
        cardClass += ' card-pending';
    }

    var cardHtml = '<div class="col-lg-6 col-xl-4 mb-4">' +
        '<div class="card ' + cardClass + ' h-100">' +
            '<div class="card-header">' +
                '<div class="d-flex justify-content-between align-items-center">' +
                    '<h5 class="card-title mb-0">' +
                        '<i class="fa ' + getFileIcon(uploadFile.id) + ' mr-2"></i>' +
                        uploadFile["Display Name"] +
                    '</h5>' +
                    '<div class="status-indicator ' + statusClass + '">' +
                        statusIcon +
                    '</div>' +
                '</div>' +
            '</div>' +
            '<div class="card-body">' +
                '<p class="card-text">' + uploadFile.Description + '</p>' +
                '<div class="status-text ' + statusClass + '">' + statusText + '</div>' +
            '</div>' +
            '<div class="card-footer">' +
                '<div class="btn-group w-100" role="group">' +
                    '<button type="button" class="btn btn-upload" onclick="uploadModal(\''+uploadFile.id+'\',\''+uploadFile["Display Name"]+'\')">' +
                        'Upload' +
                    '</button>' +
                    '<button type="button" class="btn btn-download" onclick="downloadDataFile(\''+uploadFile.id+'\')" ' + (statusData && statusData.exists ? '' : 'disabled') + '>' +
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