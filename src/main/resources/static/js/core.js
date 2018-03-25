function toggleDimmer() {
    $('#dimmer').toggleClass('active');
}

function registerOnSubmitHandler(formId) {
    $('#' + formId).on('submit', toggleDimmer);
}

function showErrorModal(error) {
    $('.ui.modal .content p').text(error);
    $('.ui.modal').modal('show');
}