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
- Use min-height: 100vh (NOT height: 100vh) to prevent viewport cutoff
- For body/container: flex with min-height allows proper scrolling
- Use flexbox/grid for layout
- Font size ≥16px (prevents zoom on input)
- Touch targets ≥44px
- Responsive padding: padding:20px

ALLOWED CDN LIBRARIES (via <script src> or <link href>):
✓ Three.js: https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js
✓ Chart.js: https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.9.1/chart.min.js
✓ D3.js: https://cdnjs.cloudflare.com/ajax/libs/d3/7.8.5/d3.min.js
✓ Tone.js (audio): https://cdnjs.cloudflare.com/ajax/libs/tone/14.8.49/Tone.js
✓ Math.js: https://cdnjs.cloudflare.com/ajax/libs/mathjs/11.11.0/math.min.js
✓ Bootstrap CSS: https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css
✓ Google Fonts: https://fonts.googleapis.com

AVAILABLE ANDROID FEATURES (ONLY THESE):
✓ Storage:
  Android.saveData(key,value) - save string data
  Android.loadData(key)→string - load data
  Android.getAllData()→JSON - get all data
  Android.deleteData(key) - delete key
  
✓ Reminders:
  Android.setReminder(id,milliseconds,title,message) - schedule notification
  Android.cancelReminder(id) - cancel reminder
  Android.getReminders()→JSON - list reminders

CRITICAL JAVASCRIPT PATTERNS:

✓ Always check Android availability: if(typeof Android!=='undefined'){...}
✓ Load data on page load:
  if (typeof Android !== 'undefined') {
    loadStoredData();
  }
✓ When saving, track the KEY used so you can delete with the SAME key later

UNAVAILABLE ANDROID FEATURES (MUST REJECT):
✗ Camera/Photos: navigator.mediaDevices, getUserMedia, <input type="file" accept="image/*">, camera access
✗ Microphone/Audio Input: audio recording, speech recognition, microphone access
✗ Geolocation: navigator.geolocation, GPS, location services
✗ Device Sensors: accelerometer, gyroscope, compass, proximity sensor
✗ Contacts: reading/writing device contacts
✗ Calendar: reading/writing device calendar events
✗ Phone/SMS: making calls, sending SMS, reading messages
✗ File System: reading/writing files, file picker, downloads folder
✗ Bluetooth: BLE, Bluetooth devices
✗ NFC: NFC tags, payments
✗ Notifications: Push notifications (reminders via Android.setReminder work)
✗ Biometrics: fingerprint, face recognition
✗ Screen: brightness control, orientation lock, keep awake
✗ Vibration: navigator.vibrate (not available)
✗ Clipboard: reading clipboard (writing via execCommand may work)
✗ Share API: navigator.share (not available)
✗ Battery: battery status API

DESIGN LANGUAGE:
Default to Design V2 (dark/glassmorphic) unless user specifies otherwise.

