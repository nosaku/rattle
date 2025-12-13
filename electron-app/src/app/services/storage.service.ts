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
import { ApiModel, ApiGroup, AppData } from '../models/app.models';
import { APP_CONSTANTS } from '../shared/constants';

declare global {
    interface Window {
        electronAPI: any;
    }
}

@Injectable({
    providedIn: 'root'
})
export class StorageService {
    private apiModelsSubject = new BehaviorSubject<Map<string, ApiModel>>(new Map());
    private apiGroupsSubject = new BehaviorSubject<Map<string, ApiGroup>>(new Map());

    public apiModels$ = this.apiModelsSubject.asObservable();
    public apiGroups$ = this.apiGroupsSubject.asObservable();

    private dataFilePath: string = '';

    constructor() {
        this.initializeFilePath();
    }

    private async initializeFilePath(): Promise<void> {
        try {
            const userDataPath = await window.electronAPI.getUserDataPath();
            this.dataFilePath = `${userDataPath}/${APP_CONSTANTS.FILE_NAME}`;
            await this.loadData();
        } catch (error) {
            console.error('Error initializing file path:', error);
        }
    }

    async loadData(): Promise<void> {
        try {
            const exists = await window.electronAPI.fileExists(this.dataFilePath);

            if (exists) {
                const result = await window.electronAPI.readFile(this.dataFilePath);
                if (result.success) {
                    const data: AppData = JSON.parse(result.data);

                    const modelsMap = new Map<string, ApiModel>();
                    data.apiModels?.forEach(model => modelsMap.set(model.id, model));

                    const groupsMap = new Map<string, ApiGroup>();
                    data.apiGroups?.forEach(group => groupsMap.set(group.id, group));

                    this.apiModelsSubject.next(modelsMap);
                    this.apiGroupsSubject.next(groupsMap);
                }
            } else {
                // Initialize with default groups
                this.initializeDefaultGroups();
            }
        } catch (error) {
            console.error('Error loading data:', error);
            this.initializeDefaultGroups();
        }
    }

    private initializeDefaultGroups(): void {
        const groupsMap = new Map<string, ApiGroup>();

        const historyGroup: ApiGroup = {
            id: this.generateId(),
            name: APP_CONSTANTS.GROUP_NAMES.HISTORY
        };

        const authGroup: ApiGroup = {
            id: this.generateId(),
            name: APP_CONSTANTS.GROUP_NAMES.AUTH_CONFIGURATIONS
        };

        groupsMap.set(historyGroup.id, historyGroup);
        groupsMap.set(authGroup.id, authGroup);

        this.apiGroupsSubject.next(groupsMap);
    }

    async saveData(): Promise<void> {
        try {
            const appData: AppData = {
                apiModels: Array.from(this.apiModelsSubject.value.values()),
                apiGroups: Array.from(this.apiGroupsSubject.value.values())
            };

            const content = JSON.stringify(appData, null, 2);
            const result = await window.electronAPI.writeFile(this.dataFilePath, content);

            if (!result.success) {
                console.error('Error saving data:', result.error);
            }
        } catch (error) {
            console.error('Error saving data:', error);
        }
    }

    addApiModel(model: ApiModel): void {
        const models = new Map(this.apiModelsSubject.value);
        models.set(model.id, model);
        this.apiModelsSubject.next(models);
    }

    updateApiModel(id: string, updates: Partial<ApiModel>): void {
        const models = new Map(this.apiModelsSubject.value);
        const model = models.get(id);
        if (model) {
            models.set(id, { ...model, ...updates });
            this.apiModelsSubject.next(models);
        }
    }

    deleteApiModel(id: string): void {
        const models = new Map(this.apiModelsSubject.value);
        models.delete(id);
        this.apiModelsSubject.next(models);
    }

    addApiGroup(group: ApiGroup): void {
        const groups = new Map(this.apiGroupsSubject.value);
        groups.set(group.id, group);
        this.apiGroupsSubject.next(groups);
    }

    updateApiGroup(id: string, updates: Partial<ApiGroup>): void {
        const groups = new Map(this.apiGroupsSubject.value);
        const group = groups.get(id);
        if (group) {
            groups.set(id, { ...group, ...updates });
            this.apiGroupsSubject.next(groups);
        }
    }

    deleteApiGroup(id: string): void {
        const groups = new Map(this.apiGroupsSubject.value);
        groups.delete(id);
        this.apiGroupsSubject.next(groups);
    }

    generateId(): string {
        return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    }
}
