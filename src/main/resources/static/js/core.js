var MAX_SERVER_RESPONSE_TIME_MS = 500;

function showDimmer(message) {
    $('#dimmer .text').text(message);
    $('#dimmer').addClass('active');
}

function hideDimmer() {
    $('#dimmer').removeClass('active');
    // Clear message for next reuse to start from blank.
    $('#dimmer .text').text('');
}

function registerOnSubmitHandler(formId) {
    $('#' + formId).on('submit', function() {
        // no message as submitting a form can also mean going back etc.
        showDimmer('');
    });
}

function showErrorModal(error) {
    $('.ui.modal .content p').text(error);
    $('.ui.modal').modal('show');
}

/**
 * Wraps effect in a dimmer that only shows if the network is slow to respond.
 *
 * @param dimmerMessage The message to display below the loading spinner.
 * @param f The side effecting HTTP POST/GET etc.
 */
function enhancedAjaxHandler(dimmerMessage, f) {
    var serverReplied = false;
    var timeoutTriggered = false;

    // The effect will invoke this if the server replies.
    var onSuccess = function() {
        serverReplied = true;
        if (timeoutTriggered) {
            hideDimmer();
        }
    };

    var onError = function(errorMessage) {
        showErrorModal(errorMessage);
        hideDimmer();
    };

    f(onSuccess, onError);

    setTimeout(function () {
        if (!serverReplied) {
            timeoutTriggered = true;
            showDimmer(dimmerMessage);
        }
    }, MAX_SERVER_RESPONSE_TIME_MS);
}