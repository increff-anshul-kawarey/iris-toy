tileData={}
function fill1stTile()
{
	var tile = $('#firstTile');
	tile.empty();
	tileTag ='<div class="card bg-primary text-white h-100"><div class="card-body text-center py-1 mt-2" >'
	          +'<h5>Tile '+tileData.dashBoardTile1+' </h5><text>lorem ipsum</text><br><br><br></div><a href="#"  class="text-white" onclick="showCard1Detail()"><div class="card-footer d-flex">'
             +'View Details<span class="ms-auto"><i class="bi bi-chevron-right"></i></span></div></a></div>'
	tile.append(tileTag)
}
function fill2ndTile()
{
    	var tile = $('#secondTile');
    	tile.empty();
    	tileTag ='<div class="card bg-warning text-dark h-100"><div class="card-body text-center py-1 mt-2" >'
    	         +'<h2> '+tileData.dashBoardTile2+'%</h2>'
                 +'lorem ipsum<br>'
                 +'<strong>10 % </strong> lorem ipsum.</div><a href="#"  class="text-dark" onclick="showCard2Detail()"><div class="card-footer d-flex">'
                 +'View Details<span class="ms-auto"><i class="bi bi-chevron-right"></i></span></div></a></div>'
    	tile.append(tileTag)

}
function fill3rdTile()
{
            var tile = $('#thirdTile');
            tile.empty();
            tileTag ='<div class="card bg-success text-white h-100"><div class="card-body text-center py-1 mt-2" >'
                    +'<h2>'+tileData.dashBoardTile3+'</h2>'
                    +'<strong><p>lorem ipsum</p></strong></div><a href="#"  class="text-white" onclick="showCard3Detail()"><div class="card-footer d-flex">'
                       +'View Details<span class="ms-auto"><i class="bi bi-chevron-right"></i></span></div></a></div>'
            tile.append(tileTag)

}
function fill4thTile()
{
            var tile = $('#fourthtile');
            tile.empty();
            tileTag ='<div class="card bg-danger text-white h-100"><div class="card-body text-center py-1 mt-2" >'
                    +'<h2>'+tileData.dashBoardTile4+'</h2>'
                    +'<strong><p>lorem ipsum</p></strong></div><a href="#"  class="text-white" onclick="showCard4Detail()"><div class="card-footer d-flex">'
                       +'View Details<span class="ms-auto"><i class="bi bi-chevron-right"></i></span></div></a></div>'
            tile.append(tileTag)
}
function showCard1Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
    messageTag ='<text>Your Message for 1 Tile</text><br>'
	modal.append(messageTag)
	$('#message-modal').modal('toggle');
}
function showCard2Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
	messageTag = '<div><strong>Anything : </strong> abc </div>'
	            +'<div><strong>Anything : </strong> abc </div>'
	            +'<div><strong>Anything : </strong> abc </div>'

	modal.append(messageTag)
	$('#message-modal').modal('toggle');
}
function showCard3Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
	messageTag = '<div><strong>Anything : </strong> abc </div>'
	            +'<div><strong>Anything : </strong> abc </div>'
	            +'<div><strong>Anything : </strong> abc </div>'
	modal.append(messageTag)
	$('#message-modal').modal('toggle');
}
function showCard4Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
    messageTag ='<text>Your Message for 4 Tile</text><br>'
	modal.append(messageTag)
	$('#message-modal').modal('toggle');
}
function setTilesData(data)
{
tileData=data
fill1stTile()
fill2ndTile()
fill3rdTile()
fill4thTile()
$('#cardview').show()
}
function fillTiles()
{
    url=getRunUrl()+"/updates";
    ajaxRestApiCallWithoutData(url,'GET', setTilesData);
}
//figure out when you want tiles data to be refreshed
$(document).ready(fillTiles)

