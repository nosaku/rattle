/*
 * Copyright (c) 2025 nosaku
 */

import { Component, Input, OnInit } from '@angular/core';
import { ApiModel, HttpResponse } from '../../models/app.models';
import { ApiService } from '../../services/api.service';
import { StorageService } from '../../services/storage.service';
import { APP_CONSTANTS } from '../../shared/constants';

@Component({
    selector: 'app-request-editor',
    templateUrl: './request-editor.component.html',
    styleUrls: ['./request-editor.component.scss']
})
export class RequestEditorComponent implements OnInit {
    @Input() apiModel!: ApiModel;

    httpMethods = APP_CONSTANTS.HTTP_METHODS;
    httpHeaders = APP_CONSTANTS.HTTP_HEADERS;

    paramsList: Array<{ key: string; value: string }> = [];
    headersList: Array<{ key: string; value: string }> = [];

    response: HttpResponse | null = null;
    responseBody: string = '';
    loading = false;

    constructor(
        private apiService: ApiService,
        private storageService: StorageService
    ) { }

    ngOnInit(): void {
        this.initializeParamsAndHeaders();

        this.apiService.loading$.subscribe(loading => {
            this.loading = loading;
        });
    }

    private initializeParamsAndHeaders(): void {
        if (this.apiModel.params) {
            this.paramsList = Object.entries(this.apiModel.params).map(([key, value]) => ({ key, value }));
        }

        if (this.apiModel.headers) {
            this.headersList = Object.entries(this.apiModel.headers).map(([key, value]) => ({ key, value }));
        }

        if (!this.paramsList.length) {
            this.addParam();
        }

        if (!this.headersList.length) {
            this.addHeader();
        }
    }

    addParam(): void {
        this.paramsList.push({ key: '', value: '' });
    }

    removeParam(index: number): void {
        this.paramsList.splice(index, 1);
        this.updateModel();
    }

    addHeader(): void {
        this.headersList.push({ key: '', value: '' });
    }

    removeHeader(index: number): void {
        this.headersList.splice(index, 1);
        this.updateModel();
    }

    updateModel(): void {
        const params: Record<string, string> = {};
        this.paramsList.forEach(p => {
            if (p.key) params[p.key] = p.value;
        });

        const headers: Record<string, string> = {};
        this.headersList.forEach(h => {
            if (h.key) headers[h.key] = h.value;
        });

        this.storageService.updateApiModel(this.apiModel.id, {
            ...this.apiModel,
            params,
            headers,
            isModified: true
        });
    }

    onFieldChange(): void {
        this.updateModel();
    }

    async sendRequest(): Promise<void> {
        try {
            this.updateModel();
            this.response = await this.apiService.executeRequest(this.apiModel);

            // Format response body
            if (typeof this.response.data === 'object') {
                this.responseBody = JSON.stringify(this.response.data, null, 2);
            } else {
                this.responseBody = String(this.response.data);
            }

            // Update model with response
            this.storageService.updateApiModel(this.apiModel.id, {
                response: this.responseBody,
                statusCode: this.response.status
            });
        } catch (error: any) {
            this.responseBody = `Error: ${error.message}`;
        }
    }

    formatJson(): void {
        if (this.apiModel.body) {
            try {
                const formatted = this.apiService.formatJson(this.apiModel.body);
                this.apiModel.body = formatted;
                this.updateModel();
            } catch (error) {
                alert('Invalid JSON');
            }
        }
    }

    getStatusClass(): string {
        if (!this.response) return '';

        const status = this.response.status;
        if (status >= 200 && status < 300) return 'success';
        if (status >= 300 && status < 400) return 'redirect';
        if (status >= 400 && status < 500) return 'client-error';
        if (status >= 500) return 'server-error';
        return '';
    }
}
