package com.zigzura.droplets.data

object PromptGenerator {

    private  val PROP_FORMAT = """Generate single-file HTML app for Android WebView. Vanilla JS, NO frameworks (React/Vue/etc).

OUTPUT FORMAT:
Respond with ONLY the complete HTML code. No explanations, no markdown code blocks, no commentary.
Start with <!DOCTYPE html> and end with </html>. Nothing before or after.

REQUIRED META TAG:
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

MOBILE DESIGN:
- Center content for portrait phones (360-400px width typical)
- Use flexbox/grid for layout
- Font size ≥16px (prevents zoom on input)
- Touch targets ≥44px
- Responsive padding: padding:20px

ALLOWED CDN LIBRARIES:
<script src="https://cdnjs.cloudflare.com/..."></script>
<link href="https://cdn.jsdelivr.net/...">
Domains: cdnjs.cloudflare.com, cdn.jsdelivr.net, unpkg.com, fonts.googleapis.com

STRICTLY FORBIDDEN:
- fetch(), XMLHttpRequest, WebSocket
- External APIs: weather, news, stocks, maps, etc.
- localStorage/sessionStorage (use Android.saveData instead)
- Geolocation, camera, microphone
- navigator.sendBeacon, service workers

ANDROID API (auto-scoped per app):
Storage:
  Android.saveData(key,value) - key & value are strings
  Android.loadData(key)→string - returns "" if not found
  Android.getAllData()→JSON - get all data as JSON string
  Android.deleteData(key)

Reminders:
  Android.setReminder(id,milliseconds,title,message) - id is unique string, milliseconds is Unix timestamp
  Android.cancelReminder(id)
  Android.getReminders()→JSON - array of {id,time,title,message}

Check availability: if(typeof Android!=='undefined'){...}

REJECTION PROTOCOL:
If prompt needs forbidden features, respond with ONLY:

XPROMPTREJECTREASON: [Feature] requires [blocked capability]. Suggestion: [alternative approach or app type].

Examples:
XPROMPTREJECTREASON: Weather data requires external API. Suggestion: Create weather simulator with demo data, or temperature unit converter.
XPROMPTREJECTREASON: Camera access unavailable. Suggestion: Create drawing app or photo filter simulator with sample images.

REQUIREMENTS:
✓ Complete working code (no TODOs/placeholders)
✓ All CSS inline in <style>
✓ All JS inline in <script>
✓ Beautiful, polished mobile UI
✓ If data needed: use realistic demo/simulated data
✓ App works immediately without setup

EXAMPLE STRUCTURE:
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>App Name</title>
<style>
body{margin:0;padding:20px;font-family:system-ui;display:flex;flex-direction:column;min-height:100vh;align-items:center}
button{padding:12px 24px;font-size:16px;min-height:44px}
</style>
</head>
<body>
<h1>App Title</h1>
<button onclick="doSomething()">Action</button>
<script>
function doSomething(){
  if(typeof Android!=='undefined'){
    Android.saveData('key','value');
  }
}
</script>
</body>
</html>

CRITICAL: Output ONLY the HTML. Do not wrap in markdown code blocks. Do not add explanations.

User request: %s"""

    fun generatePrompt(userInput: String): String {
        return PROP_FORMAT.format(userInput)
    }
}