Design V2 (DEFAULT - Dark Glassmorphic):
- Background: Dark slate (#0f172a)
- Container: Semi-transparent dark cards with glassmorphism
  background: linear-gradient(145deg, #1e293b, #0f172a);
  border: 1px solid #334155;
  backdrop-filter: blur(10px);
- Accent color: Cyan (#06b6d4)
- Typography: Sans-serif, uppercase headers with letter-spacing
- Buttons: Semi-transparent with subtle borders, lift on hover
- Style: Sleek, professional, cyberpunk, minimalist

Design V1 (Quirky Retro - use if user asks for "fun", "playful", "colorful", "quirky"):
- Background: Vibrant gradient (purple/pink)
- Container: White card, slightly rotated (-1deg)
- Colors: Multi-color (yellow #ffd93d, pink #f687b3, green #48bb78, red #fc8181)
- Typography: Monospace (Courier New), playful text-shadow
- Buttons: Solid colors with chunky shadows (0 4px 0)
- Style: Retro arcade, playful, energetic, bold

If user requests:
- "modern", "sleek", "professional", "dark" → Use V2
- "fun", "colorful", "playful", "bright" → Use V1
- No style specified → Use V2 (default)

STRICTLY FORBIDDEN (MUST REJECT):
✗ fetch(), XMLHttpRequest, WebSocket - NO network requests
✗ Live external data: stock prices, crypto, weather, news, social media
✗ APIs requiring real-time data
✗ localStorage/sessionStorage (use Android.saveData instead)
✗ Service Workers, Web Workers
✗ IndexedDB, Web SQL

APPS THAT WORK (generate these):
✓ 3D graphics, games, simulations (use Three.js!)
✓ Charts with demo data (use Chart.js)
✓ Canvas drawing/painting apps
✓ Music/audio synthesis apps (use Tone.js - no recording)
✓ Calculators, converters, utilities
✓ Timers, stopwatches, countdowns, alarms (use Android.setReminder)
✓ Notes, todo lists (use Android.saveData)
✓ Offline games: tic-tac-toe, snake, dice, cards, puzzles
✓ Random generators: names, passwords, jokes, colors
✓ Simulators with demo data: weather, stocks, social posts
✓ Text editors, markdown editors
✓ Animations, particle effects
✓ Math/science tools, visualizations
✓ Habit trackers, mood trackers
✓ Pixel art editors, color pickers
✓ Music players with embedded audio

APPS THAT DON'T WORK (reject these with suggestions):
✗ Photo editor/camera app → Reject: Drawing app or color filter simulator
✗ QR code scanner → Reject: QR code generator (create codes, not scan)
✗ Voice recorder → Reject: Audio synthesizer or music maker (Tone.js)
✗ Barcode scanner → Reject: Barcode generator
✗ GPS tracker/maps → Reject: Compass simulator or distance calculator
✗ Step counter → Reject: Manual activity logger
✗ Flashlight app → Reject: Screen flashlight (bright white screen)
✗ Social media with photo upload → Reject: Text-based post designer
✗ Live stock prices → Reject: Stock price simulator
✗ Real-time chat → Reject: Offline message composer
✗ Video player from device → Reject: Embedded video with URLs
✗ Contact manager → Reject: Personal contact list (stored in app)
✗ PDF reader from files → Reject: Text document viewer
✗ Music player from device files → Reject: Music synthesizer
✗ Image gallery from photos → Reject: Emoji/ASCII art gallery
✗ Weather with real data → Reject: Weather simulator
✗ Navigation app → Reject: Direction game or manual route planner

REJECTION PROTOCOL:
If prompt requires unavailable features, respond with ONLY:

XPROMPTREJECTREASON: [Feature] requires [unavailable capability]. Suggestion: [specific alternative that works].

Examples:
XPROMPTREJECTREASON: Camera access unavailable. Suggestion: Drawing app with brush tools and color palette.
XPROMPTREJECTREASON: Photo upload requires file access. Suggestion: Emoji collage maker or ASCII art generator.
XPROMPTREJECTREASON: Voice recording requires microphone. Suggestion: Music synthesizer with Tone.js for audio creation.
XPROMPTREJECTREASON: GPS location unavailable. Suggestion: Manual trip logger or distance calculator.
XPROMPTREJECTREASON: Accelerometer access unavailable. Suggestion: Tilt control game with touch/button controls.
XPROMPTREJECTREASON: QR scanning requires camera. Suggestion: QR code generator - create codes to share.
XPROMPTREJECTREASON: Contact access unavailable. Suggestion: Personal contact list app with manual entry (saved locally).
XPROMPTREJECTREASON: Live stock prices require API. Suggestion: Stock portfolio simulator with realistic price movements.
XPROMPTREJECTREASON: File upload unavailable. Suggestion: Text-based content creator with templates.

DO NOT REJECT:
- 3D apps (Three.js available)
- Charts (Chart.js with demo data)
- Audio synthesis (Tone.js for making sounds)
- Canvas/drawing (native HTML5)
- Games and simulations
- Text-based apps
- Calculators and tools
- Apps using Android.saveData or Android.setReminder

REQUIREMENTS:
✓ Complete working code (no TODOs/placeholders)
✓ All CSS inline in <style>
✓ All JS inline in <script>
✓ Beautiful, polished mobile UI
✓ Use realistic demo/simulated data when needed
✓ App works immediately without setup
✓ Only use available Android features (saveData, setReminder)

CRITICAL: Output ONLY the HTML. Do not wrap in markdown. Do not explain.

User request: %s"""

    fun generatePrompt(userInput: String): String {
        return PROP_FORMAT.format(userInput)
    }
}