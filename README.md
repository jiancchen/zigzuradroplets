# Droplets 💧

**AI-Powered HTML Generation for Android**

Droplets is an Android application that leverages Claude AI to generate beautiful, functional HTML content from natural language prompts. The app features a clean, intuitive interface with a WebView for real-time HTML preview and a comprehensive history system.

## Features

- **🤖 AI-Powered Generation**: Uses Claude AI to transform text prompts into complete HTML pages
- **📱 Mobile-First Design**: Native Android app built with Jetpack Compose
- **🔒 Secure API Key Storage**: Encrypted local storage using DataStore preferences
- **📝 Prompt History**: Save and revisit previous prompts and generated HTML
- **🌐 Real-time Preview**: Integrated WebView for immediate HTML rendering
- **🎨 Material Design 3**: Modern, responsive UI following Material Design guidelines

## Screenshots

*Coming soon - add screenshots of the app in action*

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK API level 28+
- Claude API key from Anthropic

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/droplets.git
   cd droplets
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Get your Claude API Key**
   - Visit [Anthropic's website](https://www.anthropic.com/)
   - Sign up for an account
   - Generate an API key from the dashboard
   - The key should start with `sk-`

5. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Enter your Claude API key when prompted

## Usage

### First Time Setup

1. **Splash Screen**: The app will check if you have an API key stored
2. **Signup Screen**: If no key is found, you'll be prompted to enter your Claude API key
3. **Main Screen**: Once authenticated, you can start generating HTML

### Generating HTML

1. **Enter a Prompt**: Type your request in the text field at the bottom
   - Example: "Create a beautiful landing page for a coffee shop"
   - Example: "Make a responsive portfolio website with dark theme"
   - Example: "Build a pricing table with three tiers"

2. **Generate**: Tap the send button to call the Claude API

3. **Preview**: The generated HTML will automatically load in the WebView

4. **History**: Access previous prompts using the history button in the top bar

### Example Prompts

- "Create a modern login form with glassmorphism effects"
- "Build a responsive navigation menu with hamburger icon"
- "Design a product showcase page for headphones"
- "Make a contact form with validation and animations"
- "Create a dashboard with charts and cards"

## Architecture

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Hilt dependency injection
- **Navigation**: Navigation Compose
- **HTTP Client**: Retrofit + OkHttp
- **Local Storage**: DataStore Preferences
- **AI Provider**: Claude API (Anthropic)

### Project Structure

```
app/src/main/java/com/zigzura/droplets/
├── api/                    # API interfaces and clients
│   ├── ApiClient.kt
│   └── ClaudeApiService.kt
├── data/                   # Data models and preferences
│   ├── PromptHistory.kt
│   └── PreferencesManager.kt
├── di/                     # Dependency injection modules
│   └── AppModule.kt
├── navigation/             # Navigation routes
│   └── Screen.kt
├── repository/             # Data repositories
│   └── ClaudeRepository.kt
├── ui/
│   ├── screens/           # Compose screens
│   │   ├── SplashScreen.kt
│   │   ├── SignupScreen.kt
│   │   └── MainScreen.kt
│   └── theme/             # Material Design theme
├── viewmodel/             # ViewModels
│   ├── MainViewModel.kt
│   └── SignupViewModel.kt
├── DropletsApplication.kt # Application class
├── MainActivity.kt        # Main activity
└── Weblet.kt             # WebView component
```

### Key Components

- **ClaudeRepository**: Handles API communication with Claude
- **PreferencesManager**: Manages encrypted local storage
- **Weblet**: Custom WebView component for HTML rendering
- **Hilt Modules**: Dependency injection setup

## API Integration

The app integrates with Claude AI using Anthropic's REST API:

- **Endpoint**: `https://api.anthropic.com/v1/messages`
- **Model**: `claude-3-haiku-20240307` (configurable)
- **Authentication**: Bearer token with your API key
- **Features**: Optimized prompts for HTML generation

## Security

- **API Key Storage**: Encrypted using Android DataStore
- **Local Data**: All prompt history stored locally on device
- **Network**: HTTPS-only communication with Claude API
- **Permissions**: Only requires INTERNET permission

## Configuration

### Customizing the AI Prompt

The app sends enhanced prompts to Claude for better HTML generation. You can modify the prompt template in `ClaudeRepository.kt`:

```kotlin
val enhancedPrompt = """
    Generate a complete HTML page based on this request: $prompt
    
    Requirements:
    - Create a full HTML document with proper structure
    - Include responsive CSS styling
    - Make it visually appealing
    - Ensure cross-browser compatibility
"""
```

### Changing the Claude Model

Update the model in `ClaudeRequest.kt`:

```kotlin
data class ClaudeRequest(
    val model: String = "claude-3-sonnet-20240229", // or other available models
    // ...
)
```

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Kotlin coding conventions
- Use Jetpack Compose best practices
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed

## Troubleshooting

### Common Issues

**API Key Issues**
- Ensure your key starts with `sk-`
- Check that your Anthropic account has credits
- Verify network connectivity

**Build Issues**
- Clean and rebuild: `./gradlew clean build`
- Invalidate caches in Android Studio
- Check Gradle sync

**WebView Not Loading**
- Check that HTML content is valid
- Ensure INTERNET permission is granted
- Clear app data and restart

## Roadmap

- [ ] Support for more AI providers (GPT, Gemini)
- [ ] Export HTML to files
- [ ] Share generated content
- [ ] Custom CSS themes
- [ ] Offline mode with cached responses
- [ ] Advanced prompt templates
- [ ] HTML editing capabilities
- [ ] Performance optimizations

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Anthropic](https://www.anthropic.com/) for the Claude AI API
- [Material Design](https://material.io/) for design guidelines
- [Jetpack Compose](https://developer.android.com/jetpack/compose) team for the UI toolkit

## Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/yourusername/droplets/issues) page
2. Create a new issue with detailed information
3. Join our community discussions

---

**Made with ❤️ for the Android development community**
