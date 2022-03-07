function getUploadList() {
//  you can get Updates for older run from here synchronous  may not be the right way
//	var url =;
//	ajaxRestApiCallWithoutData(url, 'GET', displayUploadList);
data={
"file1":"success",
"file2":"failed",
"file3":"processing"
}
displayUploadList(data)
}
function displayUploadList(data) {
    $('#upload-table').append(uploadTableHead)
    var $tbody = $('#upload-table').find('tbody');
    $tbody.empty();
    for (var i in uploadJson.uploadFiles) {
		var dataItem = uploadJson.uploadFiles[i];
		row=createTableRow(data[dataItem.id],dataItem)
		$tbody.append(row);
	}
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
$(document).ready(getUploadList);