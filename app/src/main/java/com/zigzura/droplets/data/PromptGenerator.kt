package com.zigzura.droplets.data

object PromptGenerator {

    private val PROP_FORMAT =
        """Generate single-file HTML app for Android WebView. Vanilla JS, NO frameworks (React/Vue/etc).

OUTPUT FORMAT:
Respond with ONLY the complete HTML code. No explanations, no markdown code blocks, no commentary.
Start with <!DOCTYPE html> and end with </html>. Nothing before or after.

REQUIRED META TAG:
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

MOBILE DESIGN:
- Center content for portrait phones (360-400px width typical)
- Body background: transparent (background: transparent;)
- Container positioning: margin-top: 40px for top spacing
- Container height strategy:
  * For static apps (calculators, forms): Center vertically with auto height
  * For dynamic list apps (todos, notes, logs): min-height: calc(100vh - 80px) to fill screen
- Use min-height: 100vh (NOT height: 100vh) to prevent viewport cutoff
- For body/container: flex with min-height allows proper scrolling
- Container width: 100% (padding handled by native Android container)
- Use flexbox/grid for layout
- Font size ≥16px (prevents zoom on input)
- Touch targets ≥44px
- Responsive padding: padding:20px
- Maintain consistent spacing/margins between elements  
- ALL elements must use box-sizing: border-box to include padding in width calculations
- Input fields, buttons must not exceed container width
- Use word-wrap: break-word for text content to prevent horizontal overflow
- Add to global CSS: * { box-sizing: border-box; }

CONTAINER HEIGHT RULES:
- If app has dynamic/expandable content (lists that grow: todos, notes, feeds, logs, chats, trackers):
  Container must be full-height from start: min-height: calc(100vh - 80px);
  Add overflow-y: auto; so content scrolls inside the fixed container
- If app has static/fixed content (calculators, converters, single forms):
  Container uses auto height and centers vertically (no forced full-height)
- Key principle: Don't let container grow with content - either full-height or auto, never dynamic

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
✓ IMPORTANT: Android.getAllData() returns an object with ALL keys, not just one value
✓ To load a specific key: use Android.loadData(key) NOT Android.getAllData()
✓ Correct pattern for loading:
  const stored = Android.loadData('tasks');
  if (stored) {
    tasks = JSON.parse(stored);
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

Design Language: Neo-Brutalist Peach
Default to Neo-Brutalist Peach unless user specifies otherwise.
COLORS:
- Primary gradient: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 50%, #ff9ff3 100%)
- Accent green: linear-gradient(135deg, #00b894, #00cec9)
- Accent coral: linear-gradient(135deg, #ff7675, #fd79a8)
- Accent mint: linear-gradient(135deg, #55efc4, #81ecec)
- Text dark: #2d3436
- Muted text: #636e72
- Completed bg: #dfe6e9

CONTAINER:
- Body: background: transparent;
- Container: width: 100%;
  background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 50%, #ff9ff3 100%);
  border-radius: 32px;
  padding: 32px 24px;
  
TYPOGRAPHY:
- Headers: font-weight: 900; text-transform: uppercase; letter-spacing: 2px; color: #2d3436;
- Body text: font-weight: 700; color: #2d3436;
- Subtitles: font-weight: 600; letter-spacing: 1px; opacity: 0.6;

BUTTONS & INPUTS:
- All elements: border: 4px solid #2d3436; border-radius: 24px;
- Buttons: Gradient backgrounds, chunky shadows: box-shadow: 5px 5px 0 #2d3436;
- Hover: transform: translate(-2px, -2px); box-shadow: 7px 7px 0 #2d3436;
- Active/Press: transform: translate(2px, 2px); box-shadow: 3px 3px 0 #2d3436;
- Input focus: transform: translateY(-2px); box-shadow: 6px 6px 0 rgba(45,52,54,0.2);
- Touch targets: minimum 44px height

CARDS/ITEMS:
- Background: white; border: 4px solid #2d3436; border-radius: 24px;
- Shadow: box-shadow: 5px 5px 0 rgba(45,52,54,0.3);
- Hover: lift effect with translate(-2px, -2px) and stronger shadow
- Completed state: background: #dfe6e9; border-color: #95a5a6; text-decoration: line-through;

INTERACTIONS:
- All transitions: 0.15-0.2s
- Hover effects: lift elements up with translate
- Press effects: push down with translate
- Use transform (NOT margin/position) for animations

STYLE KEYWORDS:
Neo-brutalist, playful, chunky borders, hard shadows, gradient accents, warm peach tones, tactile, energetic, bold typography, high contrast

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

APPS THAT WORK (generate anything like these):
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

NAVIGATION CONSTRAINTS:
- Single-page apps only - no multi-page navigation or routing
- Use modals, overlays, or show/hide sections for secondary content
- Avoid apps requiring separate screens or tab navigation between views
- All functionality must be accessible from a single scrolling page

FUNCTIONAL REQUIREMENTS:
- Apps with lists/collections MUST include full functionality: add, edit (if applicable), delete, persist with Android.saveData
- Never generate placeholder buttons or incomplete features
- All interactive elements must be fully functional on first load
- Data must persist across app restarts using Android.saveData/loadData

CSS FOR DYNAMIC LIST APPS:
Container must use:
  width: 100%;
  min-height: calc(100vh - 80px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;

This ensures todos, notes, trackers fill viewport height immediately.

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
- ALL elements must use box-sizing: border-box to include padding in width calculations
- Input fields, buttons must not exceed container width
- Use word-wrap: break-word for text content to prevent horizontal overflow
- Add to global CSS: * { box-sizing: border-box; }

CRITICAL: Output ONLY the HTML. Do not wrap in markdown. Do not explain.

User request: """

