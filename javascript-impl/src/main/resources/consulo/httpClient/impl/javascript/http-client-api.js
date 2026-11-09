// Declarations of the handler script API, invisible prefix of every injected {% %} block.
// Only for resolve and completion in the editor, the runtime is script-rhino/bootstrap.js.

/** Variable storage */
function HttpClientVariables() {
}
/** @param {string} name @returns {string} */
HttpClientVariables.prototype.get = function (name) { return ""; };
/** @param {string} name @param {*} value */
HttpClientVariables.prototype.set = function (name, value) { };
/** @returns {boolean} */
HttpClientVariables.prototype.isEmpty = function () { return true; };
/** @param {string} name */
HttpClientVariables.prototype.clear = function (name) { };
HttpClientVariables.prototype.clearAll = function () { };

/** Request or response headers */
function HttpClientHeaders() {
}
/** @returns {Array} */
HttpClientHeaders.prototype.all = function () { return []; };
/** @param {string} name */
HttpClientHeaders.prototype.findByName = function (name) { return {name: "", value: ""}; };
/** @param {string} name @returns {string} */
HttpClientHeaders.prototype.valueOf = function (name) { return ""; };
/** @param {string} name @returns {Array} */
HttpClientHeaders.prototype.valuesOf = function (name) { return []; };
/** @param {string} name @param {string} value */
HttpClientHeaders.prototype.set = function (name, value) { };
/** @param {string} name @param {string} value */
HttpClientHeaders.prototype.add = function (name, value) { };

var client = {
    /** Variables shared between requests and files */
    global: new HttpClientVariables(),
    /** @param {...*} values */
    log: function (values) { },
    /** @param {string} name @param {function} callback */
    test: function (name, callback) { },
    /** @param {boolean} condition @param {string} message */
    assert: function (condition, message) { },
    /** Stops the script */
    exit: function () { }
};

var request = {
    /** Variables of the current request */
    variables: new HttpClientVariables(),
    environment: {
        /** @param {string} name @returns {string} */
        get: function (name) { return ""; }
    },
    headers: new HttpClientHeaders(),
    url: {
        /** @returns {string} */
        getRaw: function () { return ""; },
        /** @returns {string} */
        tryGetSubstituted: function () { return ""; }
    },
    body: {
        /** @returns {string} */
        getRaw: function () { return ""; },
        /** @returns {string} */
        tryGetSubstituted: function () { return ""; },
        /** @returns {string} */
        string: function () { return ""; }
    },
    /** @type {string} */
    method: ""
};

var response = {
    /** @type {number} */
    status: 0,
    /** Parsed JSON for JSON responses, text otherwise */
    body: {},
    headers: new HttpClientHeaders(),
    contentType: {
        /** @type {string} */
        mimeType: "",
        /** @type {string} */
        charset: ""
    }
};

/** @param {*} object @param {string} expression */
function jsonPath(object, expression) { return null; }
/** @param {string} xml @param {string} expression @returns {string} */
function xpath(xml, expression) { return ""; }
/** @param {string} text @returns {string} */
function btoa(text) { return ""; }
/** @param {string} base64 @returns {string} */
function atob(base64) { return ""; }
var Window = {btoa: btoa, atob: atob};
/** @param {string} base64 @param {string} path */
function base64ToFile(base64, path) { }
/** @param {string} path @returns {string} */
function readString(path) { return ""; }
/** @param {string} path */
function require(path) { return {}; }
/** @param {string} query */
function URLSearchParams(query) { }
URLSearchParams.prototype.get = function (key) { return ""; };
URLSearchParams.prototype.set = function (key, value) { };
URLSearchParams.prototype.append = function (key, value) { };
URLSearchParams.prototype.has = function (key) { return true; };
URLSearchParams.prototype.delete = function (key) { };
/** crypto-js */
var CryptoJS = {};
var console = {log: client.log, info: client.log, warn: client.log, error: client.log, debug: client.log};

