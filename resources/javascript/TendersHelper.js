var TendersHelper = {};

TendersHelper.subscribe = function(message, caseId, loadingMessage, processInstanceId) {
	showLoadingMessage(message);
	TendersSubscriber.subscribe(caseId, processInstanceId, {
		callback: function(result) {
			closeAllLoadingMessages();
			
			if (result == null) {
				return false;
			}
			
			if (result.value == null || result.value == '') {
				humanMsg.displayMsg(result.id, null);
				return false;
			}
			
			humanMsg.displayMsg(result.id, {
				timeout: 2000,
				callback: function() {
					showLoadingMessage(loadingMessage);
					window.location.href = result.value;
				}
			});
		}
	});
}