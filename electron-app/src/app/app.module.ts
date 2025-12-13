/*
 * Copyright (c) 2025 nosaku
 */

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { TreeNodeComponent } from './components/tree-node/tree-node.component';
import { TabBarComponent } from './components/tab-bar/tab-bar.component';
import { RequestEditorComponent } from './components/request-editor/request-editor.component';

import { StorageService } from './services/storage.service';
import { ApiService } from './services/api.service';
import { ProxyService } from './services/proxy.service';

@NgModule({
    declarations: [
        AppComponent,
        SidebarComponent,
        TreeNodeComponent,
        TabBarComponent,
        RequestEditorComponent
    ],
    imports: [
        BrowserModule,
        FormsModule
    ],
    providers: [
        StorageService,
        ApiService,
        ProxyService
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
