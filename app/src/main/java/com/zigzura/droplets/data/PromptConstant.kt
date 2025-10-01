package com.zigzura.droplets.data

object PromptConstant {

    const val PROMPT1 = """<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta
    name="viewport"
    content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"
  />
  <title>Mobile Calculator</title>
  <style>
    :root {
      --bg: #0f172a;            /* slate-900 */
      --panel: #111827;         /* gray-900 */
      --panel-top: #0b1020;
      --text: #e5e7eb;          /* gray-200 */
      --muted: #9ca3af;         /* gray-400 */
      --accent: #22c55e;        /* green-500 */
      --danger: #ef4444;        /* red-500 */
      --btn: #1f2937;           /* gray-800 */
      --btn-hover: #263241;
      --btn-primary: #3b82f6;   /* blue-500 */
      --shadow: 0 10px 30px rgba(0,0,0,.35);
      --radius: 22px;
    }

    html, body {
      height: 100%;
      margin: 0;
      background: radial-gradient(1200px 800px at 50% -20%, #182032 0%, var(--bg) 60%);
      color: var(--text);
      font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif;
    }

    /* Center the calculator */
    .wrap {
      min-height: 100%;
      display: grid;
      place-items: center;
      padding: 24px;
      box-sizing: border-box;
    }

    .calc {
      width: min(92vw, 380px);
      background: linear-gradient(180deg, var(--panel-top), var(--panel));
      border-radius: var(--radius);
      box-shadow: var(--shadow);
      overflow: hidden;
      display: grid;
      grid-template-rows: auto 1fr;
      border: 1px solid #1f2937;
    }

    .display {
      padding: 18px 20px 8px;
      text-align: right;
      background: transparent;
      display: grid;
      gap: 6px;
    }

    .history {
      min-height: 20px;
      color: var(--muted);
      font-size: 14px;
      line-height: 1.2;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .result {
      font-size: clamp(28px, 8vw, 40px);
      font-weight: 700;
      line-height: 1.2;
      letter-spacing: .5px;
      word-break: break-all;
      min-height: 44px;
    }

    .keys {
      padding: 16px;
      display: grid;
      gap: 10px;
      grid-template-columns: repeat(4, 1fr);
      background: transparent;
    }

    button {
      -webkit-tap-highlight-color: transparent;
      appearance: none;
      border: none;
      border-radius: 14px;
      padding: 16px 12px;
      font-size: 18px;
      font-weight: 600;
      color: var(--text);
      background: var(--btn);
      box-shadow: inset 0 -2px 0 rgba(255,255,255,.02);
      transition: transform .02s ease, background .15s ease, filter .15s ease;
      touch-action: manipulation;
    }
    button:active { transform: translateY(1px) scale(0.995); }
    button:hover { background: var(--btn-hover); }

    .op     { background: #23334a; }
    .op:hover { background: #2a3a52; }

    .primary { background: var(--btn-primary); color: white; }
    .primary:hover { filter: brightness(0.95); }

    .accent { background: var(--accent); color: #052e13; }
    .accent:hover { filter: brightness(0.95); }

    .danger { background: #3a1f22; color: #fecaca; }
    .danger:hover { background: #492429; }

    .span-2 { grid-column: span 2; }

    /* Prevent long-press text selection & callout */
    * {
      -webkit-user-select: none;
      user-select: none;
      -webkit-touch-callout: none;
    }

    /* Safe area on devices with notches */
    .calc { margin: env(safe-area-inset-top) auto env(safe-area-inset-bottom); }
  </style>
</head>
<body>
  <div class="wrap">
    <main class="calc" role="application" aria-label="Calculator">
      <section class="display" aria-live="polite" aria-atomic="true">
        <div class="history" id="history"></div>
        <div class="result" id="result">0</div>
      </section>

      <section class="keys">
        <button class="danger" data-action="clear">AC</button>
        <button class="accent" data-action="sign">±</button>
        <button class="op" data-action="percent">%</button>
        <button class="op" data-op="÷">÷</button>

        <button data-num="7">7</button>
        <button data-num="8">8</button>
        <button data-num="9">9</button>
        <button class="op" data-op="×">×</button>

        <button data-num="4">4</button>
        <button data-num="5">5</button>
        <button data-num="6">6</button>
        <button class="op" data-op="-">−</button>

        <button data-num="1">1</button>
        <button data-num="2">2</button>
        <button data-num="3">3</button>
        <button class="op" data-op="+">+</button>

        <button class="span-2" data-num="0">0</button>
        <button data-action="dot">.</button>
        <button class="primary" data-action="equals">=</button>
      </section>
    </main>
  </div>

  <script>
    (function () {
      const resultEl = document.getElementById('result');
      const historyEl = document.getElementById('history');
      const keys = document.querySelector('.keys');

      let current = '0';
      let stored = null;
      let op = null;
      let fresh = true; // ready for new number after op/equals

      const fmt = (v) => {
        if (!isFinite(v)) return 'Error';
        const s = String(v);
        if (s.includes('e')) return s;
        const [i, d = ''] = s.split('.');
        if (d.length > 10) return Number(v).toFixed(10).replace(/\.?0+${'$'}/, '');
        return s;
      };

      const render = () => {
        resultEl.textContent = current;
        const parts = [];
        if (stored !== null) parts.push(fmt(stored));
        if (op) parts.push(op);
        historyEl.textContent = parts.join(' ');
      };

      const setCurrent = (s) => {
        current = s.replace(/^0(?!\.)/, '0') === '' ? '0' : s;
        render();
      };

      const inputDigit = (d) => {
        if (fresh) {
          setCurrent(d);
          fresh = false;
        } else {
          if (current.length < 18) setCurrent(current === '0' ? d : current + d);
        }
      };

      const inputDot = () => {
        if (fresh) {
          setCurrent('0.');
          fresh = false;
        } else if (!current.includes('.')) {
          setCurrent(current + '.');
        }
      };

      const toggleSign = () => {
        if (current === '0') return;
        setCurrent(current.startsWith('-') ? current.slice(1) : '-' + current);
      };

      const percent = () => {
        const v = parseFloat(current || '0') / 100;
        setCurrent(fmt(v));
      };

      const clearAll = () => {
        current = '0';
        stored = null;
        op = null;
        fresh = true;
        render();
      };

      const applyOp = () => {
        if (stored === null || op === null) return;
        const a = parseFloat(stored);
        const b = parseFloat(current);
        let res = 0;
        switch (op) {
          case '+': res = a + b; break;
          case '-': res = a - b; break;
          case '×': res = a * b; break;
          case '÷': res = b === 0 ? Infinity : a / b; break;
        }
        current = fmt(res);
        stored = null;
        op = null;
        fresh = true;
        render();
      };

      const setOp = (nextOp) => {
        if (!fresh && op && stored !== null) {
          // If chaining operations, compute first
          applyOp();
        }
        if (stored === null) {
          stored = parseFloat(current);
          fresh = true;
        }
        op = nextOp;
        render();
      };

      keys.addEventListener('click', (e) => {
        const btn = e.target.closest('button');
        if (!btn) return;

        const num = btn.getAttribute('data-num');
        const action = btn.getAttribute('data-action');
        const operator = btn.getAttribute('data-op');

        if (num !== null) return inputDigit(num);
        if (action === 'dot') return inputDot();
        if (action === 'sign') return toggleSign();
        if (action === 'percent') return percent();
        if (action === 'clear') return clearAll();
        if (action === 'equals') return applyOp();
        if (operator) return setOp(operator);
      });

      // Optional: keyboard support (useful in emulator)
      window.addEventListener('keydown', (e) => {
        const k = e.key;
        if (/^[0-9]${'$'}/.test(k)) inputDigit(k);
        else if (k === '.') inputDot();
        else if (k === 'Enter' || k === '=') applyOp();
        else if (k === 'Escape') clearAll();
        else if (k === '+' || k === '-') setOp(k);
        else if (k === '*') setOp('×');
        else if (k === '/') setOp('÷');
      });

      render();
    })();
  </script>
</body>
</html>
"""

