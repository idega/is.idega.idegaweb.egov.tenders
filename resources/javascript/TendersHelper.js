var TendersHelper = {};

TendersHelper.subscribe = function(message, caseId, processInstanceId) {
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
				timeout: 3000,
				callback: function() {
					window.location.href = result.value;
				}
			});
		}
	});
}