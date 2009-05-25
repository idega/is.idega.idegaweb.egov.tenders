var TendersHelper = {};

TendersHelper.subscribe = function(message, caseId, processInstanceId) {
	showLoadingMessage(message);
	TendersSubscriber.subscribe(caseId, processInstanceId, {
		callback: function(uri) {
			closeAllLoadingMessages();
			
			if (uri == null) {
				return false;
			}
			
			/*if (uri.indexOf('/pages/') != 0) {
				humanMsg.displayMsg(uri, null);
				return false;
			}*/
			
			window.location.href = uri;
		}
	});
}