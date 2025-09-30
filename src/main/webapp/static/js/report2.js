function getReport2List() {
	var url = getReportUrl()+"/report2";
	ajaxRestApiCallWithoutData(url, 'GET', displayReport2List);
}
function displayReport2List(data) {
    $('#report2Table').append(report2TableHead)
	var $tbody = $('#report2Table').find('tbody');
	$tbody.empty();
	for (var i in data) {
		var dataItem = data[i];
		var index = parseInt(i) + 1
		
		// Use meaningful field names (with fallback to legacy fields)
		var totalTasks = dataItem.totalTasks || dataItem.field1 || 0;
		var successfulTasks = dataItem.successfulTasks || dataItem.field2 || 0;
		var taskType = dataItem.taskType || dataItem.field3 || 'Unknown';
		var successRate = dataItem.successRate ? dataItem.successRate.toFixed(1) + '%' : 
						 (dataItem.field4 ? dataItem.field4.toFixed(1) + '%' : 'N/A');
		var avgTime = dataItem.averageExecutionTime ? dataItem.averageExecutionTime.toFixed(1) + 's' : 
					 (dataItem.field5 ? dataItem.field5.toFixed(1) + 's' : 'N/A');
		
		// Format task type for better display
		var formattedTaskType = taskType.replace(/_/g, ' ').toLowerCase()
								.replace(/\b\w/g, l => l.toUpperCase());
		
		var row = '<tr>'
			+'<td>' + index + '</td>'
			+'<td><span class="badge badge-info">' + totalTasks + '</span></td>'
			+'<td><span class="badge badge-success">' + successfulTasks + '</span></td>'
			+'<td><span class="text-muted">' + formattedTaskType + '</span></td>'
			+'<td><span class="font-weight-bold ' + (parseFloat(successRate) >= 80 ? 'text-success' : parseFloat(successRate) >= 50 ? 'text-warning' : 'text-danger') + '">' + successRate + '</span></td>'
			+'<td>' + avgTime + '</td>'
			+ '</tr>';
		$tbody.append(row);
	}
	$('#report2Table').DataTable({
		"order": [[ 4, "desc" ]], // Sort by success rate descending
		"pageLength": 25
	});
}
$(document).ready(getReport2List);