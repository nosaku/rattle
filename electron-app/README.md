# Rattle - Electron + Angular

A modern API testing tool built with Electron and Angular, converted from the original JavaFX application.

## Features

- ✅ Create and organize API requests in groups
- ✅ Support for multiple HTTP methods (GET, POST, PUT, DELETE, PATCH, etc.)
- ✅ Query parameters and headers management
- ✅ Request body editor with JSON formatting
- ✅ Response viewer with status codes and timing
- ✅ Tab-based interface for multiple requests
- ✅ Proxy settings support
- ✅ SSL certificate verification options
- ✅ Persistent storage of requests and groups
- ✅ Tree-view sidebar for easy navigation

## Requirements

- Node.js 18+ 
- npm or yarn

## Installation

```bash
cd electron-app
npm install
```

## Development

Run the application in development mode:

```bash
npm start
```

This will:
1. Start the Angular development server on http://localhost:4200
2. Compile the Electron main process
3. Launch the Electron application

## Building

Build the application for production:

```bash
# Build Angular and Electron
npm run build

# Package for current platform
npm run package

# Package for all platforms (Windows, Mac, Linux)
npm run package:all
```

The packaged application will be in the `release` folder.

## Project Structure

```
electron-app/
├── electron/           # Electron main process
│   ├── main.ts        # Main process entry point
│   └── preload.ts     # Preload script for IPC
├── src/               # Angular application
│   ├── app/
│   │   ├── components/    # UI components
│   │   ├── models/        # TypeScript interfaces
│   │   ├── services/      # Business logic services
│   │   └── shared/        # Shared utilities
│   ├── index.html
│   ├── main.ts
│   └── styles.scss
├── angular.json       # Angular configuration
├── package.json       # Dependencies and scripts
└── tsconfig.json      # TypeScript configuration
```

## Features Comparison with JavaFX Version

| Feature | JavaFX | Electron + Angular |
|---------|--------|-------------------|
| Cross-platform | ✅ | ✅ |
| Native look & feel | ✅ | ✅ |
| Modern UI | ⚠️ | ✅ |
| Web technologies | ❌ | ✅ |
| Easy updates | ⚠️ | ✅ |
| Bundle size | Medium | Larger |

## License

MIT License - Copyright (c) 2025 nosaku

## Development Notes

### Key Technologies

- **Electron**: Desktop application framework
- **Angular 17**: Frontend framework
- **TypeScript**: Type-safe development
- **SCSS**: Styling
- **Axios**: HTTP client (via Electron main process)

### Architecture

The application uses Electron's IPC (Inter-Process Communication) to handle HTTP requests securely from the main process, while the Angular frontend manages the UI and state.

### Data Storage

Requests and groups are stored in a JSON file located in the user's data directory:
- Windows: `%APPDATA%/rattle-electron/rattle.json`
- macOS: `~/Library/Application Support/rattle-electron/rattle.json`
- Linux: `~/.config/rattle-electron/rattle.json`
