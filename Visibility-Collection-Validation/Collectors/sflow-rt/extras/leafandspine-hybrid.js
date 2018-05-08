/**
 * leafandspine-hybrid.js
 * 
 * Copyright (c) InMon Corp. ALL RIGHTS RESERVED
 * 
 * @version $Id: leafandspine-hybrid.js 849 2014-12-01 12:03:17Z sgj $
 * 
 * leafandspine-hybrid.js provides emulation of true Integrated Hybrid OpenFlow
 * when using the mininet leafandspine.py simulation.
 */

(function() {
    
    var controllerSetOfRule = setOfRule;
    var ruleTable = 0;
    var normalTable = 1;
   
    /**
     * Returns {@code true} if {@code o} is non-null and defined.
     * @param o the object to test.
     * @return {@code true} if {@code o} is non-null and defined.
     */
    function isValid(o) {
        return o !== undefined && o !== null ;
    }
    
    function getProperty(property, defaultValue)
    {
        var value = getSystemProperty(property);
        if (isValid(value)) {
            return value;
        } else {
            return defaultValue;
        }
    }
    
    /**
     * jQuery-like extend functionality
     * extend(target [,object1][,objectN])
     * extend([deep,] target, object1, [,objectN])
     * @param deep true for recursive merge - false is not supported.
     * @param target object that will receive the new properties.
     * @param object1 an object containing properties to merge in.
     * @param objectN additional objects containing properties to merge in.
     * @returns extended target object
     */
    function extend() {
        var options, name, src, copy, copyIsArray, clone, 
            target = arguments[0] || {},
            i = 1,
            length = arguments.length,
            deep = false,
            toString = Object.prototype.toString,
            hasOwn = Object.prototype.hasOwnProperty,
            push = Array.prototype.push,
            slice = Array.prototype.slice,
            trim = String.prototype.trim,
            indexOf = Array.prototype.indexOf,
            class2type = {
                    "[object Boolean]": "boolean",
                    "[object Number]": "number",
                    "[object String]": "string",
                    "[object Function]": "function",
                    "[object Array]": "array",
                    "[object Date]": "date",
                    "[object RegExp]": "regexp",
                    "[object Object]": "object"
            },
            jQuery = {
                    isFunction: function (obj) {
                        return jQuery.type(obj) === "function"
                    },
                    isArray: Array.isArray ||
                    function (obj) {
                        return jQuery.type(obj) === "array"
                    },
                    isWindow: function (obj) {
                        return obj != null && obj == obj.window
                    },
                    isNumeric: function (obj) {
                        return !isNaN(parseFloat(obj)) && isFinite(obj)
                    },
                    type: function (obj) {
                        return obj == null ? 
                                String(obj) : class2type[toString.call(obj)] || "object"
                    },
                    isPlainObject: function (obj) {
                        if (!obj || jQuery.type(obj) !== "object" || obj.nodeType) {
                            return false
                        }
                        try {
                            if (obj.constructor && !hasOwn.call(obj, "constructor") 
                                    && !hasOwn.call(obj.constructor.prototype, "isPrototypeOf")) {
                                return false
                            }
                        } catch (e) {
                            return false
                        }
                        var key;
                        for (key in obj) {}
                        return key === undefined || hasOwn.call(obj, key)
                    }
            };
        if (typeof target === "boolean") {
            deep = target;
            target = arguments[1] || {};
            i = 2;
        }
        if (typeof target !== "object" && !jQuery.isFunction(target)) {
            target = {}
        }
        if (length === i) {
            target = this;
            --i;
        }
        for (i; i < length; i++) {
            if ((options = arguments[i]) != null) {
                for (name in options) {
                    src = target[name];
                    copy = options[name];
                    if (target === copy) {
                        continue
                    }
                    if (deep && copy && (jQuery.isPlainObject(copy) || (copyIsArray = jQuery.isArray(copy)))) {
                        if (copyIsArray) {
                            copyIsArray = false;
                            clone = src && jQuery.isArray(src) ? src : []
                        } else {
                            clone = src && jQuery.isPlainObject(src) ? src : {};
                        }
                        // WARNING: RECURSION
                        target[name] = extend(deep, clone, copy);
                    } else if (copy !== undefined) {
                        target[name] = copy;
                    }
                }
            }
        }
        return target;
    }
    
    function initialize() {
        ruleTable = getProperty("landsHybrid.ruleTable", ruleTable);
        normalTable = getProperty("landsHybrid.normalTable", normalTable);
        logInfo("Lead and spine hybrid mode enabled");
    }
    
    /**
     * Override regular setOfRule to provide Integrated Hybrid OpenFlow for
     * mininet with OpenFlow 1.3.
     */
    setOfRule = function(dpid, name, rule, success, failure, removal) {
        var i;
        var actions;
        var modifiedRule = {};
        extend(modifiedRule, rule);
        if (!isValid(modifiedRule.table)) {
            modifiedRule.table = ruleTable;
        }
        actions = modifiedRule.actions;
        if (isValid(actions)) {
            for (i = 0; i < actions.length; i++) {
                if (actions[i].match(/^\s*output\s*=\s*normal\s*$/)) {
                    actions[i] = "goto_table="+normalTable;
                }
            }
            
        }
        controllerSetOfRule(dpid, name, modifiedRule, success, failure, removal);
    };
        
    initialize();

})();