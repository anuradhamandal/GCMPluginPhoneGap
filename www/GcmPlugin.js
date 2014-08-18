(function( cordova ) {

    function GcmPlugin() {}

    GcmPlugin.prototype.gcm = function(win, fail) {
        return cordova.exec(
                function (args) { if(win !== undefined) { win(args); } },
                function (args) { if(fail !== undefined) { fail(args); } },
                "GcmPlugin", "Gcm", []);
    };

    if(!window.plugins) {
        window.plugins = {};
    }

    if (!window.plugins.GcmPlugin) {
        window.plugins.GcmPlugin = new GcmPlugin();
    }

})( window.cordova );