    const val PROMPT2 = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>3D Dice Roller</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #0f172a 0%, #581c87 50%, #0f172a 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        
        .header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        h1 {
            color: white;
            font-size: 2.5rem;
            margin-bottom: 10px;
        }
        
        .subtitle {
            color: #d8b4fe;
            font-size: 1.1rem;
        }
        
        #canvas-container {
            width: 100%;
            max-width: 800px;
            aspect-ratio: 16/9;
            max-height: 500px;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
            border: 4px solid #a855f7;
            margin-bottom: 30px;
        }
        
        #rollButton {
            padding: 16px 32px;
            font-size: 1.25rem;
            font-weight: bold;
            color: white;
            background: linear-gradient(135deg, #ec4899 0%, #9333ea 100%);
            border: none;
            border-radius: 50px;
            cursor: pointer;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
            transition: all 0.2s;
        }
        
        #rollButton:hover:not(:disabled) {
            transform: scale(1.05);
            background: linear-gradient(135deg, #db2777 0%, #7e22ce 100%);
        }
        
        #rollButton:active:not(:disabled) {
            transform: scale(0.95);
        }
        
        #rollButton:disabled {
            background: #6b7280;
            cursor: not-allowed;
        }
        
        #result {
            margin-top: 30px;
            text-align: center;
            animation: bounce 1s infinite;
        }
        
        .result-number {
            font-size: 4rem;
            font-weight: bold;
            background: linear-gradient(135deg, #f9a8d4 0%, #c084fc 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        
        .result-text {
            color: #d8b4fe;
            margin-top: 10px;
            font-size: 1.1rem;
        }
        
        @keyframes bounce {
            0%, 100% {
                transform: translateY(0);
            }
            50% {
                transform: translateY(-10px);
            }
        }
        
        .hidden {
            display: none;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>3D Dice Roller</h1>
        <p class="subtitle">Click the button to throw the dice!</p>
    </div>
    
    <div id="canvas-container"></div>
    
    <button id="rollButton">Roll Dice</button>
    
    <div id="result" class="hidden">
        <div class="result-number" id="resultNumber"></div>
        <p class="result-text">You rolled a <span id="resultText"></span>!</p>
    </div>

    <script>
        let scene, camera, renderer, dice;
        let isRolling = false;
        let rollState = null;
        let frameId = null;

        function init() {
            const container = document.getElementById('canvas-container');
            
            // Scene
            scene = new THREE.Scene();
            scene.background = new THREE.Color(0x1a1a2e);
            
            // Camera
            camera = new THREE.PerspectiveCamera(
                75,
                container.clientWidth / container.clientHeight,
                0.1,
                1000
            );
            camera.position.set(0, 3, 8);
            camera.lookAt(0, 0, 0);
            
            // Renderer
            renderer = new THREE.WebGLRenderer({ antialias: true });
            renderer.setSize(container.clientWidth, container.clientHeight);
            renderer.shadowMap.enabled = true;
            container.appendChild(renderer.domElement);
            
            // Lighting
            const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
            scene.add(ambientLight);
            
            const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
            directionalLight.position.set(5, 10, 5);
            directionalLight.castShadow = true;
            scene.add(directionalLight);
            
            const pointLight = new THREE.PointLight(0x00ff88, 0.5);
            pointLight.position.set(-5, 5, 2);
            scene.add(pointLight);
            
            // Ground
            const groundGeometry = new THREE.PlaneGeometry(20, 20);
            const groundMaterial = new THREE.MeshStandardMaterial({ 
                color: 0x2a2a4e,
                roughness: 0.8
            });
            const ground = new THREE.Mesh(groundGeometry, groundMaterial);
            ground.rotation.x = -Math.PI / 2;
            ground.position.y = -2;
            ground.receiveShadow = true;
            scene.add(ground);
            
            // Create dice
            const geometry = new THREE.BoxGeometry(2, 2, 2);
            const materials = [
                createDiceFace(1),
                createDiceFace(6),
                createDiceFace(2),
                createDiceFace(5),
                createDiceFace(3),
                createDiceFace(4),
            ];
            
            dice = new THREE.Mesh(geometry, materials);
            dice.castShadow = true;
            dice.position.set(0, 2, 0);
            scene.add(dice);
            
            // Handle resize
            window.addEventListener('resize', onWindowResize);
            
            // Start animation
            animate();
        }
        
        function createDiceFace(number) {
            const canvas = document.createElement('canvas');
            canvas.width = 256;
            canvas.height = 256;
            const ctx = canvas.getContext('2d');
            
            // Background
            ctx.fillStyle = '#ffffff';
            ctx.fillRect(0, 0, 256, 256);
            
            // Border
            ctx.strokeStyle = '#333333';
            ctx.lineWidth = 8;
            ctx.strokeRect(0, 0, 256, 256);
            
            // Dots
            ctx.fillStyle = '#ff0066';
            const dotRadius = 20;
            const positions = {
                1: [[128, 128]],
                2: [[64, 64], [192, 192]],
                3: [[64, 64], [128, 128], [192, 192]],
                4: [[64, 64], [192, 64], [64, 192], [192, 192]],
                5: [[64, 64], [192, 64], [128, 128], [64, 192], [192, 192]],
                6: [[64, 64], [192, 64], [64, 128], [192, 128], [64, 192], [192, 192]],
            };
            
            positions[number].forEach(([x, y]) => {
                ctx.beginPath();
                ctx.arc(x, y, dotRadius, 0, Math.PI * 2);
                ctx.fill();
            });
            
            const texture = new THREE.CanvasTexture(canvas);
            return new THREE.MeshStandardMaterial({ map: texture });
        }
        
        function animate() {
            frameId = requestAnimationFrame(animate);
            
            if (rollState && rollState.isActive) {
                // Physics update
                rollState.velocityY += rollState.gravity;
                
                dice.position.x += rollState.velocityX;
                dice.position.y += rollState.velocityY;
                dice.position.z += rollState.velocityZ;
                
                dice.rotation.x += rollState.angularVelocityX;
                dice.rotation.y += rollState.angularVelocityY;
                dice.rotation.z += rollState.angularVelocityZ;
                
                // Ground collision
                if (dice.position.y <= rollState.groundY) {
                    dice.position.y = rollState.groundY;
                    rollState.velocityY = -rollState.velocityY * rollState.restitution;
                    rollState.velocityX *= 0.95;
                    rollState.velocityZ *= 0.95;
                    rollState.angularVelocityX *= 0.8;
                    rollState.angularVelocityY *= 0.8;
                    rollState.angularVelocityZ *= 0.8;
                }
                
                // Damping
                rollState.velocityX *= 0.99;
                rollState.velocityZ *= 0.99;
                rollState.angularVelocityX *= 0.98;
                rollState.angularVelocityY *= 0.98;
                rollState.angularVelocityZ *= 0.98;
                
                rollState.elapsed++;
                
                // Check if settled
                const speed = Math.abs(rollState.velocityX) + Math.abs(rollState.velocityY) + Math.abs(rollState.velocityZ);
                const angularSpeed = Math.abs(rollState.angularVelocityX) + Math.abs(rollState.angularVelocityY) + Math.abs(rollState.angularVelocityZ);
                
                if (speed < 0.02 && angularSpeed < 0.02 && dice.position.y <= rollState.groundY + 0.1 && rollState.elapsed > 60) {
                    rollState.isActive = false;
                    rollState.settling = true;
                    rollState.settleProgress = 0;
                    rollState.startRotation = {
                        x: dice.rotation.x,
                        y: dice.rotation.y,
                        z: dice.rotation.z
                    };
                }
            } else if (rollState && rollState.settling) {
                rollState.settleProgress += 0.08;
                
                const eased = 1 - Math.pow(1 - rollState.settleProgress, 3);
                dice.rotation.x = rollState.startRotation.x + (rollState.targetRotation.x - rollState.startRotation.x) * eased;
                dice.rotation.y = rollState.startRotation.y + (rollState.targetRotation.y - rollState.startRotation.y) * eased;
                dice.rotation.z = rollState.startRotation.z + (rollState.targetRotation.z - rollState.startRotation.z) * eased;
                
                if (rollState.settleProgress >= 1) {
                    dice.rotation.set(rollState.targetRotation.x, rollState.targetRotation.y, rollState.targetRotation.z);
                    showResult(rollState.finalResult);
                    isRolling = false;
                    rollState = null;
                    document.getElementById('rollButton').disabled = false;
                    document.getElementById('rollButton').textContent = 'Roll Dice';
                }
            } else if (!isRolling && !rollState) {
                // Idle rotation (only before first roll)
                dice.rotation.x += 0.005;
                dice.rotation.y += 0.005;
            }
            
            renderer.render(scene, camera);
        }
        
        function rollDice() {
            if (isRolling) return;
            
            isRolling = true;
            document.getElementById('rollButton').disabled = true;
            document.getElementById('rollButton').textContent = 'Rolling...';
            document.getElementById('result').classList.add('hidden');
            
            const finalResult = Math.floor(Math.random() * 6) + 1;
            
            // Reset position
            dice.position.set(
                (Math.random() - 0.5) * 3,
                5,
                (Math.random() - 0.5) * 3
            );
            dice.rotation.set(
                Math.random() * Math.PI * 2,
                Math.random() * Math.PI * 2,
                Math.random() * Math.PI * 2
            );
            
            const rotations = {
                1: { x: 0, y: 0, z: 0 },
                2: { x: 0, y: Math.PI / 2, z: 0 },
                3: { x: 0, y: 0, z: -Math.PI / 2 },
                4: { x: 0, y: 0, z: Math.PI / 2 },
                5: { x: -Math.PI / 2, y: 0, z: 0 },
                6: { x: Math.PI, y: 0, z: 0 },
            };
            
            rollState = {
                isActive: true,
                settling: false,
                velocityX: (Math.random() - 0.5) * 0.3,
                velocityY: 0,
                velocityZ: (Math.random() - 0.5) * 0.3,
                angularVelocityX: (Math.random() - 0.5) * 0.4,
                angularVelocityY: (Math.random() - 0.5) * 0.4,
                angularVelocityZ: (Math.random() - 0.5) * 0.4,
                gravity: -0.025,
                groundY: -1,
                restitution: 0.5,
                elapsed: 0,
                finalResult: finalResult,
                targetRotation: rotations[finalResult]
            };
        }
        
        function showResult(number) {
            document.getElementById('resultNumber').textContent = number;
            document.getElementById('resultText').textContent = number;
            document.getElementById('result').classList.remove('hidden');
        }
        
        function onWindowResize() {
            const container = document.getElementById('canvas-container');
            camera.aspect = container.clientWidth / container.clientHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(container.clientWidth, container.clientHeight);
        }
        
        // Initialize when page loads
        window.addEventListener('load', init);
        
        // Button click handler
        document.getElementById('rollButton').addEventListener('click', rollDice);
    </script>
</body>
</html>"""
}