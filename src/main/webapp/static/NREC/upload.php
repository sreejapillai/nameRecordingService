<?php

	if(!is_dir("recordings")){
		$res = mkdir("recordings",0777); 
	}

	// ***************************************
	// Safari & HTML5 (POST)
	// ***************************************
	if(isset($_FILES['file']) and !$_FILES['file']['error']){
	    $fname = $_FILES['file']['name'];
	    move_uploaded_file($_FILES['file']['tmp_name'], "./recordings/" . $fname);
	    if ( strpos($fname, '.mov') ) {
	    	$cmd = "/usr/local/bin/ffmpeg -y -i recordings/".$fname. " recordings/".substr($fname, 0, -3).'wav'. " && rm recordings/".$fname;
		 	exec($cmd);
	    }
	}

	// ***************************************
	// Flash Recording (GET)
	// ***************************************
	parse_str($_SERVER['QUERY_STRING'], $params);
	if(isset($params['filename'])) {
		$file = $params['filename'];
		// save the recorded audio to that file
		$content = file_get_contents('php://input');
		$fh = fopen('recordings/'.$file, 'w') or die("can't open file");
		fwrite($fh, $content);
		fclose($fh);
	}


?>
