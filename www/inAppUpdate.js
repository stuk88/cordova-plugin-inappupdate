window.inappupdate = function(task,args,success,error) {
	cordova.exec(success, error, "inAppUpdate", task, args);
};
