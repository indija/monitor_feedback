import i18n = require('i18next');


export interface JQuery {
    /**
     * Register a handler to validate a HTML element.
     *
     */
    validateCategory():JQuery;
}

export var validateCategoryPluginModule = (function($, window, document) {
    /**
     * @returns {jQuery}
     *
     */
    $.fn.validateCategory = function () {
        var mandatory = this.data('mandatory'),
            manualText = this.data('mandatory-manual-text'),
            validMandatory = true;
        var showValidationError = function (errorMessage, element) {
            element.append('<span class="feedback-form-error">' + errorMessage + '</span>');
        };

        // reset
        this.find('.feedback-form-error').remove();
        this.removeClass('invalid');

        // validate mandatory
        if(mandatory && this.find('input:checked').length === 0) {
            validMandatory = false;
        }

        // display messages
        if(!validMandatory) {
            this.addClass('invalid');
            showValidationError(manualText, this);

            var invalidElement = this;
            $('html, body').animate({
                scrollTop: invalidElement.offset().top
            }, 500);
        }

        return this;
    };
});

(function($, window, document) {
    validateCategoryPluginModule($, window, document);
})(jQuery, window, document);

requirejs.config( {
    "shim": {
        "validateCategory"  : ["jquery"]
    }
} );