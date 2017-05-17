$(function(){
	
	// swal('Under Maintenance','Please try again later', 'warning')

    function getUerDetails() {
	    $.ajax({
	        type : 'GET',
	        url: '/nameRecordingService/svc/student/userDetail',
	        cache: false,
	        success : function(data) {
	        	// console.log(data);
	        	USER = data;  // global	

        		$('p.student').html(data.firstName+' '+data.lastName+',');
        		$('span.recDateTime').html(data.recordingLastModifiedDate);
        		$('audio source').attr('src', data.nameRecordingUrl+'?'+(new Date).getTime())[0];
        		$('audio').load();
        		$('#classcards-link').attr('href', '/classcards/detail.do?prsnId='+data.personId)

	        	if(data.recordingAvailable && data.recordingAvailable == true) {
	        		$('.completed').show();
	        	} else {
	        		$('.incomplete').show()
	        		chooseRecordingMethod();
	        	}       

	        	$('#rec-again').on('click', function(e){
	        		e.preventDefault();
	        		$('.completed').hide();
	        		chooseRecordingMethod();
	        	});    

	        },
	        error: function(jqXHR, textStatus, errorThrown) {

                if(textStatus === 'parsererror'){
                        
                    var redrectUrl = '/nameRecordingService/svc/report/list';

                    swal({
                        title: "Authentication Error",
                        html: "You're not logged in or your session has expired.<br>Redirecting to the login page in 3 seconds...",
                        type: "error",
                        timer: 3000,
                    }).then(function() {
                        // handles OK
                        window.location = redrectUrl;
                    }).catch(function(){
                        // handles timer promise
                        window.location = redrectUrl;
                    });
                    
                }

	        }
	    });
	}

	function hasGetUserMedia() {
	  return !!(navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
	}

	function iOS() {
	  var iDevices = ['iPad Simulator', 'iPhone Simulator', 'iPod Simulator', 'iPad', 'iPhone', 'iPod' ];
	  	if (!!navigator.platform) {
	    	while (iDevices.length) {
	      		if (navigator.platform === iDevices.pop()){ return true; }
	   	 	}
	  	}
	  	return false;
	}


	// Detect device and capabilities	

	function chooseRecordingMethod() {
		if (iOS() && !hasGetUserMedia() ) {
			console.log("IOS device without getUserMedia() support detected, using hack");
			$("#recorder-ios").show();
			iosRecord();
		} else {
			if (hasGetUserMedia()) {
			  	console.log("getUserMedia detected, using HTML5");
			  	$("#recorder-html5").show();
			  	html5Record();
			} else {
			  	console.log("No getUserMedia, using Flash");
			  	$("#recorder-flash").show();
			  	flashRecord();
			}	
		}
	}


	// ***************************************
	// iOS hack
	// ***************************************

	function iosRecord() {

		console.log("Running iosRecord function");

		$('#image_file').on('change', function(){
			fileSelected(); 
			startUploading();
		});

		// common variables
		var iBytesUploaded = 0;
		var iBytesTotal = 0;
		var iPreviousBytesLoaded = 0;
		var iMaxFilesize = 1048576; // 1MB
		var oTimer = 0;
		var sResultFileSize = '';

		function secondsToTime(secs) { // convert seconds in normal time format
		    var hr = Math.floor(secs / 3600);
		    var min = Math.floor((secs - (hr * 3600))/60);
		    var sec = Math.floor(secs - (hr * 3600) -  (min * 60));

		    if (hr < 10) {hr = "0" + hr; }
		    if (min < 10) {min = "0" + min;}
		    if (sec < 10) {sec = "0" + sec;}
		    if (hr) {hr = "00";}
		    return hr + ':' + min + ':' + sec;
		};

		function bytesToSize(bytes) {
		    var sizes = ['Bytes', 'KB', 'MB'];
		    if (bytes == 0) return 'n/a';
		    var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
		    return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + sizes[i];
		};

		function fileSelected() {

		    // get selected file element
		    var oFile = document.getElementById('image_file').files[0];

		    // filter for image files
		    var rFilter = /^(image\/bmp|image\/gif|image\/jpeg|image\/png|image\/tiff)$/i;
		    if (! rFilter.test(oFile.type)) {
		        console.log("Invalid file type");
		        return;
		    }

		    // little test for filesize
		    if (oFile.size > iMaxFilesize) {
		    	swal(
					  'Oops...',
					  'File size is too big.',
					  'error'
					)
		        return;
		    }

		    // read selected file as DataURL
		    oReader.readAsDataURL(oFile);
		}

		function startUploading() {

		    var data = new FormData();
		    data.append('file', $("#upload_form input[type='file']")[0].files[0],  USER.personId+'.mov');

      		$.ajax({
        		url :  "/nameRecordingService/svc/student/uploadForm",
		        type: 'POST',
		        data: data,
		        contentType: false,
		        processData: false,
		        success: function(data) {
		          	console.log("file uploaded successfully.");

		          	$('#recorder-ios .btn-complete, #recorder-ios #play').show();
				    $('.btn-complete').show().click(done); 
				    $('#recorder-ios .alert').hide();

				    $('#recorder-ios #play').click(function(){
				    	console.log("Playing...");
						sound = new Audio( 'https://sands.hbs.edu/audio/mbavoices/preview/' + USER.personId+'.wav?'+(new Date).getTime() );
						sound.play();
					});

		        },    
		        error: function() {
		          swal(
					  'Oops...',
					  'Cannot upload file.',
					  'error'
					)
		        }
		    });
		}
	}


	// ***************************************
	// Flash Recording
	// ***************************************

	function flashRecord() {
	    Wami.setup({
	        id: 'flash' 
	    });

	    var recording = '';
	    var recordingUrl = '';
	    var playBackUrl = '';

	    var record = $('#recorder-flash #record');
	    var play = $('#recorder-flash #play');

	    function startRecording() {
	        $('#recorder-flash #play, #recorder-flash .btn-complete').hide();

	        recording = USER.nameRecordingUrl.substring(USER.nameRecordingUrl.lastIndexOf('/')+1);
	        console.log(recording)
	        // recordingUrl = './upload.php?filename=' + recording;
	        recordingUrl = '/nameRecordingService/svc/student/uploadStream?filename='+recording;
	        Wami.startRecording(recordingUrl);
	        record
	            .html('<i class="fa fa-stop"></i><span class="hidden-sm-down">Stop</span>')
	            .unbind()
	            .click(function() {
	                stopRecording();
	            });
	    }

	    function stopRecording() {
	        Wami.stopRecording();
	        playBackUrl = 'https://sands.hbs.edu/audio/mbavoices/preview/' + USER.personId+'.wav?'+(new Date).getTime(); 
	        record
	            .html('<i class="fa fa-microphone"></i><span class="hidden-sm-down">Record</span>')
	            .unbind()
	            .click(function() {
	                startRecording();
	            });

	            setTimeout(function(){
		        	$('#recorder-flash #play').show();
		        	$('.btn-complete').show().click(done); 
		    	}, 1500);
	    }

	    function startPlaying() {
	    	console.log('Playing '+playBackUrl);
	        Wami.startPlaying(playBackUrl);
	    }

	    function stopPlaying() {
	        Wami.stopPlaying();
	        play
	            .html('<i class="fa fa-play"></i><span class="hidden-sm-down">Play</span>')
	            .unbind()
	            .click(function() {
	                startPlaying();
	            });           
	    }

	    record.click(function() {
	        startRecording();
	    });

	    play.click(function() {
	        startPlaying();
	    });
	}


	// ***************************************
	// HTML5 Recording
	// ***************************************

	function html5Record() {

		init();

		$('#startRec').click(function(){
			startRecording();
		})

		function __log(e, data) {
		    console.log(e + " " + (data || ''))
		}

	  	var audio_context;
	  	var recorder;

		function startUserMedia(stream) {
		   var input = audio_context.createMediaStreamSource(stream);
		    __log('Media stream created.');
		    
		    recorder = new Recorder(input);
		    __log('Recorder initialised.');
		}

		function startRecording() {
		    recorder && recorder.record();
		    $('#playRec, .btn-complete').hide();
		    $('#startRec').html('<i class="fa fa-stop"></i><span class="hidden-sm-down">Stop</span>')
	            .unbind()
	            .click(function() {
	                stopRecording();
	            });

		    __log('Recording...');
		}

		function stopRecording() {
		    recorder && recorder.stop();
		    $('#startRec').html('<i class="fa fa-microphone"></i><span class="hidden-sm-down">Record</span>')
	            .unbind()
	            .click(function() {
	                startRecording();
	            });

		    __log('Stopped recording.');
		    

		    uploadToServer();
		    
		    recorder.clear();
		}

		function uploadToServer() {
		    recorder && recorder.exportWAV(function(blob) {

			    var data = new FormData();
			    data.append('file', blob,  USER.personId+'.wav?');

	      		$.ajax({
	        		url :  "/nameRecordingService/svc/student/uploadForm",
			        type: 'POST',
			        data: data,
			        contentType: false,
			        processData: false,
			        success: function(data) {
			          	console.log("file uploaded successfully.");

			          	$('.btn-complete').show().click(done);
		    			$('#playRec, .btn-complete').show();

			        },    
			        error: function() {
			          swal(
						  'Oops...',
						  'Cannot upload file.',
						  'error'
						)
			        }
			    });

		    });
		}

		$(document).on('click', '#playRec', function(){
        	console.log("Playing "+USER.nameRecordingUrl);
			snd = new Audio('https://sands.hbs.edu/audio/mbavoices/preview/' + USER.personId+'.wav?'+(new Date).getTime() );
			//snd.preload = 'none';
			snd.play();
		})

	  	function init() {
	    try {
		    // webkit shim
		    window.AudioContext = window.AudioContext || window.webkitAudioContext;
		    navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
		    window.URL = window.URL || window.webkitURL;
	      	audio_context = new AudioContext;
	     	__log('Audio context set up.');
	      	__log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not present!'));
	    } catch (e) {
      		swal(
			  	'Oops...',
			  	'No web audio support in this browser.',
			  	'error'
			)
    	}
    
	    navigator.getUserMedia({audio: true}, startUserMedia, function(e) {
	      __log('No live audio input: ' + e);
	    });
	  };

	}

	function done() {
		// swal(
		//   'Thank you',
		//   'You have successfully recorded your name',
		//   'success'
		// ).then(function () {
		//   window.location.replace('/nameRecordingService/svc/student/nameRecord');
		// });

		swal({
		  title: 'Are you sure?',
		  text: "This recording will be added to your HBS Classcard.",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonText: 'Save',
		  cancelButtonText: 'Cancel',
		  confirmButtonClass: 'btn btn-success mr-2',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false
		}).then(function () {

			$.ajax({
		        type : 'POST',
		        url: '/nameRecordingService/svc/student/recordingSubmit',
		        cache: false,
		        success : function(data) {
			       	swal({
					    'title' : 'Done',
					    'html' : 'Your name has been successfully recorded.<br>Refreshing in 3 seconds...',
					    'type' : 'success',
					    'timer': 3000,
                        'showConfirmButton': false
					}).then(
					  	refresh, refresh
					);

		        },
		        error: function(jqXHR, textStatus, errorThrown) {
		        	swal({
					    'title' : 'Error',
					    'html' : 'Cannot overwrite previous recording.<br>Refreshing in 3 seconds...',
					    'type' : 'error',
					    'timer': 3000,
                        'showConfirmButton': false					    
					}).then(
					  	refresh, refresh
					);
		        }
		    });

		}, function (dismiss) {
		  // dismiss can be 'cancel', 'overlay',
		  // 'close', and 'timer'
		  if (dismiss === 'cancel' || dismiss === 'overlay' || dismiss === 'close' || dismiss === 'timer') {
		    
		    $.ajax({
		        type : 'POST',
		        url: '/nameRecordingService/svc/student/recordingCancel',
		        cache: false,
		        success : function(data) {
				    swal({
				      	'title' : 'Cancelled',
				      	'html' : 'Your original name recording has been kept.<br>Refreshing in 3 seconds...',
				      	'type' : 'info',
					    'timer': 3000,
                        'showConfirmButton': false				      	
				    }).then(
					  	refresh, refresh
					);

		        },
		        error: function(jqXHR, textStatus, errorThrown) {
		        	console.log(textStatus);
		        }
		    });

		  }
		})

		function refresh(dismiss) {
			window.location.replace('/nameRecordingService/svc/student/nameRecord');
		}
	}


	getUerDetails();
	//chooseRecordingMethod();
	

});