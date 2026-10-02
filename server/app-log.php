<?php
/**
 * app-log.php - receives log lines from the Eskooltech Android app and appends them
 * to android-app.log.
 *
 * Upload to:  https://student.eskooltech.com/api/app-log.php
 * The app sends:  POST, JSON body {"device":{...},"logs":[{"t","l","tag","m","th"}...]}
 *                 header X-Log-Key: <LOG_KEY below>
 */

header('Content-Type: application/json; charset=utf-8');

// ---------------------------------------------------------------- settings
// Must match LOG_KEY in the app's com/tech/eskool/Log.java
const LOG_KEY = '871ac077075c3c393ef71b08529d7366';

// Where android-app.log is written. Keep it OUTSIDE the public web folder if you can,
// so nobody can download it from the browser. Change this path to suit your server.
$LOG_DIR = dirname(__DIR__, 2) . '/app-logs';
// Fallback if the folder above can't be created: a "logs" folder next to this file.
// (On nginx/OpenResty, block it with:  location ^~ /api/logs/ { deny all; } )
$FALLBACK_DIR = __DIR__ . '/logs';

const LOG_FILE_NAME  = 'android-app.log';
const MAX_BODY_BYTES = 512 * 1024;        // reject bigger requests
const MAX_LINES      = 200;               // lines accepted per request
const MAX_FILE_BYTES = 10 * 1024 * 1024;  // rotate android-app.log at 10 MB
const KEEP_ROTATED   = 10;                // keep this many old log files
date_default_timezone_set('Asia/Kolkata');

// ---------------------------------------------------------------- helpers
function reply(int $code, array $data): void {
    http_response_code($code);
    echo json_encode($data);
    exit;
}

/** Remove control characters; keep newlines (stack traces) but indent them. */
function clean($value, int $max = 4000): string {
    $s = is_scalar($value) ? (string) $value : '';
    $s = preg_replace('/[^\P{C}\n\t]/u', '', $s) ?? '';
    if (mb_strlen($s) > $max) $s = mb_substr($s, 0, $max) . '...[cut]';
    return str_replace("\n", "\n        ", rtrim($s));
}

function logDir(string $preferred, string $fallback): ?string {
    foreach ([$preferred, $fallback] as $dir) {
        if (is_dir($dir) || @mkdir($dir, 0750, true)) {
            if (is_writable($dir)) return $dir;
        }
    }
    return null;
}

function rotate(string $file, string $dir): void {
    clearstatcache(true, $file);
    if (!is_file($file) || filesize($file) < MAX_FILE_BYTES) return;
    @rename($file, $dir . '/android-app-' . date('Ymd-His') . '.log');
    $old = glob($dir . '/android-app-*.log') ?: [];
    rsort($old);
    foreach (array_slice($old, KEEP_ROTATED) as $f) @unlink($f);
}

// ---------------------------------------------------------------- checks
if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') {
    reply(405, ['status' => false, 'message' => 'POST only']);
}
if (!hash_equals(LOG_KEY, (string) ($_SERVER['HTTP_X_LOG_KEY'] ?? ''))) {
    reply(403, ['status' => false, 'message' => 'Forbidden']);
}

$raw = file_get_contents('php://input', false, null, 0, MAX_BODY_BYTES + 1);
if ($raw === false || strlen($raw) > MAX_BODY_BYTES) {
    reply(413, ['status' => false, 'message' => 'Too large']);
}

$data = json_decode($raw, true);
if (!is_array($data) || !isset($data['logs']) || !is_array($data['logs'])) {
    reply(400, ['status' => false, 'message' => 'Invalid JSON']);
}

// ---------------------------------------------------------------- write
$d    = is_array($data['device'] ?? null) ? $data['device'] : [];
$ip   = $_SERVER['HTTP_CF_CONNECTING_IP'] ?? $_SERVER['REMOTE_ADDR'] ?? '-';
$who  = sprintf('%s | %s | Android %s | app %s %s | session %s',
    clean($ip, 64), clean($d['model'] ?? '-', 80), clean($d['android'] ?? '-', 40),
    clean($d['app'] ?? '-', 40), clean($d['build'] ?? '', 10), clean($d['session'] ?? '-', 16));
if (!empty($d['user'])) $who .= ' | user ' . clean($d['user'], 80);

$out = '';
$count = 0;
foreach (array_slice($data['logs'], 0, MAX_LINES) as $line) {
    if (!is_array($line)) continue;
    $ms    = is_numeric($line['t'] ?? null) ? (int) $line['t'] : (int) (microtime(true) * 1000);
    $time  = date('Y-m-d H:i:s', intdiv($ms, 1000)) . sprintf('.%03d', $ms % 1000);
    $level = strtoupper(clean($line['l'] ?? 'I', 1));
    $tag   = clean($line['tag'] ?? '', 60);
    $msg   = clean($line['m'] ?? '');
    $out  .= "[$time] $level/$tag: $msg\n        ($who)\n";
    $count++;
}

if ($count === 0) {
    reply(200, ['status' => true, 'saved' => 0]);
}

$dir = logDir($LOG_DIR, $FALLBACK_DIR);
if ($dir === null) {
    reply(500, ['status' => false, 'message' => 'Log folder not writable']);
}

$file = $dir . '/' . LOG_FILE_NAME;
rotate($file, $dir);
if (file_put_contents($file, $out, FILE_APPEND | LOCK_EX) === false) {
    reply(500, ['status' => false, 'message' => 'Write failed']);
}

reply(200, ['status' => true, 'saved' => $count]);
