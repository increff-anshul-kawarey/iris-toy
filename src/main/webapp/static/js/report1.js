function getReport1List() {
	var url = getReportUrl()+"/report1";
	ajaxRestApiCallWithoutData(url, 'GET', displayReport1List);
}
//what if data to display is to big ? will you show it on UI ?
function displayReport1List(data) {
    $('#report1Table').append(report1TableHead)
	var $tbody = $('#report1Table').find('tbody');
	$tbody.empty();
	for (var i in data) {
		var dataItem = data[i];
		var index = parseInt(i) + 1
		
		// Use meaningful field names (with fallback to legacy fields)
		var algorithmLabel = dataItem.algorithmLabel || dataItem.field1 || 'N/A';
		var executionStatus = dataItem.executionStatus || dataItem.field2 || 'UNKNOWN';
		var stylesProcessed = dataItem.totalStylesProcessed || dataItem.field3 || 'N/A';
		
		// Create meaningful classification summary
		var coreStyles = dataItem.coreStyles || 0;
		var bestsellerStyles = dataItem.bestsellerStyles || 0; 
		var fashionStyles = dataItem.fashionStyles || 0;
		var classificationSummary = coreStyles + bestsellerStyles + fashionStyles > 0 ? 
			`C:${coreStyles} B:${bestsellerStyles} F:${fashionStyles}` : 
			(dataItem.field4 || 'N/A');
		
		var statusBadge = executionStatus === 'COMPLETED' ? 'badge-success' : 
		                 executionStatus === 'FAILED' ? 'badge-danger' : 
		                 executionStatus === 'RUNNING' ? 'badge-primary' : 'badge-secondary';
		
		var row = '<tr>'
			+'<td>' + index + '</td>'
			+'<td><strong>' + algorithmLabel + '</strong></td>'
			+'<td><span class="badge ' + statusBadge + '">' + executionStatus + '</span></td>'
			+'<td><span class="text-info font-weight-bold">' + (typeof stylesProcessed === 'number' ? stylesProcessed.toLocaleString() : stylesProcessed) + '</span></td>'
			+'<td><span class="badge badge-outline-primary">' + classificationSummary + '</span></td>'
			+ '</tr>';
		$tbody.append(row);
	}
	$('#report1Table').DataTable({
		"order": [[ 0, "desc" ]], // Sort by most recent first
		"pageLength": 25
	});
}
$(document).ready(getReport1List);