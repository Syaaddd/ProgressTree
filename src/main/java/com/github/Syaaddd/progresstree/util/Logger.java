package com.github.Syaaddd.progresstree.util;

import java.util.logging.Level;

/**
 * Leveled logger wrapper. When debug=false, fine/debug calls are suppressed.
 * Fixes: "debug log dihapus total dari production" (v1.0.6).
 */
public class Logger {

    private final java.util.logging.Logger log;
    private final boolean debug;

    public Logger(Object plugin, boolean debug) {
        if (plugin instanceof org.bukkit.plugin.Plugin p) {
            this.log = p.getLogger();
        } else {
            this.log = java.util.logging.Logger.getLogger("ProgressTree");
        }
        this.debug = debug;
    }

    public void info(String msg) {
        log.info("[PT] " + msg);
    }

    public void warn(String msg) {
        log.warning("[PT] " + msg);
    }

    public void severe(String msg) {
        log.severe("[PT] " + msg);
    }

    /** Only prints when config debug=true */
    public void debug(String msg) {
        if (debug) {
            log.log(Level.INFO, "[PT:DEBUG] " + msg);
        }
    }

    public boolean isDebugEnabled() {
        return debug;
    }
}
