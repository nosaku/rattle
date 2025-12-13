/*
 * Copyright (c) 2025 nosaku
 */

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ApiModel } from '../../models/app.models';

@Component({
    selector: 'app-tab-bar',
    templateUrl: './tab-bar.component.html',
    styleUrls: ['./tab-bar.component.scss']
})
export class TabBarComponent {
    @Input() tabs: ApiModel[] = [];
    @Input() activeTab: ApiModel | null = null;
    @Output() tabSelected = new EventEmitter<ApiModel>();
    @Output() tabClosed = new EventEmitter<ApiModel>();

    onTabClick(tab: ApiModel): void {
        this.tabSelected.emit(tab);
    }

    onCloseTab(tab: ApiModel, event: Event): void {
        event.stopPropagation();
        this.tabClosed.emit(tab);
    }

    getMethodClass(method: string): string {
        return method?.toLowerCase() || '';
    }
}
