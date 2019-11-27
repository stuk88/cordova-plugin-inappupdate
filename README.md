# cordova-plugin-inappupdate
This plugin establishes the functionality for downloading the app in background

**For Optional Update:**
>window.inappupdate("inAppUpdate", [{
			"updateIndicator": "optional",
			"displayMessage": "An update has just been downloaded",
			"actionBtn": "Install"
		}
	], function (msg) {
	console.log(JSON.stringify(msg));
	return true;
}, function (data) {
	console.log(data);
});

**For Mandatory Update:**
>window.inappupdate("inAppUpdate", [{
			"updateIndicator": "mandatory",
			"displayMessage": "An update has just been downloaded",
			"actionBtn": "Install"
		}
	], function (msg) {
	console.log(JSON.stringify(msg));
	return true;
}, function (data) {
	console.log(data);
});
