"""
Captures the logged-in app screens from a connected phone for the Play Store listing.

Restarts the app on Home, then saves Home, the side menu and each page in PAGES (opened through
the side menu by its visible label) to playstore/raw. A screenshot is only taken while the app is
the app in front, so nothing else on the phone is ever captured. Run with the phone unlocked and
the app signed in:

    python playstore/capture_pages.py <adb-serial> [label ...]

raw/ is git-ignored: the captures contain student data.
"""
import os
import re
import subprocess
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
PACKAGE = "com.tech.eskool"


def sdk_dir():
    with open(os.path.join(HERE, "..", "local.properties"), encoding="utf-8") as f:
        for line in f:
            if line.startswith("sdk.dir="):
                return line.split("=", 1)[1].strip().replace("\\:", ":").replace("\\\\", "\\")
    return os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk")


ADB = os.path.join(sdk_dir(), "platform-tools", "adb.exe")
SERIAL = sys.argv[1] if len(sys.argv) > 1 else None
ONLY = set(sys.argv[2:])          # optional: capture just these labels (skips Home and menu shots)
RAW = os.path.join(HERE, "raw")
MENU_BUTTON = (70, 205)           # toolbar hamburger on a 1080-wide screen
PAGE_LOAD_SECONDS = 8

PAGES = ["Dashboard", "Attendance", "Notification", "Home Work", "Fee",
         "School Time Table", "Exam Time Table", "Result"]


def adb(*args, binary=False):
    cmd = [ADB] + (["-s", SERIAL] if SERIAL else []) + list(args)
    out = subprocess.run(cmd, capture_output=True, env=dict(os.environ, MSYS_NO_PATHCONV="1"))
    return out.stdout if binary else out.stdout.decode("utf-8", "replace")


def app_in_front():
    for line in adb("shell", "dumpsys", "activity", "activities").splitlines():
        if "topResumedActivity" in line:
            return PACKAGE + "/" in line
    return False


def screenshot(name):
    if not app_in_front():
        print("SKIPPED", name, "- the app is not in front")
        return
    with open(os.path.join(RAW, name), "wb") as f:
        f.write(adb("exec-out", "screencap", "-p", binary=True))
    print("saved", name)


def find(label):
    """Centre of the side-menu row whose text is exactly `label`, or None."""
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("shell", "cat", "/sdcard/ui.xml")
    m = re.search(r'text="' + re.escape(label) + r'" resource-id="' + PACKAGE + r':id/title"[^>]*'
                  r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
    if not m:
        return None
    x1, y1, x2, y2 = map(int, m.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap(x, y):
    adb("shell", "input", "tap", str(x), str(y))


def open_drawer_item(label):
    tap(*MENU_BUTTON)
    time.sleep(1.2)
    for _ in range(4):
        pos = find(label)
        if pos and 620 < pos[1] < 2250:      # fully visible: below the header, above the gesture bar
            tap(*pos)
            return True
        adb("shell", "input", "swipe", "300", "2000", "300", "1100", "400")   # scroll the menu
        time.sleep(0.8)
    adb("shell", "input", "keyevent", "KEYCODE_BACK")
    return False


def slug(text):
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def main():
    os.makedirs(RAW, exist_ok=True)
    adb("shell", "input", "keyevent", "KEYCODE_WAKEUP")
    adb("shell", "am", "start", "-n", PACKAGE + "/.StartActivity")
    time.sleep(9)
    if not ONLY:
        screenshot("10_home.png")
        tap(*MENU_BUTTON)
        time.sleep(1.5)
        screenshot("11_menu.png")
        adb("shell", "input", "keyevent", "KEYCODE_BACK")
        time.sleep(1)

    for i, label in enumerate(PAGES, start=20):
        if ONLY and label not in ONLY:
            continue
        if not open_drawer_item(label):
            print("not found in menu:", label)
            continue
        time.sleep(PAGE_LOAD_SECONDS)
        screenshot(f"{i}_{slug(label)}.png")


if __name__ == "__main__":
    main()
