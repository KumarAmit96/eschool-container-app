"""
Menu icon set for the Eskooltech (ESchool Container) app and web dashboard.

One visual family: a rounded colour tile with a soft top-left-to-bottom-right gradient and a white
line glyph (24-unit grid, Material-style strokes), one colour per feature. Writes SVG sources to
./svg and renders PNGs with headless Chrome.

    large/<name>.png  - dashboard tiles (icon_large), 512 px. No text: the app prints the name.
    small/<name>.png  - menu icons (icon_small). Each file keeps the size it has on the server
                        (mostly 128 px), and the file names match, so menu URLs keep working.

Usage:  python generate.py [output_dir]      (default: ./out)
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
CHROME = r"C:\Program Files\Google\Chrome\Application\chrome.exe"

# Accent shapes get a translucent white fill so each glyph has a little depth.
A = 'fill="#fff" fill-opacity=".22"'
DOT = 'fill="#fff" stroke="none"'

# Shared parts
CALENDAR = ('<rect x="3.5" y="5" width="17" height="15.5" rx="2.5" ' + A + '/>'
            '<path d="M3.5 9.5h17M8 3v4M16 3v4"/>')

GLYPHS = {
    "attendance": (
        '<rect x="5" y="4" width="14" height="17" rx="2.2" ' + A + '/>'
        '<rect x="9" y="2.5" width="6" height="3.2" rx="1.2" fill="#fff" stroke="none"/>'
        '<path d="M8.2 10.4l1.5 1.5 2.6-2.6M14.2 10.7h1.8M8.2 15.6l1.5 1.5 2.6-2.6M14.2 15.9h1.8"/>'),
    "student-attendance": (
        '<circle cx="9.5" cy="8" r="3.6" ' + A + '/>'
        '<path d="M3 20.5c0-3.6 2.9-6.3 6.5-6.3s6.5 2.7 6.5 6.3"/>'
        '<path d="M15.6 10.6l2 2 4-4"/>'),
    "birthday": (
        '<rect x="4" y="12" width="16" height="8.5" rx="2.2" ' + A + '/>'
        '<path d="M4 16c1.3 1 2.7 1 4 0s2.7-1 4 0 2.7 1 4 0 2.7-1 4 0M8 12V9.2M12 12V9.2M16 12V9.2M2.5 20.5h19"/>'
        '<path d="M8 7.2c-.9-.8-.9-1.9 0-3 .9 1.1.9 2.2 0 3zM12 7.2c-.9-.8-.9-1.9 0-3 .9 1.1.9 2.2 0 3zM16 7.2c-.9-.8-.9-1.9 0-3 .9 1.1.9 2.2 0 3z" ' + DOT + '/>'),
    "class-work": (
        '<rect x="3" y="3.5" width="18" height="12.5" rx="2" ' + A + '/>'
        '<path d="M7 8.2h5M7 11.5h8.5M9 16l-2 4.5M15 16l2 4.5"/>'),
    "dashboard": (
        '<rect x="4" y="4" width="7" height="9" rx="1.8" fill="#fff" stroke="none"/>'
        '<rect x="13" y="4" width="7" height="5" rx="1.8" ' + A + '/>'
        '<rect x="4" y="15" width="7" height="5" rx="1.8" ' + A + '/>'
        '<rect x="13" y="11" width="7" height="9" rx="1.8" ' + A + '/>'),
    "enquiry": (
        '<path d="M4 6.5A2.5 2.5 0 0 1 6.5 4h11A2.5 2.5 0 0 1 20 6.5v8a2.5 2.5 0 0 1-2.5 2.5H11l-4.2 3.5V17h-.3A2.5 2.5 0 0 1 4 14.5z" ' + A + '/>'
        '<path d="M10 8.6a2 2 0 1 1 2.8 1.8c-.5.3-.8.7-.8 1.2v.4"/>'
        '<circle cx="12" cy="14.2" r=".95" ' + DOT + '/>'),
    "exam-timetable": (
        '<path d="M11.5 21H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h7l4 4v3.5" ' + A + '/>'
        '<path d="M14 3v4h4M8.3 9h4.2M8.3 12.5h2.8"/>'
        '<circle cx="17" cy="17" r="4.6" fill="#fff" fill-opacity=".22"/>'
        '<path d="M17 14.8v2.4l1.6 1"/>'),
    "fee": (
        '<rect x="3" y="6" width="18" height="14.5" rx="2.6" ' + A + '/>'
        '<path d="M15.5 6V4.6a1.4 1.4 0 0 0-1.8-1.3L5.4 6"/>'
        '<path d="M21 11h-3.6a2.2 2.2 0 0 0 0 4.4H21"/>'
        '<path d="M7.2 10.2h5M7.2 12.5h5M9 10.2c2.4 0 2.4 2.9 0 2.9H7.6l3.6 3.7"/>'),
    "holiday-calendar": (
        CALENDAR +
        '<circle cx="12" cy="15" r="1.6" fill="#fff" stroke="none"/>'
        '<path d="M14.70 15.00L15.90 15.00M13.91 16.91L14.76 17.76M12.00 17.70L12.00 18.90M10.09 16.91L9.24 17.76M9.30 15.00L8.10 15.00M10.09 13.09L9.24 12.24M12.00 12.30L12.00 11.10M13.91 13.09L14.76 12.24" stroke-width="1.5"/>'),
    "school-timetable": (
        CALENDAR +
        '<g ' + DOT + '><circle cx="8" cy="13" r="1"/><circle cx="12" cy="13" r="1"/><circle cx="16" cy="13" r="1"/>'
        '<circle cx="8" cy="16.6" r="1"/><circle cx="12" cy="16.6" r="1"/><circle cx="16" cy="16.6" r="1" fill-opacity=".45"/></g>'),
    "home-work": (
        '<path d="M14.5 21H6.5A2.5 2.5 0 0 1 4 18.5v-13A2.5 2.5 0 0 1 6.5 3h9A2.5 2.5 0 0 1 18 5.5v4.5" ' + A + '/>'
        '<path d="M8 3v18M11 8h4M11 11.5h3"/>'
        '<path d="M19.4 13.2l1.6 1.6-5.7 5.7H13.7v-1.6z"/>'),
    "live": (
        '<circle cx="12" cy="12" r="2.4" ' + DOT + '/>'
        '<path d="M8.6 8.6a4.8 4.8 0 0 0 0 6.8M15.4 8.6a4.8 4.8 0 0 1 0 6.8M5.7 5.7a8.9 8.9 0 0 0 0 12.6M18.3 5.7a8.9 8.9 0 0 1 0 12.6"/>'),
    "model-paper": (
        '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z" ' + A + '/>'
        '<path d="M14 3v5h5M8.5 12h7M8.5 15.5h7M8.5 8.5h3"/>'),
    "notification": (
        '<path d="M6 16v-4.8a6 6 0 0 1 12 0V16l1.6 2.2H4.4z" ' + A + '/>'
        '<path d="M10 20.8a2.2 2.2 0 0 0 4 0"/>'
        '<circle cx="18" cy="5.5" r="2.4" ' + DOT + '/>'),
    "photo-gallery": (
        '<rect x="3" y="5" width="18" height="14" rx="2.4" ' + A + '/>'
        '<circle cx="8.5" cy="9.5" r="1.6"/>'
        '<path d="M3.3 16.6l5-4.4 3.8 3.3 2.9-2.4 5.7 4.4"/>'),
    "question-answer": (
        '<path d="M3 5.6A2.6 2.6 0 0 1 5.6 3h6.8A2.6 2.6 0 0 1 15 5.6v4A2.6 2.6 0 0 1 12.4 12.2H8.4L5.4 14.6v-2.4h0A2.4 2.4 0 0 1 3 9.8z" ' + A + '/>'
        '<path d="M7.7 6.4a1.3 1.3 0 1 1 1.9 1.2c-.35.2-.6.5-.6.9"/>'
        '<path d="M17.4 9h1A2.6 2.6 0 0 1 21 11.6v4a2.6 2.6 0 0 1-2.6 2.6h-.3v2.6l-3.2-2.6h-3.3A2.6 2.6 0 0 1 9 15.6v-.8"/>'
        '<path d="M13.2 15.2h4.4"/>'),
    "result": (
        '<path d="M8 4h8v5a4 4 0 0 1-8 0z" ' + A + '/>'
        '<path d="M8 6H5.2a3 3 0 0 0 3 4.2M16 6h2.8a3 3 0 0 1-3 4.2M12 13v3.8M8.5 20.5h7M9.8 16.8h4.4l.6 3.7H9.2z"/>'),
    "smart-class": (
        '<rect x="3" y="4" width="18" height="12.5" rx="2.2" ' + A + '/>'
        '<path d="M8.5 20.5h7M12 16.5v4"/>'
        '<path d="M10.5 7.9v4.8l4.1-2.4z" ' + DOT + '/>'),
    "student-detail": (
        '<rect x="3" y="5" width="18" height="14" rx="2.6" ' + A + '/>'
        '<circle cx="9" cy="10.6" r="2.2"/>'
        '<path d="M5.8 16.2c.5-1.6 1.7-2.5 3.2-2.5s2.7.9 3.2 2.5M14.5 9.8h3.6M14.5 13.3h2.6"/>'),
    "syllabus": (
        '<path d="M12 6.5C10 5 7.5 4.5 3.5 4.5v14c4 0 6.5.5 8.5 2 2-1.5 4.5-2 8.5-2v-14c-4 0-6.5.5-8.5 2z" ' + A + '/>'
        '<path d="M12 6.5v14M6.2 9h3.4M6.2 12.2h3.4M14.4 9h3.4M14.4 12.2h3.4"/>'),
    "video-gallery": (
        '<rect x="3" y="5" width="18" height="14.5" rx="2.4" ' + A + '/>'
        '<path d="M3 9.2h18M7 5l2 4.2M11.4 5l2 4.2M15.8 5l2 4.2"/>'
        '<path d="M10.5 11.9v4.6l4-2.3z" ' + DOT + '/>'),
    "payment": (
        '<rect x="3" y="5.5" width="18" height="13" rx="2.4" ' + A + '/>'
        '<path d="M3 10h18M7 15h3.5M14.5 15h2.5"/>'),
    "paid": (
        '<circle cx="12" cy="12" r="8.6" ' + A + '/>'
        '<path d="M8.2 12.3l2.6 2.6 5-5.2"/>'),
    "student": (
        '<circle cx="12" cy="8.4" r="3.9" ' + A + '/>'
        '<path d="M4.5 20.5c0-4 3.4-6.9 7.5-6.9s7.5 2.9 7.5 6.9"/>'),
    "report": (
        '<rect x="4" y="3" width="16" height="18" rx="2.4" ' + A + '/>'
        '<path d="M8 17v-3M12 17v-6.5M16 17v-9"/>'),
}

# One colour per feature: distinct hues so the menu is easy to scan, all dark enough for the white
# glyph (contrast >= 3:1). Dashboard, attendance and syllabus use the Eskooltech logo charcoal/green.
COLORS = {
    "dashboard": "#2B2F2C",          # logo charcoal
    "attendance": "#4F8A1A",         # logo green (darkened for contrast)
    "student-attendance": "#4F8A1A",
    "syllabus": "#3F7A12",
    "student": "#4F8A1A",
    "birthday": "#DB2777",
    "class-work": "#4F46E5",
    "enquiry": "#0369A1",
    "exam-timetable": "#DC2626",
    "fee": "#0F766E",
    "payment": "#0F766E",
    "paid": "#15803D",
    "holiday-calendar": "#EA580C",
    "school-timetable": "#1D4ED8",
    "home-work": "#2563EB",
    "live": "#E11D48",
    "model-paper": "#7C3AED",
    "notification": "#D97706",
    "photo-gallery": "#0891B2",
    "question-answer": "#9333EA",
    "result": "#B45309",
    "smart-class": "#047857",
    "student-detail": "#4338CA",
    "video-gallery": "#BE123C",
    "report": "#475569",
}

# Large server file name -> (design, size). paid/payment are tiny inline icons.
LARGE = {
    "attendance": ("attendance", 512), "birthday": ("birthday", 512), "class-work": ("class-work", 512),
    "dashboard": ("dashboard", 512), "enquiry": ("enquiry", 512), "Exam -Time-Table": ("exam-timetable", 512),
    "exam-timetable": ("exam-timetable", 512), "fee": ("fee", 512), "holiday-calendar": ("holiday-calendar", 512),
    "home-work": ("home-work", 512), "live": ("live", 512), "modal-paper": ("model-paper", 512),
    "notification": ("notification", 512), "paid": ("paid", 32), "payment": ("payment", 32),
    "photo-gallery": ("photo-gallery", 512), "question-answer": ("question-answer", 512),
    "result": ("result", 512), "School-Time-Table": ("school-timetable", 512),
    "school-timetable": ("school-timetable", 512), "smart-class": ("smart-class", 512),
    "student-attendance": ("student-attendance", 512), "student-detail": ("student-detail", 512),
    "syllabus": ("syllabus", 512), "video-gallery": ("video-gallery", 512),
}

# Server file name -> (design, size in px). Sizes match the current files on the server.
SMALL = {
    "birthday": ("birthday", 128), "class-work": ("class-work", 128), "dashboard": ("dashboard", 128),
    "enquiry": ("enquiry", 128), "Inquiry": ("enquiry", 128), "exam-timetable": ("exam-timetable", 128),
    "fee": ("fee", 128), "gallery": ("photo-gallery", 64), "holiday-calendar": ("holiday-calendar", 128),
    "home-work": ("home-work", 128), "live": ("live", 128), "modal-paper": ("model-paper", 128),
    "notification": ("notification", 128), "paid": ("paid", 32), "payment": ("payment", 32),
    "photo-gallery": ("photo-gallery", 128), "question-answer": ("question-answer", 128),
    "report": ("report", 64), "result": ("result", 128), "school-timetable": ("school-timetable", 128),
    "smart-class": ("smart-class", 128), "student-attendance": ("student-attendance", 128),
    "student-detail": ("student-detail", 128), "student-male-small": ("student", 32),
    "syllabus": ("syllabus", 128), "video-gallery": ("video-gallery", 128),
}


def shade(hex_color, factor):
    """factor > 0 mixes toward white, < 0 toward black."""
    r, g, b = (int(hex_color[i:i + 2], 16) for i in (1, 3, 5))
    if factor >= 0:
        r, g, b = (round(c + (255 - c) * factor) for c in (r, g, b))
    else:
        r, g, b = (round(c * (1 + factor)) for c in (r, g, b))
    return "#{:02X}{:02X}{:02X}".format(r, g, b)


def svg(design, size):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 100 100">
{tile(design, size)}
</svg>'''


def tile(design, size):
    """The gradient tile + glyph, drawn in a 100x100 box."""
    color = COLORS[design]
    light, dark = shade(color, 0.14), shade(color, -0.16)
    stroke = 2.1 if size <= 32 else 1.75
    return f'''<defs>
  <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="{light}"/><stop offset="1" stop-color="{dark}"/>
  </linearGradient>
  <linearGradient id="h" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#fff" stop-opacity=".18"/><stop offset=".5" stop-color="#fff" stop-opacity="0"/>
  </linearGradient>
</defs>
<rect x="2" y="2" width="96" height="96" rx="24" fill="url(#g)"/>
<rect x="2" y="2" width="96" height="96" rx="24" fill="url(#h)"/>
<g transform="translate(20 20) scale(2.5)" fill="none" stroke="#fff" stroke-width="{stroke}"
   stroke-linecap="round" stroke-linejoin="round">{GLYPHS[design]}</g>'''


def render(svg_path, png_path, size):
    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars",
                    "--default-background-color=00000000", f"--window-size={size},{size}",
                    f"--screenshot={png_path}", "file:///" + svg_path.replace("\\", "/")],
                   check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def main():
    out = os.path.abspath(sys.argv[1]) if len(sys.argv) > 1 else os.path.join(HERE, "out")
    svg_dir = os.path.join(HERE, "svg")
    os.makedirs(svg_dir, exist_ok=True)
    for design in GLYPHS:
        with open(os.path.join(svg_dir, design + ".svg"), "w", encoding="utf-8") as f:
            f.write(svg(design, 100))

    tmp = os.path.join(out, "_tmp")
    os.makedirs(tmp, exist_ok=True)
    for folder, mapping, make in (("large", LARGE, svg), ("small", SMALL, svg)):
        dest = os.path.join(out, folder)
        os.makedirs(dest, exist_ok=True)
        for name, (design, size) in mapping.items():
            src = os.path.join(tmp, f"{folder}_{design}_{size}.svg")
            if not os.path.exists(src):
                with open(src, "w", encoding="utf-8") as f:
                    f.write(make(design, size))
            render(src, os.path.join(dest, name + ".png"), size)
            print(folder, name, size)
    for f in os.listdir(tmp):
        os.remove(os.path.join(tmp, f))
    os.rmdir(tmp)


if __name__ == "__main__":
    main()
