"use strict";

$(document).ready(function() {
var wsUri = "ws://localhost:8008/logs_broadcast?name=aaa";
var websocket;

// INIT MODULE
function init() {
    initWebSocket();
}
function initWebSocket() {

    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) {
        onOpen(evt)
    };
    websocket.onclose = function(evt) {
        onClose(evt)
    };
    websocket.onmessage = function(evt) {
        onMessage(evt)
    };
    websocket.onerror = function(evt) {
        onError(evt)
    };
}
// WEBSOCKET EVENTS MODULE
function onOpen(evt) {
}
function onClose(evt) {
}
function onMessage(evt) {
   $("#debug").html(evt.data)
}
function onError(evt) {
}
function doSend(message) {
    //unused, left for future usage
    websocket.send(message);
}

init();
});