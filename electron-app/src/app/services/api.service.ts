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
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiModel, HttpResponse } from '../models/app.models';
import { ProxyService } from './proxy.service';

declare global {
    interface Window {
        electronAPI: any;
    }
}

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private loadingSubject = new BehaviorSubject<boolean>(false);
    public loading$ = this.loadingSubject.asObservable();

    constructor(private proxyService: ProxyService) { }

    async executeRequest(apiModel: ApiModel): Promise<HttpResponse> {
        this.loadingSubject.next(true);

        try {
            const requestConfig = {
                method: apiModel.method,
                url: apiModel.url,
                headers: apiModel.headers || {},
                params: apiModel.params || {},
                body: apiModel.body,
                proxySettings: this.proxyService.getProxySettings()
            };

            const result = await window.electronAPI.makeHttpRequest(requestConfig);

            this.loadingSubject.next(false);

            if (result.success) {
                return result.data;
            } else {
                throw new Error(result.error);
            }
        } catch (error: any) {
            this.loadingSubject.next(false);
            throw error;
        }
    }

    formatJson(json: string): string {
        try {
            const parsed = JSON.parse(json);
            return JSON.stringify(parsed, null, 2);
        } catch {
            return json;
        }
    }

    isValidJson(str: string): boolean {
        try {
            JSON.parse(str);
            return true;
        } catch {
            return false;
        }
    }

    buildUrl(baseUrl: string, params?: Record<string, string>): string {
        if (!params || Object.keys(params).length === 0) {
            return baseUrl;
        }

        const url = new URL(baseUrl);
        Object.entries(params).forEach(([key, value]) => {
            url.searchParams.append(key, value);
        });

        return url.toString();
    }

    parseHeaders(headersString: string): Record<string, string> {
        const headers: Record<string, string> = {};

        if (!headersString) return headers;

        headersString.split('\n').forEach(line => {
            const [key, ...valueParts] = line.split(':');
            if (key && valueParts.length > 0) {
                headers[key.trim()] = valueParts.join(':').trim();
            }
        });

        return headers;
    }

    formatHeaders(headers: Record<string, string>): string {
        return Object.entries(headers)
            .map(([key, value]) => `${key}: ${value}`)
            .join('\n');
    }
}
