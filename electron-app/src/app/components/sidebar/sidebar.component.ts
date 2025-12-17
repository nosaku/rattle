/*
 * Copyright (c) 2025 nosaku
 */

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { StorageService } from '../../services/storage.service';
import { ApiModel, ApiGroup, TreeNode } from '../../models/app.models';

@Component({
    selector: 'app-sidebar',
    templateUrl: './sidebar.component.html',
    styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
    @Output() tabOpened = new EventEmitter<ApiModel>();
    @Input() activeTabId?: string;

    treeNodes: TreeNode[] = [];
    apiModelsMap = new Map<string, ApiModel>();
    apiGroupsMap = new Map<string, ApiGroup>();

    constructor(private storageService: StorageService) { }

    ngOnInit(): void {
        this.storageService.apiModels$.subscribe(models => {
            this.apiModelsMap = models;
            this.buildTree();
        });

        this.storageService.apiGroups$.subscribe(groups => {
            this.apiGroupsMap = groups;
            this.buildTree();
        });
    }

    private buildTree(): void {
        this.treeNodes = [];
        const nodeMap = new Map<string, TreeNode>();

        // Create group nodes
        this.apiGroupsMap.forEach(group => {
            const node: TreeNode = {
                id: group.id,
                name: group.name,
                type: 'group',
                data: group,
                children: [],
                expanded: true
            };
            nodeMap.set(group.id, node);
        });

        // Build hierarchy
        this.apiGroupsMap.forEach(group => {
            const node = nodeMap.get(group.id);
            if (node) {
                if (group.parentId && nodeMap.has(group.parentId)) {
                    const parentNode = nodeMap.get(group.parentId);
                    parentNode?.children?.push(node);
                } else {
                    this.treeNodes.push(node);
                }
            }
        });

        // Add API models to groups
        this.apiModelsMap.forEach(model => {
            if (model.groupId && nodeMap.has(model.groupId)) {
                const requestNode: TreeNode = {
                    id: model.id,
                    name: model.name,
                    type: model.isAuthConfig ? 'auth-config' : 'request',
                    data: model
                };
                nodeMap.get(model.groupId)?.children?.push(requestNode);
            }
        });
    }

    onNodeClick(node: TreeNode): void {
        if (node.type === 'group') {
            node.expanded = !node.expanded;
        } else if (node.type === 'request' || node.type === 'auth-config') {
            this.tabOpened.emit(node.data as ApiModel);
        }
    }

    onAddRequest(groupId?: string): void {
        const newRequest: ApiModel = {
            id: this.storageService.generateId(),
            name: 'New Request',
            method: 'GET',
            url: '',
            groupId: groupId,
            isModified: true,
            isTabOpen: false,
            isCurrentTab: false,
            isNewTab: true,
            isAuthConfig: false
        };

        this.storageService.addApiModel(newRequest);
        this.tabOpened.emit(newRequest);
    }

    onAddGroup(parentId?: string): void {
        const groupName = prompt('Enter group name:');
        if (groupName) {
            const newGroup: ApiGroup = {
                id: this.storageService.generateId(),
                name: groupName,
                parentId: parentId
            };

            this.storageService.addApiGroup(newGroup);
        }
    }

    onDeleteNode(node: TreeNode): void {
        if (confirm(`Delete ${node.name}?`)) {
            if (node.type === 'group') {
                this.storageService.deleteApiGroup(node.id);
            } else {
                this.storageService.deleteApiModel(node.id);
            }
        }
    }
}
