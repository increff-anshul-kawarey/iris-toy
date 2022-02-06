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
		var buttonHtml = ''
		var row = '<tr>'
			+'<td>' + index + '</td>'
			+'<td>' + dataItem.field1 + '</td>'
			+'<td>' + dataItem.field2 + '</td>'
			+'<td>' + dataItem.field3 + '</td>'
			+'<td>' + dataItem.field4 + '</td>'
			+ '</tr>';
		$tbody.append(row);
	}
	$('#report1Table').DataTable();
}
$(document).ready(getReport1List);