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

import { contextBridge, ipcRenderer } from 'electron';

contextBridge.exposeInMainWorld('electronAPI', {
    // File operations
    readFile: (filePath: string) => ipcRenderer.invoke('read-file', filePath),
    writeFile: (filePath: string, content: string) => ipcRenderer.invoke('write-file', filePath, content),
    fileExists: (filePath: string) => ipcRenderer.invoke('file-exists', filePath),

    // HTTP operations
    makeHttpRequest: (config: any) => ipcRenderer.invoke('make-http-request', config),

    // App operations
    getUserDataPath: () => ipcRenderer.invoke('get-user-data-path'),

    // Menu event listeners
    onMenuNewRequest: (callback: () => void) => {
        ipcRenderer.on('menu-new-request', callback);
    },
    onMenuNewGroup: (callback: () => void) => {
        ipcRenderer.on('menu-new-group', callback);
    },
    onMenuSave: (callback: () => void) => {
        ipcRenderer.on('menu-save', callback);
    },
    onMenuSaveAll: (callback: () => void) => {
        ipcRenderer.on('menu-save-all', callback);
    },
    onMenuProxySettings: (callback: () => void) => {
        ipcRenderer.on('menu-proxy-settings', callback);
    },
    onMenuAbout: (callback: () => void) => {
        ipcRenderer.on('menu-about', callback);
    }
});
