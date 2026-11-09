// Handler script API: client, request, response, console.
// Functions named __bridge_* are implemented in Java (RhinoJsExecutor).

var __HTTP_CLIENT_EXIT = "__http_client_exit__";

function __toStringOrNull(value) {
    return value === null || value === undefined ? null : String(value);
}

function __joinArgs(args) {
    var parts = [];
    for (var i = 0; i < args.length; i++) {
        var value = args[i];
        if (value !== null && typeof value === "object") {
            try {
                parts.push(JSON.stringify(value));
                continue;
            }
            catch (e) {
                // fall through
            }
        }
        parts.push(String(value));
    }
    return parts.join(" ");
}

function __variables(prefix) {
    return {
        get: function (name) {
            return __bridge_varGet(prefix, String(name));
        },
        set: function (name, value) {
            __bridge_varSet(prefix, String(name), __toStringOrNull(value));
        },
        isEmpty: function () {
            return __bridge_varIsEmpty(prefix);
        },
        clear: function (name) {
            __bridge_varSet(prefix, String(name), null);
        },
        clearAll: function () {
            __bridge_varClearAll(prefix);
        }
    };
}

var client = {
    global: __variables("global"),
    log: function () {
        __bridge_log(__joinArgs(arguments));
    },
    test: function (name, callback) {
        try {
            callback();
            __bridge_test(String(name), true, null);
        }
        catch (e) {
            if (e === __HTTP_CLIENT_EXIT) {
                throw e;
            }
            __bridge_test(String(name), false, e && e.message ? String(e.message) : String(e));
        }
    },
    assert: function (condition, message) {
        if (!condition) {
            throw new Error(message ? String(message) : "Assertion failed");
        }
    },
    exit: function () {
        throw __HTTP_CLIENT_EXIT;
    }
};

var console = {
    log: client.log,
    info: client.log,
    warn: client.log,
    error: client.log,
    debug: client.log
};

function __headersObject(listJson) {
    var list = JSON.parse(listJson);
    return {
        all: function () {
            return list;
        },
        findByName: function (name) {
            var lower = String(name).toLowerCase();
            for (var i = 0; i < list.length; i++) {
                if (list[i].name.toLowerCase() === lower) {
                    return list[i];
                }
            }
            return null;
        },
        valueOf: function (name) {
            var values = this.valuesOf(name);
            return values.length === 0 ? null : values[0];
        },
        valuesOf: function (name) {
            var lower = String(name).toLowerCase();
            var result = [];
            for (var i = 0; i < list.length; i++) {
                if (list[i].name.toLowerCase() === lower) {
                    result.push(list[i].value);
                }
            }
            return result;
        }
    };
}

var request = {
    variables: __variables("request"),
    environment: {
        get: function (name) {
            return __bridge_envGet(String(name));
        }
    },
    headers: {
        all: function () {
            return __headersObject(__bridge_requestHeaders()).all();
        },
        findByName: function (name) {
            return __headersObject(__bridge_requestHeaders()).findByName(name);
        },
        set: function (name, value) {
            __bridge_requestHeaderSet(String(name), String(value));
        },
        add: function (name, value) {
            __bridge_requestHeaderAdd(String(name), String(value));
        }
    },
    url: {
        getRaw: function () {
            return __bridge_requestUrl(false);
        },
        tryGetSubstituted: function () {
            return __bridge_requestUrl(true);
        }
    },
    body: {
        getRaw: function () {
            return __bridge_requestBody();
        },
        tryGetSubstituted: function () {
            return __bridge_requestBody();
        },
        string: function () {
            return __bridge_requestBody();
        }
    }
};

Object.defineProperty(request, "method", {
    get: function () {
        return __bridge_requestMethod();
    }
});

function __createResponse(status, headersJson, bodyText, isJson, mimeType, charset) {
    var body = bodyText;
    if (isJson) {
        try {
            body = JSON.parse(bodyText);
        }
        catch (e) {
            // keep text
        }
        if (body !== null && typeof body === "object") {
            Object.defineProperty(body, "jsonPath", {
                enumerable: false,
                value: {
                    evaluate: function (expression) {
                        return jsonPath(body, expression);
                    }
                }
            });
        }
    }
    else if (/[\/+]xml$/.test(mimeType || "")) {
        // String object: still usable as text, and carries the xpath helper like the previous plugin API
        body = new String(bodyText);
        Object.defineProperty(body, "xpath", {
            enumerable: false,
            value: {
                evaluate: function (expression) {
                    return xpath(bodyText, expression);
                }
            }
        });
    }

    return {
        status: status,
        body: body,
        headers: __headersObject(headersJson),
        contentType: {
            mimeType: mimeType,
            charset: charset
        }
    };
}

// ---- helpers shared with the previous plugin API ----

function btoa(text) {
    return __bridge_btoa(String(text));
}

function atob(base64) {
    return __bridge_atob(String(base64));
}

var Window = {
    btoa: btoa,
    atob: atob
};

function base64ToFile(base64, path) {
    var file = __bridge_base64ToFile(String(base64), String(path));
    client.log("Saved base64 content to " + file);
}

function readString(path) {
    return __bridge_readFile(String(path));
}

/**
 * Runs a local script (relative to the .http file) and returns its module.exports, or an object with the
 * top level variables it defined.
 */
