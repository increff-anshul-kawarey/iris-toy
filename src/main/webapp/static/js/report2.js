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
		var buttonHtml = ''
		var row = '<tr>'
			+'<td>' + index + '</td>'
			+'<td>' + dataItem.field1 + '</td>'
			+'<td>' + dataItem.field2 + '</td>'
			+'<td>' + dataItem.field3 + '</td>'
			+'<td>' + dataItem.field4 + '</td>'
			+'<td>' + dataItem.field5 + '</td>'
			+ '</tr>';
		$tbody.append(row);
	}
	$('#report2Table').DataTable();
}
$(document).ready(getReport2List);