import {ScreenshotViewDrawing} from './screenshot_view_drawing';
import {DataHelper} from '../../js/helpers/data_helper';
import '../../js/lib/html2canvas.js';
import {Mechanism} from '../../models/mechanisms/mechanism';
import 'fabric';

const freehandDrawingMode:string = 'freehandDrawingMode';
const rectDrawingMode:string = 'rectDrawingMode';
const fillRectDrawingMode:string = 'fillRectDrawingMode';
const circleDrawingMode:string = 'circleDrawingMode';
const arrowDrawingMode:string = 'arrowDrawingMode';
const croppingMode:string = 'croppingMode';
const stickingMode:string = 'stickingMode';
const textMode:string = 'textMode';
const textMode2:string = 'textMode2';
const black:string = "#000000";
const defaultColor:string = black;
const canvasId:string = 'screenshotCanvas';


export class ScreenshotView {
    screenshotMechanism:Mechanism;
    screenshotPreviewElement:JQuery;
    screenshotCaptureButton:JQuery;
    elementToCapture:JQuery;
    elementsToHide:[JQuery];
    screenshotCanvas:any;
    context:any;
    startX:number;
    startY:number;
    drawingMode:string;
    canvasState:HTMLImageElement;
    canvasStates:any;
    canvasWidth:number;
    canvasHeight:number;
    screenshotViewDrawing:ScreenshotViewDrawing;
    canvasOriginalWidth:number;
    canvasOriginalHeight:number;
    container:JQuery;
    distPath:string;
    fabricCanvas;
    croppingIsActive:boolean;


    constructor(screenshotMechanism:Mechanism, screenshotPreviewElement:JQuery, screenshotCaptureButton:JQuery,
                elementToCapture:JQuery, container:JQuery, distPath:string, elementsToHide?:any) {
        this.screenshotMechanism = screenshotMechanism;
        this.screenshotPreviewElement = screenshotPreviewElement;
        this.screenshotCaptureButton = screenshotCaptureButton;
        this.elementToCapture = elementToCapture;
        this.container = container;
        this.elementsToHide = elementsToHide;
        this.canvasState = null;
        this.canvasStates = [];
        this.distPath = distPath;
        this.screenshotViewDrawing = new ScreenshotViewDrawing();
        this.addCaptureEventToButton();
        this.croppingIsActive = false;
    }

    checkAutoTake() {
        if (this.screenshotMechanism.getParameterValue('autoTake')) {
            this.generateScreenshot();
        }
    }

    generateScreenshot() {
        this.hideElements();
        var myThis = this;

        html2canvas(this.elementToCapture, {
            onrendered: function (canvas) {
                myThis.showElements();
                myThis.screenshotPreviewElement.empty().append(canvas);
                myThis.screenshotPreviewElement.show();
                jQuery('.screenshot-preview canvas').attr('id', canvasId);

                var windowRatio = myThis.elementToCapture.width() / myThis.elementToCapture.height();

                // save the canvas content as imageURL
                var data = canvas.toDataURL("image/png");
                myThis.context = canvas.getContext("2d");
                myThis.canvasOriginalWidth = canvas.width;
                myThis.canvasOriginalHeight = canvas.height;

                myThis.canvasWidth = myThis.screenshotPreviewElement.width();
                myThis.canvasHeight = myThis.screenshotPreviewElement.width() / windowRatio;

                jQuery(canvas).prop('width', myThis.canvasWidth);
                jQuery(canvas).prop('height', myThis.canvasHeight);

                var img = new Image();
                img.crossOrigin = "Anonymous";
                myThis.canvasState = img;
                myThis.screenshotCanvas = canvas;
                img.src = data;
                img.onload = function () {
                    myThis.context.drawImage(img, 0, 0, img.width, img.height, 0, 0, canvas.width, canvas.height);
                    myThis.initFabric(img, canvas);
                    myThis.initSVGStickers();
                    myThis.initScreenshotOperations();
                };

                let screenshotCaptureButtonActiveText = myThis.screenshotCaptureButton.data('active-text');
                myThis.screenshotCaptureButton.text(screenshotCaptureButtonActiveText);
            }
        });
    }

