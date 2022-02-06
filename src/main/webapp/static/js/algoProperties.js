algoParams={}
function fillUpdateForm()
{
    url=getAlgoUrl();
    ajaxRestApiCallWithoutData(url,'GET', setAlgoParams);
}
function setAlgoParams(data)
{
 algoParams=data
}

function successFunction(data)
{
$('#update-properties-modal').modal('toggle');
algoParams=data
messageAlertPass("Parameters Updated Successfully")
}

function showUpdateForm()
{
   displayEditAlgo(algoParams)
}
function displayEditAlgo(data) {
	$("#algo-edit-form input[name=parameter1]").val(data.parameter1);
	$("#algo-edit-form input[name=parameter2]").val(data.parameter2);
	$("#algo-edit-form input[name=parameter3]").val(data.parameter3);
	$("#algo-edit-form input[name=parameter4]").val(data.parameter4);
	$("#algo-edit-form input[name=parameter5]").val(data.parameter5);
	$('#update-properties-modal').modal('toggle');
}
function updateAlgo(event) {
    	var data = {};
    	data["parameter1"] =$("#algo-edit-form input[name=parameter1]").val();
        data["parameter2"] =$("#algo-edit-form input[name=parameter2]").val();
        data["parameter3"] =$("#algo-edit-form input[name=parameter3]").val();
        data["parameter4"] =$("#algo-edit-form input[name=parameter4]").val();
        data["parameter5"] =$("#algo-edit-form input[name=parameter5]").val();
    	var json = JSON.stringify(data);
    	url=getAlgoUrl();
        ajaxRestApiCallWithData(url, 'POST',successFunction, json)
        event.preventDefault();

}
function startAlgo(algo)
{
url=getRunUrl()+'/'+algo;
ajaxRestApiCallWithoutData(url,'GET',algoSuccess);
}
function algoSuccess(data)
{
messageAlertPass("Algorithm Run Started")
fillTiles(data)
}

function messageRunModal(algoName) {
	var modal = $('#message-modal-body-runalgo');
	modal.empty();
	messageTag = '<div><strong>Algo Selected : </strong>'+algoName+ '</div>'
	            +'<div><strong>Parameters Given </strong>'
	            +'<div><text>Parameter1 : '+algoParams.parameter1+' </text></div>'
	            +'<div><text>Parameter2 : '+algoParams.parameter2+' </text></div>'
                +'<div><text>Parameter3 : '+algoParams.parameter3+' </text></div>'
                +'<div><text>Parameter4 : '+algoParams.parameter4+' </text></div>'
                +'<div><text>Parameter5: '+algoParams.parameter5+'</text></div>'
	modal.append(messageTag)
	var modalfoot=$('#message-modal-foot-runalgo');
	modalfoot.empty();
	footTag='<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>'
	        +'<button type="button" class="btn btn-success" data-dismiss="modal" onclick="startAlgo(\''+algoName+'\')">Start</button>'
	modalfoot.append(footTag)
	$('#message-modal-runalgo').modal('toggle');
}
function runAlgo(algoName)
{
    messageRunModal(algoName)
}
function displayDatePicker(){
    $("#parameter5").datepicker({
        numberOfMonths: 1,
        dateFormat: 'yy-mm-dd'
    });
}
function init() {
	$('#updatealgo').click(showUpdateForm);
	$('#update-algo').click(updateAlgo);
}
$(document).ready(init);
$(document).ready(fillUpdateForm)
$(document).ready(displayDatePicker);