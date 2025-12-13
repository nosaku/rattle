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

import { Component, OnInit } from '@angular/core';
import { StorageService } from './services/storage.service';
import { ApiModel, ApiGroup } from './models/app.models';
import { APP_CONSTANTS } from './shared/constants';

declare global {
    interface Window {
        electronAPI: any;
    }
}

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    title = APP_CONSTANTS.APP_TITLE;
    copyright = APP_CONSTANTS.COPYRIGHT;

    openTabs: ApiModel[] = [];
    activeTab: ApiModel | null = null;

    constructor(private storageService: StorageService) { }

    ngOnInit(): void {
        this.setupMenuListeners();
    }

    private setupMenuListeners(): void {
        if (window.electronAPI) {
            window.electronAPI.onMenuNewRequest(() => this.createNewRequest());
            window.electronAPI.onMenuNewGroup(() => this.createNewGroup());
            window.electronAPI.onMenuSave(() => this.save());
            window.electronAPI.onMenuSaveAll(() => this.saveAll());
            window.electronAPI.onMenuProxySettings(() => this.openProxySettings());
            window.electronAPI.onMenuAbout(() => this.showAbout());
        }
    }

    onTabOpened(apiModel: ApiModel): void {
        const existing = this.openTabs.find(t => t.id === apiModel.id);
        if (!existing) {
            this.openTabs.push(apiModel);
        }
        this.activeTab = apiModel;
    }

    onTabClosed(apiModel: ApiModel): void {
        const index = this.openTabs.findIndex(t => t.id === apiModel.id);
        if (index !== -1) {
            this.openTabs.splice(index, 1);
            if (this.activeTab?.id === apiModel.id) {
                this.activeTab = this.openTabs.length > 0 ? this.openTabs[this.openTabs.length - 1] : null;
            }
        }
    }

    onTabSelected(apiModel: ApiModel): void {
        this.activeTab = apiModel;
    }

    createNewRequest(): void {
        const newRequest: ApiModel = {
            id: this.storageService.generateId(),
            name: 'New Request',
            method: 'GET',
            url: '',
            isModified: true,
            isTabOpen: false,
            isCurrentTab: false,
            isNewTab: true,
            isAuthConfig: false
        };

        this.storageService.addApiModel(newRequest);
        this.onTabOpened(newRequest);
    }

    createNewGroup(): void {
        // This will be handled by the sidebar component
    }

    save(): void {
        if (this.activeTab) {
            this.storageService.updateApiModel(this.activeTab.id, {
                ...this.activeTab,
                isModified: false
            });
        }
        this.storageService.saveData();
    }

    saveAll(): void {
        this.storageService.saveData();
    }

    openProxySettings(): void {
        // This will be handled by opening a dialog
    }

    showAbout(): void {
        alert(`${APP_CONSTANTS.APP_TITLE}\n\n${APP_CONSTANTS.COPYRIGHT}`);
    }
}
