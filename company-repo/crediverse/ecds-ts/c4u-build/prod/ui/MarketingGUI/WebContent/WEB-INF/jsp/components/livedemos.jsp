<!-- CSS -->

<link href="resources/css/index.css" rel="stylesheet"/>
<link href="resources/css/lastUpdate.css" rel="stylesheet"/>

<!-- HTML -->

<div class="live-demo-items row">
	<div class="col-md-4 col-sm-6 livedemo-item">
		<small class="text-muted admin-last-update" style="font-size:6pt">&nbsp;</small>
		<a id="admin-demo" href="#admin-modal-demo" class="livedemo-link" data-toggle="modal">
	    	<div class="livedemo-hover">
	        	<div class="livedemo-hover-content">
	            	<i class="fa fa-plus fa-3x"></i>
	          	</div>
	      	</div>
	      	<img src="resources/img/livedemo/admin-livedemo.png" class="img-responsive" alt="">
	  	</a>
	  	<div class="livedemo-caption">
	    	<h4>Admin</h4>
	      	<p class="text-muted">Handles the configuration side of C4U.</p>
	  	</div>
	</div>
	<div class="col-md-4 col-sm-6 livedemo-item">
		<small class="text-muted custcare-last-update" style="font-size:6pt">&nbsp;</small>
		<a id="crm-demo" href="#crm-modal-demo" class="livedemo-link" data-toggle="modal">
	    	<div class="livedemo-hover">
	        	<div class="livedemo-hover-content">
	           		<i class="fa fa-plus fa-3x"></i>
	        	</div>
	      	</div>
	      	<img src="resources/img/livedemo/crm-livedemo.png" class="img-responsive" alt="">
	  	</a>
	  	<div class="livedemo-caption">
	    	<h4>Customer Care</h4>
	    	<p class="text-muted">The customer relations management GUI. <a id="tutorialLink" class="page-scroll" href="#livedemo">Need Help?</a></p>
	  	</div>
	</div>
	<div class="col-md-4 col-sm-6 livedemo-item">
		<small class="text-muted simobi-last-update" style="font-size:6pt">&nbsp;</small>
	  	<a id="simobi-demo" href="#simobi-modal-demo" class="livedemo-link" data-toggle="modal">
	    	<div class="livedemo-hover">
	       		<div class="livedemo-hover-content">
	           		<i class="fa fa-plus fa-3x"></i>
	       		</div>
	    	</div>
	    	<img src="resources/img/livedemo/simobi-livedemo.png" class="img-responsive" alt="">
	  	</a>
	  	<div class="livedemo-caption">
	    	<h4>Simobi</h4>
	    	<p class="text-muted">Mobile phone simulations for feature and smartphones.</p>
	  	</div>
	</div>
</div>

<br/>

<div class="row" style="text-align: center">

	<h3 class="section-subheading text-muted">Try out our Android application by clicking the image below:</h3>
	<a href="/c4u/components/download/apk"><img src="resources/img/android.png" width="250px"/></a>

</div>
            		
<!-- livedemo Modals -->
<!-- Use the modals below to showcase details about your livedemo projects! -->

<!-- livedemo Modal 1 -->
<div class="livedemo-modal modal fade" id="admin-modal-demo" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="close-modal" data-dismiss="modal">
    	<button class="btn btn-danger btn-large close-modal-button">X</button>
    </div>
    <div class="modal-content"></div>
</div>

<!-- livedemo Modal 2 -->
<div class="livedemo-modal modal fade" id="crm-modal-demo" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="close-modal" data-dismiss="modal">
    	<button class="btn btn-danger btn-large close-modal-button">X</button>
    </div>
    <div class="modal-content"></div>
</div>

<!-- livedemo Modal 3 -->
<div class="livedemo-modal modal fade" id="simobi-modal-demo" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="close-modal" data-dismiss="modal">
    	<button class="btn btn-danger btn-large close-modal-button">X</button>
    </div>
    <div class="modal-content">
    
    	<div id="dialog-position-1" class="phone-container col-xs-6 col-sm-6 col-md-6">
	    	<div id="simobi-number-1"></div>
	    	<div id="dialog-1">
	    		<div class="phone-emulator">
	    			<div id="simobi-term-1"></div>
	    		</div>
	    	</div>
    	</div>
    
    	<div id="dialog-position-2" class="phone-container col-xs-6 col-sm-6 col-md-6">
	    	<div id="simobi-number-2"></div>
	    	<div id="dialog-2">
	    		<div class="phone-emulator">
	    			<div id="simobi-term-2"></div>
	    		</div>
	    	</div>
    	</div>
    
    </div>
</div>
  
<!-- JAVASCRIPT --> 

<!-- Custom Theme JavaScript -->
<script src="resources/js/components/livedemos.js"></script>