    private val PROP_FORMAT2 =
        """Generate single-file HTML app for Android WebView. Vanilla JS, NO frameworks (React/Vue/etc).
OUTPUT FORMAT:
Respond with ONLY the complete HTML code. No explanations, no markdown code blocks, no commentary.
Start with <!DOCTYPE html> and end with </html>. Nothing before or after.
REQUIRED META TAG:
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
=== LAYOUT SYSTEM ===
CORE LAYOUT RULES (ALWAYS FOLLOW):

html, body: height: 100%; overflow: hidden; margin: 0; padding: 0; background: transparent;
.container: height: 100%; overflow: hidden; width: 100%; display: flex; flex-direction: column;
Main page NEVER scrolls - only isolated child divs scroll
Use 100% for heights (NOT 100vh) - respects native Android container padding

LAYOUT PATTERNS:
Pattern A - Static Apps (calculators, forms, converters):
.container {
height: 100%;
overflow: hidden;
display: flex;
align-items: center;
justify-content: center;
}
.content {
/* auto height, centered */
}
Pattern B - Dynamic List Apps (todos, notes, trackers, feeds):
.container {
height: 100%;
overflow: hidden;
display: flex;
flex-direction: column;
}
.header {
/* fixed header /
}
.list-container {
flex: 1;
overflow-y: auto;
/ ONLY this scrolls /
}
.footer {
/ fixed footer */
}
MOBILE DESIGN:

Portrait phones: 360-400px width typical
Font size ≥16px (prevents zoom on input)
Touch targets ≥44px minimum
Padding: 20px on containers
Box model: * { box-sizing: border-box; }
Text wrapping: word-wrap: break-word;
No elements exceed container width

=== STYLING ===
Design Language: Neo-Brutalist Peach (default unless user specifies otherwise)
COLORS:

Primary gradient: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 50%, #ff9ff3 100%)
Accent green: linear-gradient(135deg, #00b894, #00cec9)
Accent coral: linear-gradient(135deg, #ff7675, #fd79a8)
Accent mint: linear-gradient(135deg, #55efc4, #81ecec)
Text dark: #2d3436
Muted text: #636e72
Completed bg: #dfe6e9

CONTAINER STYLING:

Background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 50%, #ff9ff3 100%)
Border-radius: 32px
Padding: 32px 24px

TYPOGRAPHY:

Headers: font-weight: 900; text-transform: uppercase; letter-spacing: 2px; color: #2d3436;
Body: font-weight: 700; color: #2d3436;
Subtitles: font-weight: 600; letter-spacing: 1px; opacity: 0.6;

BUTTONS & INPUTS:

Border: 4px solid #2d3436; border-radius: 24px;
Buttons: Gradient backgrounds, box-shadow: 5px 5px 0 #2d3436;
Hover: transform: translate(-2px, -2px); box-shadow: 7px 7px 0 #2d3436;
Active: transform: translate(2px, 2px); box-shadow: 3px 3px 0 #2d3436;
Input focus: transform: translateY(-2px); box-shadow: 6px 6px 0 rgba(45,52,54,0.2);
Min height: 44px

CARDS/ITEMS:

Background: white; border: 4px solid #2d3436; border-radius: 24px;
Shadow: box-shadow: 5px 5px 0 rgba(45,52,54,0.3);
Hover: translate(-2px, -2px) + stronger shadow
Completed: background: #dfe6e9; border-color: #95a5a6; text-decoration: line-through;

INTERACTIONS:

Transitions: 0.15-0.2s
Use transform for animations (NOT margin/position)
Hover: lift up, Active: push down

ALTERNATIVE STYLES:

User says "modern/sleek/professional/dark" → Use V2 style
User says "fun/colorful/playful/bright" → Use V1 style
No style specified → Use Neo-Brutalist Peach (default)

=== ANDROID FEATURES ===
AVAILABLE (USE THESE):
✓ Storage:
Android.saveData(key, value) - save string
Android.loadData(key) - load string
Android.getAllData() - get all keys object
Android.deleteData(key) - delete key
✓ Reminders:
Android.setReminder(id, milliseconds, title, message)
Android.cancelReminder(id)
Android.getReminders() - get all reminders
JAVASCRIPT PATTERNS:

Always check: if(typeof Android !== 'undefined') {...}
Load specific key: const data = Android.loadData('mykey');
Parse after loading: if(data) { items = JSON.parse(data); }
Save with JSON: Android.saveData('mykey', JSON.stringify(items));
Track keys used for later deletion

UNAVAILABLE (MUST REJECT):
✗ Camera/Photos/File uploads
✗ Microphone/Audio recording
✗ Geolocation/GPS
✗ Device sensors (accelerometer, gyroscope)
✗ Contacts/Calendar/SMS/Phone
✗ File system access
✗ Bluetooth/NFC
✗ Push notifications (use Android.setReminder instead)
✗ Biometrics
✗ Vibration
✗ Share API
✗ Clipboard read
✗ localStorage/sessionStorage (use Android.saveData)
✗ Network requests (fetch/XMLHttpRequest/WebSocket)
✗ Live external data
✗ Service Workers
=== LIBRARIES ===
ALLOWED CDN IMPORTS:
✓ Three.js: https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js
✓ Chart.js: https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.9.1/chart.min.js
✓ D3.js: https://cdnjs.cloudflare.com/ajax/libs/d3/7.8.5/d3.min.js
✓ Tone.js: https://cdnjs.cloudflare.com/ajax/libs/tone/14.8.49/Tone.js
✓ Math.js: https://cdnjs.cloudflare.com/ajax/libs/mathjs/11.11.0/math.min.js
✓ Bootstrap CSS: https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css
✓ Google Fonts: https://fonts.googleapis.com
=== APP TYPES ===
APPS THAT WORK:
✓ 3D graphics/games (Three.js)
✓ Charts with demo data (Chart.js)
✓ Canvas drawing/painting
✓ Audio synthesis (Tone.js - no recording)
✓ Calculators/converters/utilities
✓ Timers/stopwatches/alarms
✓ Notes/todos/trackers (with Android.saveData)
✓ Offline games (tic-tac-toe, snake, puzzles)
✓ Random generators
✓ Simulators with demo/fake data
✓ Text editors
✓ Animations/particle effects
✓ Math/science visualizations
✓ Color pickers/pixel art editors
APPS THAT DON'T WORK (with alternatives):
✗ Photo editor → Suggest: Drawing app or color filter simulator
✗ QR/Barcode scanner → Suggest: QR/Barcode generator
✗ Voice recorder → Suggest: Audio synthesizer (Tone.js)
✗ GPS tracker → Suggest: Compass simulator or distance calculator
✗ Step counter → Suggest: Manual activity logger
✗ Camera app → Suggest: Drawing app with brush tools
✗ File manager → Suggest: Text document viewer (in-app only)
✗ Live stocks/weather → Suggest: Stock/weather simulator with demo data
✗ Social media with uploads → Suggest: Text-based post designer
✗ Device music player → Suggest: Music synthesizer
REJECTION PROTOCOL:
If unavailable features requested, respond with ONLY:
XPROMPTREJECTREASON: [Feature] requires [capability]. Suggestion: [alternative].
Example:
XPROMPTREJECTREASON: Camera access unavailable. Suggestion: Drawing app with brush tools and color palette.
=== REQUIREMENTS ===
FUNCTIONAL:
✓ Complete working code (no TODOs/placeholders)
✓ All features fully functional on first load
✓ Apps with lists MUST have: add, edit (if applicable), delete, persist
✓ Data persists with Android.saveData/loadData
✓ Single-page only (use modals/overlays for secondary content)
✓ Use realistic demo data when needed
TECHNICAL:
✓ All CSS inline in <style>
✓ All JS inline in <script>
✓ Box-sizing: border-box on all elements
✓ No horizontal overflow
✓ Beautiful, polished mobile UI
✓ Works immediately without setup
CRITICAL: Output ONLY the HTML. Do not wrap in markdown. Do not explain.
User request: """

