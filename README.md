# ✨ Giphy Inserter

A sleek, lightweight desktop application built with **Compose for Desktop** that helps you quickly find and copy Giphy animations to your clipboard.

![img.png](./docs/img.png)

## 🚀 Features

- **Quick Search**: Effortlessly search for GIFs using the Giphy API.
- **Random Discovery**: Discover new GIFs with a single shortcut.
- **Copy to Clipboard**: Instant copy functionality to use GIFs anywhere.
- **Keyboard Shortcuts**: Designed for power users who love speed.
- **Minimalist UI**: A clean, transparent, and undecorated interface that stays out of your way.
- **System Tray Integration**: Keep it running in the background and access it quickly when needed.

## ⌨️ Shortcuts

- `Enter` / `Down Arrow`: Fetch next GIF for the current search.
- `Up Arrow`: Fetch previous GIF.
- `Cmd + C` (macOS): Copy current GIF to clipboard (as GIF file, URL, and HTML).
- `Cmd + R` (macOS): Fetch a random GIF.
- `Cmd + ,` (macOS): Open Settings.
- `Esc`: Exit application.

*Note: For Windows/Linux, use `Ctrl` instead of `Cmd`.*

## ⚙️ Configuration

To use the application, you need a Giphy API Key:
1. Get your free API key from the [Giphy Developers Portal](https://developers.giphy.com/).
2. Open the application.
3. Open **Settings** (`Cmd + ,`).
4. Enter your API Key and click **Save**.

## 🛠 Development

### Prerequisites
- JDK 11 or higher.

### Running the App
```bash
./gradlew run
```

### Building Native Distributions
You can build native distributions for your operating system:
- **macOS**: `./gradlew packageDmg`
- **Windows**: `./gradlew packageMsi`
- **Linux**: `./gradlew packageDeb`

Native installers will be generated in `build/compose/binaries`.

---
*Built with ❤️ using Compose for Desktop.*