    initFabric(img, canvas) {
        var myThis = this;
        this.fabricCanvas = new fabric.Canvas(canvasId);
        var pageScreenshotCanvas = new fabric.Image(img, {width: canvas.width, height: canvas.height});

        pageScreenshotCanvas.set('selectable', false);
        pageScreenshotCanvas.set('hoverCursor', 'default');
        this.fabricCanvas.add(pageScreenshotCanvas);

        fabric.Object.prototype.set({
            transparentCorners: true,
            borderColor: '#000000',
            cornerColor: '#545454',
            cornerSize: 9,
            padding: 1
        });

        var selectedObjectControls = jQuery('#screenshotMechanism' + myThis.screenshotMechanism.id + ' .selectedObjectControls');
        selectedObjectControls.hide();

        myThis.fabricCanvas.on('object:selected', function (e) {
            var selectedObject = e.target;

            if (selectedObject.get('type') !== 'path-group') {
                var currentObjectColor = selectedObject.getFill();
            } else {
                for (var path of selectedObject.paths) {
                    if (path.getFill() != "") {
                        var currentObjectColor = path.getFill();
                        break;
                    }
                }
            }

            selectedObjectControls.show();
            selectedObjectControls.find('.delete').off().on('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                if (selectedObject) {
                    selectedObject.remove();
                }
            });

            selectedObjectControls.find('a.color').css('color', currentObjectColor);
            selectedObjectControls.find('a.color').spectrum({
                color: defaultColor,
                showPaletteOnly: true,
                togglePaletteOnly: true,
                togglePaletteMoreText: 'more',
                togglePaletteLessText: 'less',
                palette: [
                    ["#000", "#444", "#666", "#999", "#ccc", "#eee", "#f3f3f3", "#fff"],
                    ["#f00", "#f90", "#ff0", "#0f0", "#0ff", "#00f", "#90f", "#f0f"],
                    ["#f4cccc", "#fce5cd", "#fff2cc", "#d9ead3", "#d0e0e3", "#cfe2f3", "#d9d2e9", "#ead1dc"],
                    ["#ea9999", "#f9cb9c", "#ffe599", "#b6d7a8", "#a2c4c9", "#9fc5e8", "#b4a7d6", "#d5a6bd"],
                    ["#e06666", "#f6b26b", "#ffd966", "#93c47d", "#76a5af", "#6fa8dc", "#8e7cc3", "#c27ba0"],
                    ["#c00", "#e69138", "#f1c232", "#6aa84f", "#45818e", "#3d85c6", "#674ea7", "#a64d79"],
                    ["#900", "#b45f06", "#bf9000", "#38761d", "#134f5c", "#0b5394", "#351c75", "#741b47"],
                    ["#600", "#783f04", "#7f6000", "#274e13", "#0c343d", "#073763", "#20124d", "#4c1130"]
                ],
                change: function (color) {
                    var color = color.toHexString();
                    jQuery(this).css('color', color);

                    if (selectedObject.get('type') !== 'path-group') {
                        selectedObject.setFill(color);
                    } else {
                        for (var path of selectedObject.paths) {
                            if (path.getFill() != "") {
                                path.setFill(color);
                            }
                        }
                    }
                    myThis.fabricCanvas.renderAll();
                }
            });
        });

        myThis.fabricCanvas.on('selection:cleared', function () {
            var selectedObjectControls = jQuery('#screenshotMechanism' + myThis.screenshotMechanism.id + ' .selectedObjectControls');
            selectedObjectControls.hide();
            selectedObjectControls.find('.delete').off();
            selectedObjectControls.find('.color').off();
        });
    }

    initCrop() {
        var myThis = this;
        var pos = [0, 0];
        var canvasBoundingRect = document.getElementById(canvasId).getBoundingClientRect();
        pos[0] = canvasBoundingRect.left;
        pos[1] = jQuery(myThis.screenshotCanvas).parent().offset().top;

        var mousex = 0;
        var mousey = 0;
        var crop = false;

        var croppingRect = new fabric.Rect({
            fill: 'transparent',
            originX: 'left',
            originY: 'top',
            stroke: '#333',
            strokeDashArray: [3, 3],
            opacity: 1,
            width: 1,
            height: 1
        });

        this.croppingIsActive = true;

        croppingRect.visible = false;
        this.fabricCanvas.add(croppingRect);

        this.fabricCanvas.on("mouse:down", function (event) {
            if (!myThis.croppingIsActive) {
                return;
            }
            croppingRect.left = event.e.pageX - pos[0];
            croppingRect.top = event.e.pageY - pos[1];
            //el.selectable = false;
            croppingRect.visible = true;
            mousex = event.e.pageX;
            mousey = event.e.pageY;
            crop = true;
            myThis.fabricCanvas.bringToFront(croppingRect);
        });

        this.fabricCanvas.on("mouse:move", function (event) {
            if (crop && myThis.croppingIsActive) {
                if (event.e.pageX - mousex > 0) {
                    croppingRect.width = event.e.pageX - mousex;
                }

                if (event.e.pageY - mousey > 0) {
                    croppingRect.height = event.e.pageY - mousey;
                }
            }
            myThis.fabricCanvas.renderAll();
        });

        this.fabricCanvas.on("mouse:up", function (event) {
            crop = false;
        });

        this.container.find('.screenshot-crop-cancel').show().on('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            myThis.croppingIsActive = false;
            jQuery(this).hide();
            jQuery('.screenshot-crop-confirm').hide();
            croppingRect.remove();
            myThis.fabricCanvas.renderAll();
        });

        this.container.find('.screenshot-crop-confirm').show().on('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            myThis.cropTheCanvas(croppingRect);
            myThis.croppingIsActive = false;
            jQuery(this).hide();
            jQuery('.screenshot-crop-cancel').hide();
            croppingRect.remove();
            myThis.fabricCanvas.renderAll();
        });
    }

    cropTheCanvas(croppingRect) {
        this.updateCanvasState();
        this.fabricCanvas.clipTo = function (ctx) {
            ctx.rect(croppingRect.left, croppingRect.top, croppingRect.width, croppingRect.height);
        };
        this.fabricCanvas.renderAll();
        this.container.find('.screenshot-draw-undo').show();
    }

    addTextAnnotation(left, top) {
        var text = new fabric.IText('Your text', {left: left, top: top, fontFamily: 'arial black'});
        this.fabricCanvas.add(text);
    }

    initSVGStickers() {
        var myThis = this;
        myThis.container.find('.svg-sticker-source').draggable({
            cursor: "crosshair",
            revert: "invalid",
            helper: "clone",
            zIndex: 5000,
            drag: function (event:Event, ui) {
                myThis.screenshotPreviewElement.css('border-style', 'dashed');
            },
            stop: function (event:DragEvent, ui) {
                myThis.screenshotPreviewElement.css('border-style', 'solid');
            }
        }).on('mouseover mouseenter', function () {
            myThis.screenshotPreviewElement.css('border-style', 'dashed');
        }).on('mouseleave', function () {
            myThis.screenshotPreviewElement.css('border-style', 'solid');
        });

        myThis.screenshotPreviewElement.droppable({
            drop: function (event:DragEvent, ui) {
                var sticker = $(ui.helper);

                var offsetY = event.pageY - $(this).offset().top;
                var offsetX = event.pageX - $(this).offset().left;

                offsetY -= 12;
                offsetX -= 12;

                if (sticker.hasClass('text')) {
                    myThis.addTextAnnotation(offsetX, offsetY);
                } else {
                    fabric.loadSVGFromURL(sticker.attr('src'), function (objects, options) {
                        var obj = fabric.util.groupSVGElements(objects, options);
                        obj.set('left', offsetX);
                        obj.set('top', offsetY);
                        myThis.fabricCanvas.add(obj).renderAll();
                    });
                }
            }
        });
    }

    getScreenshotAsBinary() {
        if (this.screenshotCanvas) {
            var dataURL = this.screenshotCanvas.toDataURL("image/png");
            return DataHelper.dataURItoBlob(dataURL);
        } else {
            return null;
        }
    }

    addCaptureEventToButton() {
        var myThis = this;
        this.screenshotCaptureButton.on('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            myThis.generateScreenshot();
        });
    }

    hideElements() {
        if (this.elementsToHide != null) {
            for (var elementToHide of this.elementsToHide) {
                elementToHide.hide();
            }
        }
    }

    showElements() {
        if (this.elementsToHide != null) {
            for (var elementToHide of this.elementsToHide) {
                elementToHide.show();
            }
        }
    }

    reset() {
        this.screenshotPreviewElement.hide();
        if (this.context) {
            this.context.clearRect(0, 0, this.context.width, this.context.height);
        }
        this.screenshotCanvas = null;
        this.canvasStates = [];
        this.container.find('.screenshot-operations').hide();

        this.disableAllScreenshotOperations();

        let screenshotCaptureButtonDefaultText = this.screenshotCaptureButton.data('default-text');
        this.screenshotCaptureButton.text(screenshotCaptureButtonDefaultText);
    }

    updateCanvasState() {
        this.canvasState.src = this.fabricCanvas.toDataURL("image/png");
        this.canvasStates.push(this.canvasState.src);
    }

    undoOperation() {
        if (this.canvasStates.length < 1) {
            return;
        }

        this.canvasState.src = this.canvasStates.pop();
        var context = this.fabricCanvas.getContext('2d');

        context.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
        context.drawImage(this.canvasState, 0, 0, this.canvasState.width,
            this.canvasState.height, 0, 0, this.fabricCanvas.width,
            this.fabricCanvas.height);
    }

    initScreenshotOperations() {
        var myThis = this;

        this.container.find('.screenshot-crop').on('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            myThis.initCrop();
        });

        this.container.find('.screenshot-draw-undo').on('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            myThis.undoOperation();
        });

        this.container.find('.screenshot-draw-remove').on('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            myThis.reset();
        });

        this.container.find('.screenshot-operations').show();
        this.container.find('.screenshot-operation.default-hidden').hide();
    }

    disableAllScreenshotOperations() {
        this.container.find('button.screenshot-operation').removeClass('active');
    }
}














