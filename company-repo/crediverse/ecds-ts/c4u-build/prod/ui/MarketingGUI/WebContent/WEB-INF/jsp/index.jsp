<!DOCTYPE html>
<html lang="en">

<head>

    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="description" content="The C4U Demo Lab for testing the C4U products."/>
    <meta name="author" content="Concurrent Systems"/>

    <title>Demo4U</title>

    <!-- Bootstrap Core CSS -->
    <link href="resources/css/bootstrap.min.css" rel="stylesheet"/>

	<!-- Toast Plugin -->
	<link href="resources/css/toastr.css" rel="stylesheet"/>

	<!-- JQuery Terminal Plugin -->
	<link href="resources/css/jquery.terminal.css" rel="stylesheet"/>
	<link href="resources/css/jquery-ui.min.css" rel="stylesheet"/>
	<link href="resources/css/jquery-ui.structure.min.css" rel="stylesheet"/>
	<link href="resources/css/jquery-ui.theme.min.css" rel="stylesheet"/>
	
	<!-- Magnific Popup Plugin -->
	<link href="resources/css/magnific-popup.css" rel="stylesheet"/>

    <!-- Custom CSS -->
    <link href="resources/css/index.css" rel="stylesheet"/>

    <!-- Custom Fonts -->
    <link href="resources/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
    <link href="https://fonts.googleapis.com/css?family=Montserrat:400,700" rel="stylesheet" type="text/css"/>
    <link href='https://fonts.googleapis.com/css?family=Kaushan+Script' rel='stylesheet' type='text/css'/>
    <link href='https://fonts.googleapis.com/css?family=Droid+Serif:400,700,400italic,700italic' rel='stylesheet' type='text/css'/>
    <link href='https://fonts.googleapis.com/css?family=Roboto+Slab:400,100,300,700' rel='stylesheet' type='text/css'/>

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<body id="page-top" class="index">

    <!-- Navigation -->
    <nav class="navbar navbar-default navbar-fixed-top">
        <div class="container">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header page-scroll">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand page-scroll" href="#page-top">Credit4U Demolab</a>
            </div>

            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav navbar-right">
                    <li class="hidden">
                        <a href="#page-top"></a>
                    </li>
                    <li>
                        <a class="page-scroll" href="#services">Services</a>
                    </li>
                    <li>
                        <a class="page-scroll" href="#livedemo">Live Demo</a>
                    </li>
                    <li>
                        <a class="page-scroll" href="#contact">Contact</a>
                    </li>
                    <li>
                        <a id="sign-in-link">Sign In</a>
                    </li>
                    <li id="account-dropdown" class="dropdown" style="display: none;">
                        <a id="account-link" href="#" data-toggle="dropdown" class="dropdown-toggle"><span id="account-name">Account Name</span> <b class="caret"></b></a>
                        <ul class="dropdown-menu">
                            <li><a id="account-logout">Logout</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container-fluid -->
    </nav>

    <!-- Header -->
    <header>
        <div class="container">
            <div class="intro-text">
            
            	<!-- Set the welcome title to include the organisation name -->
                <div class="intro-lead-in">Welcome to the Credit4U Demo Lab</div>
                <div class="intro-heading">It's Nice To Meet You</div>
                <a id="more-information" href="http://www.youtube.com/watch?v=whEDsAZ6a7s" class="page-scroll btn btn-xl">Tell Me More</a>
                
            </div>
        </div>
    </header>
    
    <!-- Popover -->
    
    <div style="display: none">
	    
	  	<div id="sign-in-popover">
	    	<form id="form-sign-in" class="authentication">
				<div class="section-heading title">
					<h3>Sign In</h3>
				</div> 
				<div class="form-main">
					<div class="form-group">
						<div class="un-wrap">
							<i class="fa fa-envelope"></i>
							<input id="sign-in-email" type="email" class="form-control" placeholder="Email" required/>
							<p class="help-block text-danger"></p>
						</div>
						<div class="pw-wrap">
							<i class="fa fa-lock"></i>
							<input id="sign-in-password" type="password" class="form-control" placeholder="Password" required/>
							<p class="help-block text-danger"></p>
						</div>
						<div class="row top-buffer">
							<div class="col-md-6">
								<a class="page-scroll need-account" href="#livedemo">Need an Account?</a>
							</div>
							<div class="col-md-6">
						  		<button class="btn btn-block signin" onclick="signIn(event, this)">Sign In</button>
						  		<br/>
							</div>
							<br/>
						</div>
						<div class="row">
							<div class="col-md-10 col-md-offset-1">
								<div id="status"></div>									
							</div>
						</div>
					</div>
				</div>		
			</form>
	  	</div>
	  	
    </div>
    
    <!-- Authentication -->
    
    <div id="authentication-forms" style="display: none">    				
		<div class="row">
	    	<div class="col-md-4 col-md-offset-1">
	    		<div id="registration">
	  				<form id="form-registration" class="authentication"> 
						<div class="section-heading title">
							<h3>Registration</h3>
						</div> 
						<div class="form-main">
							<div class="form-group">
								<div class="un-wrap">
									<i class="fa fa-envelope"></i>
									<input id="register-email" type="email" class="form-control" placeholder="Email" required/>
									<p class="help-block text-danger"></p>
								</div>
								<div class="un-wrap">
									<i class="fa fa-user"></i>
									<input id="register-organisation" type="text" class="form-control" placeholder="Organisation" required/>
									<p class="help-block text-danger"></p>
								</div>
								<div class="pw-wrap">
									<i class="fa fa-lock"></i>
									<input id="register-password" type="password" class="form-control" placeholder="Password" required/>
								<p class="help-block text-danger"></p>
								</div>
								<div class="pw-wrap">
									<i class="fa fa-lock"></i>
									<input id="register-retype-password" type="password" class="form-control" placeholder="Repeat Password" required data-match/>
									<p class="help-block text-danger"></p>
								</div>
								<div class="un-wrap">
									<div id="captcha-goes-here"></div>
									<br/>
								</div>
								<div class="row top-buffer">
									<div class="col-md-6 col-md-offset-6">
						   				<button class="btn btn-block signin" onClick="register(event, this)">Register</button>
						   				<br/>
									</div>
								</div>
								<div class="row">
									<div class="col-md-10 col-md-offset-1">
										<div id="status"></div>									
									</div>
								</div>
							</div>
						</div>		
					</form>
	  			</div>
	    	</div>
	        <div class="col-md-1 vertical-divider-right"></div>
	        <div class="col-md-1 vertical-divider-left"></div>
			<div class="col-md-4">
	  			<div id="sign-in">
	    			<form id="form-sign-in" class="authentication">
						<div class="section-heading title">
							<h3>Sign In</h3>
						</div> 
						<div class="form-main">
							<div class="form-group">
								<div class="un-wrap">
									<i class="fa fa-envelope"></i>
									<input id="sign-in-email" type="email" class="form-control" placeholder="Email" required/>
									<p class="help-block text-danger"></p>
								</div>
								<div class="pw-wrap">
									<i class="fa fa-lock"></i>
									<input id="sign-in-password" type="password" class="form-control" placeholder="Password" required/>
									<p class="help-block text-danger"></p>
								</div>
								<div class="row top-buffer">
									<div class="col-md-6 col-md-offset-6">
						  				<button class="btn btn-block signin" onClick="signIn(event, this)">Sign In</button>
						  				<br/>
									</div>
								</div>
								<div class="row">
									<div class="col-md-10 col-md-offset-1">
										<div id="status"></div>									
									</div>
								</div>
							</div>
						</div>		
					</form>
	  			</div>
			</div>
		</div>			
    </div>

    <!-- Services Section -->
    <section id="services">
        <div class="container">
            <div class="row">
                <div class="col-lg-12 text-center">
                    <h2 class="section-heading">Services</h2>
                    <h3 class="section-subheading text-muted">Credit4U provides many mobile orientated services.</h3>
                </div>
            </div>
            <div class="row text-center">
                <div class="col-md-4">
                    <a class="popup-with-zoom-anim" href="#crshr-popup">
                    	<span class="fa-stack fa-4x">
                        	<i class="fa fa-circle fa-stack-2x text-primary"></i>
                        	<i class="fa fa-credit-card fa-stack-1x fa-inverse"></i>
                    	</span>
                    </a>
                    <h4 class="service-heading">Credit Sharing</h4>
                    <p class="text-muted">Share airtime, data, sms's as well as mms's with family members and friends.</p>
                    
                    <div id="crshr-popup" class="popup-dialog zoom-anim-dialog mfp-hide">
						<h3>Credit Sharing</h3>
						<br/>
						<p>Credit Sharing provides shared accounts for families and supports Bring Your Own Device (BYOD) strategies for small businesses, where costs can 
						be shared from one main account.<br/><br/>
						Various user defined restrictions for destination and time of use can be set up to manage usage.</p>
					</div>
                    
                </div>
                <div class="col-md-4">
                    <a class="popup-with-zoom-anim" href="#autoxfr-popup">
                    	<span class="fa-stack fa-4x">
                        	<i class="fa fa-circle fa-stack-2x text-primary"></i>
                        	<i class="fa fa-exchange fa-stack-1x fa-inverse"></i>
                    	</span>
                    </a>
                    <h4 class="service-heading">Automatic Credit Transfer</h4>
                    <p class="text-muted">Automatically transfer credit between your friends and family members.</p>
                    
                    <div id="autoxfr-popup" class="popup-dialog zoom-anim-dialog mfp-hide">
						<h3>Automatic Credit Transfer</h3>
						<br/>
						<div class="row">
						
							<div class="col-md-3">
							
								<span class="fa-stack fa-4x">
                        			<i class="fa fa-circle fa-stack-2x text-primary"></i>
                        			<i class="fa fa-history fa-stack-1x fa-inverse"></i>
                    			</span>
							
							</div>
						
							<div class="col-md-9">
							
								<h4>Periodic Transfers:</h4>
								<p>ACT is ideal for families. Subscribers set up a schedule of recurring transfers to a defined group of beneficiaries, 
								and ACT performs the credit transfers for Voice, Data or SMS automatically.</p>
								<br/>
							
							</div>
						
						</div>
						<div class="row">
						
							<div class="col-md-3">
							
								<span class="fa-stack fa-4x">
                        			<i class="fa fa-circle fa-stack-2x text-primary"></i>
                        			<i class="fa fa-tasks fa-stack-1x fa-inverse"></i>
                    			</span>
							
							</div>
						
							<div class="col-md-9">
							
								<h4>Account Balance Based Transfers:</h4>
								<p>Subscribers can set a minimum balance threshold for their beneficiaries. When this minimum balance is reached, the 
								beneficiaries will receive an automatic account top-up to the preset value. Subscribers can limit the total amount of transfers per period.</p>
							
							</div>
						
						</div>
					</div>
                </div>
                <div class="col-md-4">
                    <a class="popup-with-zoom-anim" href="#re2u-popup">
                    	<span class="fa-stack fa-4x">
                        	<i class="fa fa-circle fa-stack-2x text-primary"></i>
                        	<i class="fa fa-users fa-stack-1x fa-inverse"></i>
                    	</span>
                    </a>
                    <h4 class="service-heading">Reseller2U</h4>
                    <p class="text-muted">Coming Soon.</p>
                    
                    <div id="re2u-popup" class="popup-dialog zoom-anim-dialog mfp-hide">
						<h3>Reseller Credit Transfer</h3>
						<br/>
						<p>Using Reseller2U enables an operator's regular subscribers to resell credit to other subscribers on the network. This offers reduced voucher 
						distribution cost and facilitates credit distribution. Subscribers can earn commission when transferring credit.</p>
						<p>Any subscriber can become a reseller by:</p>
						<ol>
							
							<li>Registering to resell network credit.</li>
							<li>Purchasing mobile credit stock.</li>
							<li>Selling and transferring to a buyer.</li>
							<li>Earning airtime as a commission on the transfer.</li>
							<li>Re-selling the airtime commission.</li>
							
						</ol>
					</div>
                </div>
            </div>
        </div>
    </section>

    <!-- livedemo Grid Section -->
    <section id="livedemo" class="bg-light-gray">
        <div class="container">
            <div class="row">
                <div class="col-lg-12 text-center">
                    <h2 class="section-heading">Live Demo</h2>
                    <h3 class="section-subheading text-muted">Have a look at what Credit4U has to offer.</h3>
                </div>
            </div>
            
            <!-- Check whether there is a valid session open -->
            <c:choose>
            
            	<c:when test="${hasSession}">
            		
            		<div class="col-lg-12 col-md-12 col-xs-12">
            		
            			<div id="livedemo-items" class="row"></div>
            		
            		</div>
            
            	</c:when>
            	
            	<c:otherwise>
            	
            		<!-- Else the session is not valid -->
            	
            		<div class="row">
            		
            			<div class="col-lg-12 text-center">
            			
            				<h4 class="section-subheading">There currently is no session available at this time.</h4>
            				<h5 class="section-subheading">Please try again later.</h5>
            				
            			</div>
            		
            		</div>

				</c:otherwise>
            
            </c:choose>
        </div>
    </section>
    
    <!-- Contact Section -->
    <section id="contact">
        <div class="container">
            <div class="row">
                <div class="col-lg-12 text-center">
                    <h2 class="section-heading">Contact Us</h2>
                    <h3 class="section-subheading text-muted">Any further questions, please feel free to contact us.</h3>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <form name="sentMessage" id="contactForm">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="form-group">
                                    <input id="contact-name" type="text" class="form-control" placeholder="Name" required/>
                                    <p class="help-block text-danger"></p>
                                </div>
                                <div class="form-group">
                                    <input id="contact-email" type="email" class="form-control" placeholder="Email" required/>
                                    <p class="help-block text-danger"></p>
                                </div>
                                <div class="form-group">
                                    <input id="contact-phone" type="tel" class="form-control" placeholder="Phone Number" required/>
                                    <p class="help-block text-danger"></p>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-group">
                                    <textarea id="contact-message" class="form-control" placeholder="Message" required></textarea>
                                    <p class="help-block text-danger"></p>
                                </div>
                            </div>
                            <div class="clearfix"></div>
                            <div class="col-lg-12 text-center">
                                <button class="btn btn-xl" onClick="contact(event, this)">Send Message</button>
                            </div>
                        </div>
                        <div class="row">
							<div class="col-md-12">
								<br/>
								<div id="status"></div>									
							</div>
						</div>
                    </form>
                </div>
            </div>
        </div>
    </section>

    <footer>
        <div class="container">
            <div class="row">
                <div class="col-md-4">
                    <span class="copyright">Copyright &copy; Credit4U Demo Lab 2015</span>
                </div>
                <div class="col-md-4">
                    <ul class="list-inline social-buttons">
                        <li><a href="https://twitter.com/ConcurrentSys"><i class="fa fa-twitter"></i></a>
                        </li>
                        <!-- <li><a href="#"><i class="fa fa-facebook"></i></a>
                        </li> -->
                        <li><a href="https://www.linkedin.com/company/concurrent-systems"><i class="fa fa-linkedin"></i></a>
                        </li>
                    </ul>
                </div>
                <div class="col-md-4">
                    <ul class="list-inline quicklinks">
                        <li><a href="#privacy-policy-modal" data-toggle="modal">Privacy Policy</a>
                        </li>
                        <li><a href="#terms-of-use-modal" data-toggle="modal">Terms of Use</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </footer>
    
    <a class="logo" href="http://www.concurrent.co.za" target="_blank"></a>
    <div id="warning_message"></div>
    
    <!-- Privacy Policy Modal -->
    <div class="livedemo-modal modal fade" id="privacy-policy-modal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-content">
        
        	<div class="close-modal" data-dismiss="modal">
        		<button class="btn btn-default btn-large close-modal-button">X</button>
        	</div>
        
        	<div class="container">
                <div class="row">
                    <div class="col-lg-8 col-lg-offset-2">
                        <div class="modal-body">
                            <%@ include file="/WEB-INF/pages/privacypolicy.html" %>
                            <button type="button" class="btn btn-primary" data-dismiss="modal"><i class="fa fa-times"></i> Close Policy</button>
                        </div>
                    </div>
                </div>
            </div>
        
        </div>
    </div>
    
    <!-- Terms of Use Modal -->
    <div class="livedemo-modal modal fade" id="terms-of-use-modal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-content">
        
        	<div class="close-modal" data-dismiss="modal">
        		<button class="btn btn-default btn-large close-modal-button">X</button>
        	</div>
        
        	<div class="container">
                <div class="row">
                    <div class="col-lg-8 col-lg-offset-2">
                        <div class="modal-body">
                            <%@ include file="/WEB-INF/pages/termsofuse.html" %>
                            <button type="button" class="btn btn-primary" data-dismiss="modal" style="text-align: center"><i class="fa fa-times"></i> Close Policy</button>
                        </div>
                    </div>
                </div>
            </div>
        
        </div>
    </div>

    <!-- jQuery -->
    <script src="resources/js/jquery/jquery-1.9.min.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="resources/js/bootstrap/bootstrap.min.js"></script>

    <!-- Plugin JavaScript -->
    <script src="resources/js/jquery/jquery.easing.min.js"></script>
    <script src="resources/js/classie.js"></script>
    <script src="resources/js/bootstrap/cbpAnimatedHeader.min.js"></script>

	<!-- Toast Plugin -->
	<script src="resources/js/toastr.js"></script>
	
	<!-- JQuery Terminal Plugin -->
	<script src="resources/js/jquery/jquery.terminal-min.js"></script>
	<script src="resources/js/jquery/jquery-ui.min.js"></script>

	<!-- Magnific Popup Plugin -->
	<script src="resources/js/jquery/jquery.magnific-popup.min.js"></script>
	
	<!-- Bounce.js Plugin -->
	<script src="resources/js/bounce.min.js"></script>
	
	<!-- Recaptcha Plugin -->
	<script src="https://www.google.com/recaptcha/api.js?onload=onloadCallback&render=explicit"
        async defer>
    </script>

    <!-- Custom Theme JavaScript -->
    <script src="resources/js/index.js"></script>

</body>

</html>