function require(path) {
    var source = __bridge_readFile(String(path));
    var module = {exports: {}};
    var scope = {};
    var names = [];
    var re = /^(?:var|let|const|function)\s+([A-Za-z_$][\w$]*)/gm;
    var match;
    while ((match = re.exec(source)) !== null) {
        names.push(match[1]);
    }
    var collect = names.map(function (name) {
        return "try { __scope['" + name + "'] = " + name + "; } catch (e) {}";
    }).join("\n");
    var fn = new Function("module", "exports", "__scope", source + "\n" + collect);
    fn(module, module.exports, scope);
    return Object.keys(module.exports).length > 0 || typeof module.exports !== "object" ? module.exports : scope;
}

/**
 * XPath over a XML string (or response.body of a XML response), result as text.
 */
function xpath(xml, expression) {
    return __bridge_xpath(String(xml), String(expression));
}

/**
 * JSONPath subset: $, .name, ['name'], [index], [*], .*, ..name (deep scan).
 * A path without wildcards returns the value, otherwise an array of matches.
 */
function jsonPath(object, expression) {
    var root = typeof object === "string" ? JSON.parse(object) : object;
    var path = String(expression).trim();
    if (path.charAt(0) !== "$") {
        throw new Error("JSONPath must start with $: " + path);
    }

    var tokens = [];
    var i = 1;
    var multiple = false;
    while (i < path.length) {
        var c = path.charAt(i);
        if (c === ".") {
            var deep = path.charAt(i + 1) === ".";
            i += deep ? 2 : 1;
            var start = i;
            while (i < path.length && path.charAt(i) !== "." && path.charAt(i) !== "[") {
                i++;
            }
            var name = path.substring(start, i);
            if (deep) {
                multiple = true;
                tokens.push({deep: name});
            }
            else if (name === "*") {
                multiple = true;
                tokens.push({wildcard: true});
            }
            else {
                tokens.push({name: name});
            }
        }
        else if (c === "[") {
            var end = path.indexOf("]", i);
            if (end < 0) {
                throw new Error("Unclosed [ in JSONPath: " + path);
            }
            var inner = path.substring(i + 1, end).trim();
            i = end + 1;
            if (inner === "*") {
                multiple = true;
                tokens.push({wildcard: true});
            }
            else if (/^-?\d+$/.test(inner)) {
                tokens.push({index: parseInt(inner, 10)});
            }
            else {
                tokens.push({name: inner.replace(/^['"]|['"]$/g, "")});
            }
        }
        else {
            throw new Error("Unexpected '" + c + "' in JSONPath: " + path);
        }
    }

    function children(value) {
        if (Array.isArray(value)) {
            return value;
        }
        if (value !== null && typeof value === "object") {
            return Object.keys(value).map(function (key) {
                return value[key];
            });
        }
        return [];
    }

    function scan(value, name, result) {
        if (value !== null && typeof value === "object") {
            if (!Array.isArray(value) && Object.prototype.hasOwnProperty.call(value, name)) {
                result.push(value[name]);
            }
            children(value).forEach(function (child) {
                scan(child, name, result);
            });
        }
    }

    var current = [root];
    tokens.forEach(function (token) {
        var next = [];
        current.forEach(function (value) {
            if (value === null || value === undefined) {
                return;
            }
            if (token.deep !== undefined) {
                scan(value, token.deep, next);
            }
            else if (token.wildcard) {
                next = next.concat(children(value));
            }
            else if (token.index !== undefined) {
                if (Array.isArray(value)) {
                    var index = token.index < 0 ? value.length + token.index : token.index;
                    if (index >= 0 && index < value.length) {
                        next.push(value[index]);
                    }
                }
            }
            else if (typeof value === "object" && Object.prototype.hasOwnProperty.call(value, token.name)) {
                next.push(value[token.name]);
            }
        });
        current = next;
    });

    if (multiple) {
        return current;
    }
    return current.length === 0 ? null : current[0];
}

function URLSearchParams(queryParams) {
    this.params = {};
    if (queryParams !== null && queryParams !== undefined && queryParams !== "") {
        var params = this.params;
        String(queryParams).replace(/^\?/, "").split("&").forEach(function (pair) {
            var parts = pair.split("=");
            params[decodeURIComponent(parts[0])] = decodeURIComponent(parts[1] || "");
        });
    }
}

URLSearchParams.prototype.append = function (key, value) {
    this.params[key] = value;
};

URLSearchParams.prototype.has = function (key) {
    return this.params[key] !== undefined;
};

URLSearchParams.prototype.get = function (key) {
    return this.params[key] === undefined ? null : this.params[key];
};

URLSearchParams.prototype.set = function (key, value) {
    this.params[key] = value;
};

URLSearchParams.prototype.delete = function (key) {
    delete this.params[key];
};

URLSearchParams.prototype.toString = function () {
    var params = this.params;
    return Object.keys(params).map(function (key) {
        return encodeURIComponent(key) + "=" + encodeURIComponent(params[key]);
    }).join("&");
};

// crypto-js is big: loaded on first use
Object.defineProperty(this, "CryptoJS", {
    configurable: true,
    get: function () {
        var cryptoJs = __bridge_cryptoJs();
        Object.defineProperty(this, "CryptoJS", {value: cryptoJs});
        return cryptoJs;
    }
});
