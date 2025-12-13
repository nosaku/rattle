/*
 * Copyright (c) 2025 nosaku
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { app, BrowserWindow, ipcMain, Menu } from 'electron';
import * as path from 'path';
import * as fs from 'fs';
import axios, { AxiosRequestConfig } from 'axios';
import { HttpsProxyAgent } from 'https-proxy-agent';

let mainWindow: BrowserWindow | null = null;

function createWindow(): void {
    mainWindow = new BrowserWindow({
        width: 1400,
        height: 900,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true,
            preload: path.join(__dirname, 'preload.js')
        },
        title: 'Rattle'
    });

    // In development mode, load from localhost
    if (process.env.NODE_ENV === 'development') {
        mainWindow.loadURL('http://localhost:4200');
        mainWindow.webContents.openDevTools();
    } else {
        mainWindow.loadFile(path.join(__dirname, '../angular/index.html'));
    }

    createMenu();

    mainWindow.on('closed', () => {
        mainWindow = null;
    });
}

function createMenu(): void {
    const template: Electron.MenuItemConstructorOptions[] = [
        {
            label: 'File',
            submenu: [
                {
                    label: 'New Request',
                    accelerator: 'CmdOrCtrl+T',
                    click: () => {
                        mainWindow?.webContents.send('menu-new-request');
                    }
                },
                {
                    label: 'New Group',
                    accelerator: 'CmdOrCtrl+G',
                    click: () => {
                        mainWindow?.webContents.send('menu-new-group');
                    }
                },
                { type: 'separator' },
                {
                    label: 'Save',
                    accelerator: 'CmdOrCtrl+S',
                    click: () => {
                        mainWindow?.webContents.send('menu-save');
                    }
                },
                {
                    label: 'Save All',
                    accelerator: 'CmdOrCtrl+Shift+S',
                    click: () => {
                        mainWindow?.webContents.send('menu-save-all');
                    }
                },
                { type: 'separator' },
                {
                    label: 'Exit',
                    accelerator: 'Alt+X',
                    click: () => {
                        app.quit();
                    }
                }
            ]
        },
        {
            label: 'Settings',
            submenu: [
                {
                    label: 'Proxy Settings',
                    click: () => {
                        mainWindow?.webContents.send('menu-proxy-settings');
                    }
                }
            ]
        },
        {
            label: 'Help',
            submenu: [
                {
                    label: 'About',
                    click: () => {
                        mainWindow?.webContents.send('menu-about');
                    }
                }
            ]
        }
    ];

    const menu = Menu.buildFromTemplate(template);
    Menu.setApplicationMenu(menu);
}

app.on('ready', createWindow);

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

app.on('activate', () => {
    if (mainWindow === null) {
        createWindow();
    }
});

// IPC Handlers

// File system operations
ipcMain.handle('read-file', async (event, filePath: string) => {
    try {
        const data = await fs.promises.readFile(filePath, 'utf-8');
        return { success: true, data };
    } catch (error: any) {
        return { success: false, error: error.message };
    }
});

ipcMain.handle('write-file', async (event, filePath: string, content: string) => {
    try {
        await fs.promises.writeFile(filePath, content, 'utf-8');
        return { success: true };
    } catch (error: any) {
        return { success: false, error: error.message };
    }
});

ipcMain.handle('file-exists', async (event, filePath: string) => {
    try {
        await fs.promises.access(filePath);
        return true;
    } catch {
        return false;
    }
});

// HTTP requests
ipcMain.handle('make-http-request', async (event, requestConfig: any) => {
    try {
        const { method, url, headers, body, params, proxySettings } = requestConfig;

        const config: AxiosRequestConfig = {
            method,
            url,
            headers,
            params,
            data: body,
            validateStatus: () => true, // Don't throw on any status
            maxRedirects: 5,
            timeout: 30000
        };

        // Configure proxy if needed
        if (proxySettings && proxySettings.proxyMode === 'ON' && proxySettings.httpProxy) {
            const proxyUrl = proxySettings.username && proxySettings.password
                ? `http://${proxySettings.username}:${proxySettings.password}@${proxySettings.httpProxy}`
                : `http://${proxySettings.httpProxy}`;

            config.httpsAgent = new HttpsProxyAgent(proxyUrl);
        }

        // Configure SSL verification
        if (proxySettings && !proxySettings.verifySslCertificate) {
            config.httpsAgent = new (require('https').Agent)({
                rejectUnauthorized: false
            });
        }

        const startTime = Date.now();
        const response = await axios(config);
        const endTime = Date.now();

        return {
            success: true,
            data: {
                status: response.status,
                statusText: response.statusText,
                headers: response.headers,
                data: response.data,
                duration: endTime - startTime
            }
        };
    } catch (error: any) {
        return {
            success: false,
            error: error.message,
            data: {
                status: error.response?.status || 0,
                statusText: error.response?.statusText || 'Error',
                headers: error.response?.headers || {},
                data: error.response?.data || error.message,
                duration: 0
            }
        };
    }
});

// Get user data path
ipcMain.handle('get-user-data-path', async () => {
    return app.getPath('userData');
});

console.log('Electron main process started');