    private val PROP_FORMAT3 =
        try {
            """Generate single-file HTML app for Android WebView. Vanilla JS, NO frameworks (React/Vue/etc).
    
    OUTPUT FORMAT:
    Respond with ONLY the complete HTML code. No explanations, no markdown code blocks, no commentary.
    Start with <!DOCTYPE html> and end with </html>. Nothing before or after.
    
    REQUIRED META TAG:
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    
    MOBILE DESIGN:
    - Center content for portrait phones (360-400px width typical)
    - Body background: transparent (background: transparent;)
    - Container positioning: margin-top: 40px for top spacing
    - Container height strategy:
      * For static apps (calculators, forms): Center vertically with auto height
      * For dynamic list apps (todos, notes, logs): min-height: calc(100vh - 80px) to fill screen
    - Use min-height: 100vh (NOT height: 100vh) to prevent viewport cutoff
    - For body/container: flex with min-height allows proper scrolling
    - Container width: 100% (padding handled by native Android container)
    - Use flexbox/grid for layout
    - Font size ≥16px (prevents zoom on input)
    - Touch targets ≥44px
    - Responsive padding: padding:20px
    - Maintain consistent spacing/margins between elements  
    - ALL elements must use box-sizing: border-box to include padding in width calculations
    - Input fields, buttons must not exceed container width
    - Use word-wrap: break-word for text content to prevent horizontal overflow
    - Add to global CSS: * { box-sizing: border-box; }
    
    CONTAINER HEIGHT RULES:
    - If app has dynamic/expandable content (lists that grow: todos, notes, feeds, logs, chats, trackers):
      Container must be full-height from start: min-height: calc(100vh - 80px);
      Add overflow-y: auto; so content scrolls inside the fixed container
    - If app has static/fixed content (calculators, converters, single forms):
      Container uses auto height and centers vertically (no forced full-height)
    - Key principle: Don't let container grow with content - either full-height or auto, never dynamic
    
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
    ✓ IMPORTANT: Android.getAllData() returns an object with ALL keys, not just one value
    ✓ To load a specific key: use Android.loadData(key) NOT Android.getAllData()
    ✓ Correct pattern for loading:
      const stored = Android.loadData('tasks');
      if (stored) {
        tasks = JSON.parse(stored);
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
    
    Design Language:
    Use a layered 3D card design language. Each UI element should feel like a floating card with soft shadows, rounded corners, and depth.
    Use bold typography (Inter, Poppins, or Manrope).
    Backgrounds are flat and vibrant, e.g. yellow, teal, or coral.
    Foreground cards use black or white surfaces with subtle inner shadows.
    Accent elements use gradient highlights (like pink–orange, purple–blue).
    Keep spacing generous, corners round, and UI components centered.
    The style should feel playful yet technical, like a mix of code cards and album covers.
    
    Refernce Design: 
      body {
          background: #f7d441;
          display: flex;
          justify-content: center;
          align-items: center;
          min-height: 100vh;
          font-family: "Inter", sans-serif;
        }
    
        .stack {
          position: relative;
          width: 320px;
          height: 440px;
          transform-style: preserve-3d;
        }
    
        .card {
          position: absolute;
          width: 100%;
          height: 100%;
          border-radius: 24px;
          box-shadow: 0 25px 60px rgba(0,0,0,0.25);
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          transition: transform 0.3s ease;
        }
    
        .card:nth-child(1) {
          background: #111;
          color: #fff;
          transform: translateZ(0);
        }
    
        .card:nth-child(2) {
          background: #fff;
          color: #222;
          transform: translateY(20px) translateZ(-40px);
        }
    
        .card:nth-child(3) {
          background: #111;
          transform: translateY(40px) translateZ(-80px);
        }
    
        .display {
          width: 85%;
          height: 60px;
          background: rgba(255,255,255,0.1);
          border-radius: 12px;
          text-align: right;
          padding: 16px;
          font-size: 1.6rem;
          box-shadow: inset 0 0 10px rgba(255,255,255,0.1);
        }
    
        .buttons {
          margin-top: 20px;
          width: 85%;
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 12px;
        }
    
        button {
          height: 60px;
          border: none;
          border-radius: 12px;
          font-size: 1.2rem;
          font-weight: 600;
          cursor: pointer;
          background: linear-gradient(145deg, #1b1b1b, #292929);
          color: white;
          box-shadow: 0 6px 14px rgba(0, 0, 0, 0.3);
          transition: all 0.15s ease;
        }
    
        button:active {
          transform: scale(0.96);
          box-shadow: 0 2px 4px rgba(0,0,0,0.4);
        }
    
        .accent {
          background: linear-gradient(145deg, #f43f5e, #ec4899);
        }
    
    INTERACTIONS:
    - All transitions: 0.15-0.2s
    - Hover effects: lift elements up with translate
    - Press effects: push down with translate
    - Use transform (NOT margin/position) for animations
    - webkit-tap-highlight-color: transparent; for all elements
    
    STYLE KEYWORDS:
    Neo-brutalist, playful, chunky borders, hard shadows, gradient accents, warm peach tones, tactile, energetic, bold typography, high contrast
    
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
    
    APPS THAT WORK (generate anything like these):
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
    
    NAVIGATION CONSTRAINTS:
    - Single-page apps only - no multi-page navigation or routing
    - Use modals, overlays, or show/hide sections for secondary content
    - Avoid apps requiring separate screens or tab navigation between views
    - All functionality must be accessible from a single scrolling page
    
    FUNCTIONAL REQUIREMENTS:
    - Apps with lists/collections MUST include full functionality: add, edit (if applicable), delete, persist with Android.saveData
    - Never generate placeholder buttons or incomplete features
    - All interactive elements must be fully functional on first load
    - Data must persist across app restarts using Android.saveData/loadData
    
    CSS FOR DYNAMIC LIST APPS:
    Container must use:
      width: 100%;
      min-height: calc(100vh - 80px);
      overflow-y: auto;
      display: flex;
      flex-direction: column;
    
    This ensures todos, notes, trackers fill viewport height immediately.
    
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
    - ALL elements must use box-sizing: border-box to include padding in width calculations
    - Input fields, buttons must not exceed container width
    - Use word-wrap: break-word for text content to prevent horizontal overflow
    - Add to global CSS: * { box-sizing: border-box; }
    
    CRITICAL: Output ONLY the HTML. Do not wrap in markdown. Do not explain.
    
    User request: """
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }


    fun generatePrompt(userInput: String, style: String? = PromptStyles.DEFAULT): String {
        return PROP_FORMAT3 + userInput
    }
}