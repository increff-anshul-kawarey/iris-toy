tileData={}
function fill1stTile()
{
	var tile = $('#firstTile');
	tile.empty();
	// Tile 1: Sales Data Records
	var salesCount = tileData.totalSalesRecords ? tileData.totalSalesRecords.toLocaleString() : tileData.dashBoardTile1;
	var salesStatus = tileData.salesDataStatus || 'Data Records';
	tileTag ='<div class="dashboard-tile-modern tile-primary h-100 d-flex flex-column">'
	          +'<div class="dashboard-tile-header">'
	          +'<div class="dashboard-tile-icon"><i class="fa fa-database"></i></div>'
	          +'<div class="dashboard-tile-value">'+salesCount+'</div>'
	          +'<div class="dashboard-tile-label">Sales Records</div>'
	          +'<small class="text-muted">'+salesStatus+'</small></div>'
	          +'<div class="card-footer bg-transparent border-0 text-center pt-0">'
	          +'<div class="small mb-2 text-muted">Last updated: '+formatCurrentDate()+'</div>'
	          +'<a href="#" class="text-decoration-none" onclick="showCard1Detail()">'
	          +'<div class="d-flex justify-content-between align-items-center">'
             +'<span>View Details</span><i class="fa fa-chevron-right"></i></div></a></div></div>'
	tile.append(tileTag)
}
function fill2ndTile()
{
    	var tile = $('#secondTile');
    	tile.empty();
    	// Tile 2: Master Data (SKUs + Stores + Styles)
    	var totalMaster = (tileData.totalSkus || 0) + (tileData.totalStores || 0) + (tileData.totalStyles || 0);
    	var masterStatus = tileData.masterDataStatus || 'Master Data';
    	var masterCount = totalMaster > 0 ? totalMaster.toLocaleString() : (tileData.dashBoardTile2 || 0);
    	tileTag ='<div class="dashboard-tile-modern tile-success h-100 d-flex flex-column">'
    	         +'<div class="dashboard-tile-header">'
    	         +'<div class="dashboard-tile-icon"><i class="fa fa-cubes"></i></div>'
    	         +'<div class="dashboard-tile-value">'+masterCount+'</div>'
    	         +'<div class="dashboard-tile-label">Master Records</div>'
    	         +'<small class="text-muted">'+masterStatus+'</small></div>'
                 +'<div class="card-footer bg-transparent border-0 text-center pt-0">'
                 +'<div class="small mb-2 text-muted">Last updated: '+formatCurrentDate()+'</div>'
                 +'<a href="#" class="text-decoration-none" onclick="showCard2Detail()">'
                 +'<div class="d-flex justify-content-between align-items-center">'
                 +'<span>View Details</span><i class="fa fa-chevron-right"></i></div></a></div></div>'
    	tile.append(tileTag)

}
function fill3rdTile()
{
            var tile = $('#thirdTile');
            tile.empty();
            // Tile 3: Recent Upload Activity
            var recentUploads = tileData.recentUploads || tileData.dashBoardTile3 || 0;
            var successRate = tileData.uploadSuccessRate || 0;
            var activityStatus = tileData.recentActivityStatus || 'Recent Activity';
            tileTag ='<div class="dashboard-tile-modern tile-info h-100 d-flex flex-column">'
                    +'<div class="dashboard-tile-header">'
                    +'<div class="dashboard-tile-icon"><i class="fa fa-upload"></i></div>'
                    +'<div class="dashboard-tile-value">'+recentUploads+'</div>'
                    +'<div class="dashboard-tile-label">Recent Uploads</div>'
                    +'<small class="text-muted">'+Math.round(successRate)+'% success rate</small></div>'
                    +'<div class="card-footer bg-transparent border-0 text-center pt-0">'
                    +'<div class="small mb-2 text-muted">Last updated: '+formatCurrentDate()+'</div>'
                    +'<a href="#" class="text-decoration-none" onclick="showCard3Detail()">'
                    +'<div class="d-flex justify-content-between align-items-center">'
                       +'<span>View Details</span><i class="fa fa-chevron-right"></i></div></a></div></div>'
            tile.append(tileTag)

}
function fill4thTile()
{
            var tile = $('#fourthtile');
            tile.empty();
            // Tile 4: Processing Status
            var activeTasks = tileData.activeTasks || 0;
            var pendingTasks = tileData.pendingTasks || 0;
            var totalTasks = activeTasks + pendingTasks;
            var processingStatus = tileData.processingStatus || 'Processing Status';
            var displayCount = totalTasks > 0 ? totalTasks : (tileData.dashBoardTile4 || 0);
            tileTag ='<div class="dashboard-tile-modern tile-warning h-100 d-flex flex-column">'
                    +'<div class="dashboard-tile-header">'
                    +'<div class="dashboard-tile-icon"><i class="fa fa-cogs"></i></div>'
                    +'<div class="dashboard-tile-value">'+displayCount+'</div>'
                    +'<div class="dashboard-tile-label">Active Tasks</div>'
                    +'<small class="text-muted">'+processingStatus+'</small></div>'
                    +'<div class="card-footer bg-transparent border-0 text-center pt-0">'
                    +'<div class="small mb-2 text-muted">Last updated: '+formatCurrentDate()+'</div>'
                    +'<a href="#" class="text-decoration-none" onclick="showCard4Detail()">'
                    +'<div class="d-flex justify-content-between align-items-center">'
                       +'<span>View Details</span><i class="fa fa-chevron-right"></i></div></a></div></div>'
            tile.append(tileTag)
}
function showCard1Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
	// Sales Data Details
	var salesCount = tileData.totalSalesRecords ? tileData.totalSalesRecords.toLocaleString() : 'N/A';
	var salesStatus = tileData.salesDataStatus || 'Data unavailable';
    messageTag ='<h5><i class="fa fa-database"></i> Sales Data Overview</h5>'
              +'<div><strong>Total Sales Records: </strong>' + salesCount + '</div>'
              +'<div><strong>Status: </strong>' + salesStatus + '</div>'
              +'<hr><p class="text-muted">Sales data is essential for NOOS algorithm analysis. '
              +'More data typically leads to better classification accuracy.</p>'
	modal.append(messageTag)
	$('#message-modal').modal('toggle');
}
function showCard2Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
	// Master Data Details
	var totalSkus = tileData.totalSkus || 0;
	var totalStores = tileData.totalStores || 0;
	var totalStyles = tileData.totalStyles || 0;
	var masterStatus = tileData.masterDataStatus || 'Setup required';
	messageTag = '<h5><i class="fa fa-cubes"></i> Master Data Breakdown</h5>'
	            +'<div><strong>SKUs: </strong>' + totalSkus.toLocaleString() + '</div>'
	            +'<div><strong>Stores: </strong>' + totalStores.toLocaleString() + '</div>'
	            +'<div><strong>Styles: </strong>' + totalStyles.toLocaleString() + '</div>'
	            +'<div><strong>Status: </strong>' + masterStatus + '</div>'
	            +'<hr><p class="text-muted">Complete master data ensures accurate sales analysis '
	            +'and proper NOOS algorithm functionality.</p>'

	modal.append(messageTag)
	$('#message-modal').modal('toggle');
}
function showCard3Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
	// Recent Activity Details
	var recentUploads = tileData.recentUploads || 0;
	var successRate = tileData.uploadSuccessRate || 0;
	var activityStatus = tileData.recentActivityStatus || 'No recent activity';
	messageTag = '<h5><i class="fa fa-upload"></i> Recent Upload Activity (Last 7 Days)</h5>'
	            +'<div><strong>Total Uploads: </strong>' + recentUploads + '</div>'
	            +'<div><strong>Success Rate: </strong>' + Math.round(successRate) + '%</div>'
	            +'<div><strong>Status: </strong>' + activityStatus + '</div>'
	            +'<hr><p class="text-muted">Monitor upload activity to ensure data freshness '
	            +'for optimal algorithm performance.</p>'
	modal.append(messageTag)
	$('#message-modal').modal('toggle');
}
function showCard4Detail() {
	var modal = $('#message-modal-body');
	modal.empty();
	// Processing Status Details
	var activeTasks = tileData.activeTasks || 0;
	var pendingTasks = tileData.pendingTasks || 0;
	var processingStatus = tileData.processingStatus || 'System idle';
    messageTag ='<h5><i class="fa fa-cogs"></i> Processing Status</h5>'
              +'<div><strong>Active Tasks: </strong>' + activeTasks + '</div>'
              +'<div><strong>Pending Tasks: </strong>' + pendingTasks + '</div>'
              +'<div><strong>System Status: </strong>' + processingStatus + '</div>'
              +'<hr><p class="text-muted">Track system processing load and task queue status '
              +'to monitor system health.</p>'
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
/**
 * Format current date for display in tiles
 */
function formatCurrentDate() {
    var now = new Date();
    return now.toLocaleDateString() + ' ' + now.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

//figure out when you want tiles data to be refreshed
$(document).ready(fillTiles)

