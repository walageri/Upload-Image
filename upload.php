<?PHP
if(isset($_POST['image'])){
	$now = DateTime::createFromFormat('U.u',microtime(true));
	$id = $now->format('tmdHisu');

	$uplaod_folder ="upload";
	$path = "$upload_folder/$id.jpeg";
	$image = $_POST['image'];
	if(file_put_contents($path, base64_decode($image)) !=false){
		echo "uploaded_success"
		exit;
	}else{
		echo "upload_failed";
		exit
	}
}else{
	echo "image_not_in";
	exit;
}
?>