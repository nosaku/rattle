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

export interface ApiModel {
    id: string;
    name: string;
    method: string;
    url: string;
    params?: Record<string, string>;
    headers?: Record<string, string>;
    body?: string;
    response?: string;
    statusCode?: number;
    isModified: boolean;
    isTabOpen: boolean;
    tabNbr?: number;
    isCurrentTab: boolean;
    consoleLog?: string;
    isNewTab: boolean;
    isAuthConfig: boolean;
    authType?: string;
    authConfigId?: string;
    groupId?: string;
}

export interface ApiGroup {
    id: string;
    name: string;
    parentId?: string;
}

export interface ProxySettings {
    proxyMode: 'OFF' | 'ON' | 'SYSTEM';
    httpProxy?: string;
    httpsProxy?: string;
    username?: string;
    password?: string;
    verifySslCertificate: boolean;
}

export interface AppData {
    apiModels: ApiModel[];
    apiGroups: ApiGroup[];
    proxySettings?: ProxySettings;
}

export interface HttpResponse {
    status: number;
    statusText: string;
    headers: Record<string, string>;
    data: any;
    duration: number;
}

export interface TreeNode {
    id: string;
    name: string;
    type: 'group' | 'request' | 'auth-config';
    data?: ApiModel | ApiGroup;
    children?: TreeNode[];
    expanded?: boolean;
}
