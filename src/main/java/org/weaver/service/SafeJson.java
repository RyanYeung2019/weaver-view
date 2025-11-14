package org.weaver.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Minimal safe JSON parsing guard for untrusted inputs.
 * - rejects payloads with suspicious tokens (e.g. @type)
 * - limits payload size
 * - ensures the content looks like JSON
 *
 * Notes: this is a mitigation, not a full replacement for migrating away from fastjson.
 */
public class SafeJson {

    private static final int MAX_LENGTH = 20000; // adjust as needed

    public static JSONObject safeParseToJSONObject(String json) {
        if (json == null) return null;
        String trimmed = json.trim();
        if (trimmed.length() == 0) return null;
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            throw new RuntimeException("Invalid JSON content");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new RuntimeException("JSON payload too large");
        }
        String lower = trimmed.toLowerCase();
        // basic blacklist checks: forbid auto-type and class indicators
        if (lower.contains("@type") || lower.contains("\"@type\"") || lower.contains("class\"") || lower.contains("class:") || lower.contains("java.lang")) {
            throw new RuntimeException("JSON contains forbidden tokens");
        }
        // Finally call fastjson to parse into JSONObject (no autoType)
        return JSON.parseObject(trimmed);
    }
}
