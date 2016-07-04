define(["require", "exports", '../js/helper', '../js/lib/html2canvas.js'], function (require, exports, helper_1) {
    "use strict";
    var myThis;
    var ScreenshotView = (function () {
        function ScreenshotView(screenshotMechanism, screenshotPreviewElement, screenshotCaptureButton, elementToCapture, elementsToHide) {
            myThis = this;
            this.screenshotMechanism = screenshotMechanism;
            this.screenshotPreviewElement = screenshotPreviewElement;
            this.screenshotCaptureButton = screenshotCaptureButton;
            this.elementToCapture = elementToCapture;
            this.elementsToHide = elementsToHide;
            this.canvasState = null;
            this.addCaptureEventToButton();
        }
        ScreenshotView.prototype.generateScreenshot = function () {
            this.hideElements();
            html2canvas(this.elementToCapture, {
                onrendered: function (canvas) {
                    myThis.showElements();
                    myThis.screenshotPreviewElement.empty().append(canvas);
                    myThis.screenshotPreviewElement.show();
                    var windowRatio = myThis.elementToCapture.width() / myThis.elementToCapture.height();
                    var data = canvas.toDataURL();
                    myThis.context = canvas.getContext("2d");
                    myThis.canvasWidth = myThis.screenshotPreviewElement.width();
                    myThis.canvasHeight = myThis.screenshotPreviewElement.width() / windowRatio;
                    $(canvas).prop('width', myThis.canvasWidth);
                    $(canvas).prop('height', myThis.canvasHeight);
                    var img = new Image();
                    img.onload = function () {
                        myThis.context.drawImage(img, 0, 0, img.width, img.height, 0, 0, canvas.width, canvas.height);
                    };
                    img.src = data;
                    myThis.canvasState = new Image();
                    myThis.canvasState.src = img.src;
                    myThis.screenshotCanvas = canvas;
                    myThis.initDrawing();
                }
            });
        };
        ScreenshotView.prototype.getScreenshotAsBinary = function () {
            var dataURL = this.screenshotCanvas.toDataURL("image/png");
            return helper_1.Helper.dataURItoBlob(dataURL);
        };
        ScreenshotView.prototype.addCaptureEventToButton = function () {
            var myThis = this;
            this.screenshotCaptureButton.on('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                myThis.generateScreenshot();
            });
        };
        ScreenshotView.prototype.hideElements = function () {
            for (var _i = 0, _a = this.elementsToHide; _i < _a.length; _i++) {
                var elementToHide = _a[_i];
                elementToHide.hide();
            }
        };
        ScreenshotView.prototype.showElements = function () {
            for (var _i = 0, _a = this.elementsToHide; _i < _a.length; _i++) {
                var elementToHide = _a[_i];
                elementToHide.show();
            }
        };
        ScreenshotView.prototype.initDrawing = function () {
            var context = this.screenshotCanvas.getContext('2d');
            this.drawingMode = 'fillRect';
            this.isPainting = false;
            $(this.screenshotCanvas).on('mousedown touchstart', function (event) {
                var parentOffset = $(this).parent().offset();
                myThis.startX = event.pageX - parentOffset.left;
                myThis.startY = event.pageY - parentOffset.top;
                myThis.isPainting = true;
            }).on('mousemove touchmove', function (event) {
                if (myThis.isPainting) {
                    context.clearRect(0, 0, myThis.canvasWidth, myThis.canvasHeight);
                    context.drawImage(myThis.canvasState, 0, 0, myThis.canvasState.width, myThis.canvasState.height, 0, 0, myThis.screenshotCanvas.width, myThis.screenshotCanvas.height);
                    var parentOffset = $(this).parent().offset();
                    var currentX = event.pageX - parentOffset.left;
                    var currentY = event.pageY - parentOffset.top;
                    var width = currentX - myThis.startX;
                    var height = currentY - myThis.startY;
                    if (myThis.drawingMode === 'rect') {
                        context.rect(myThis.startX, myThis.startY, width, height);
                    }
                    else if (myThis.drawingMode === 'fillRect') {
                        context.fillRect(myThis.startX, myThis.startY, width, height);
                    }
                    context.stroke();
                }
            }).on('mouseup touchend', function (event) {
                myThis.isPainting = false;
                var parentOffset = $(this).parent().offset();
                var endX = event.pageX - parentOffset.left;
                var endY = event.pageY - parentOffset.top;
                var width = endX - myThis.startX;
                var height = endY - myThis.startY;
                if (myThis.drawingMode === 'rect') {
                    context.rect(myThis.startX, myThis.startY, width, height);
                }
                else if (myThis.drawingMode === 'fillRect') {
                    context.fillRect(myThis.startX, myThis.startY, width, height);
                }
                context.stroke();
                myThis.canvasState.src = myThis.screenshotCanvas.toDataURL("image/jpeg");
            }).on('mouseleave touchleave', function () {
                myThis.isPainting = false;
            });
        };
        return ScreenshotView;
    }());
    exports.ScreenshotView = ScreenshotView;
});
