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
 */

import { Injectable } from '@angular/core';
import { ProxySettings } from '../models/app.models';

@Injectable({
    providedIn: 'root'
})
export class ProxyService {
    private proxySettings: ProxySettings = {
        proxyMode: 'OFF',
        verifySslCertificate: true
    };

    constructor() {
        this.loadProxySettings();
    }

    getProxySettings(): ProxySettings {
        return { ...this.proxySettings };
    }

    setProxySettings(settings: ProxySettings): void {
        this.proxySettings = settings;
        this.saveProxySettings();
    }

    private loadProxySettings(): void {
        const saved = localStorage.getItem('proxySettings');
        if (saved) {
            try {
                this.proxySettings = JSON.parse(saved);
            } catch (error) {
                console.error('Error loading proxy settings:', error);
            }
        }
    }

    private saveProxySettings(): void {
        try {
            localStorage.setItem('proxySettings', JSON.stringify(this.proxySettings));
        } catch (error) {
            console.error('Error saving proxy settings:', error);
        }
    }
}
