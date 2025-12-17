/*
 * Copyright (c) 2025 nosaku
 */

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TreeNode } from '../../models/app.models';

@Component({
    selector: 'app-tree-node',
    templateUrl: './tree-node.component.html',
    styleUrls: ['./tree-node.component.scss']
})
export class TreeNodeComponent {
    @Input() node!: TreeNode;
    @Input() activeId?: string;
    @Input() level: number = 0;
    @Output() nodeClick = new EventEmitter<TreeNode>();
    @Output() addRequest = new EventEmitter<string>();
    @Output() addGroup = new EventEmitter<string>();
    @Output() deleteNode = new EventEmitter<TreeNode>();

    onNodeClick(): void {
        this.nodeClick.emit(this.node);
    }

    onAddRequest(event: Event): void {
        event.stopPropagation();
        this.addRequest.emit(this.node.id);
    }

    onAddGroup(event: Event): void {
        event.stopPropagation();
        this.addGroup.emit(this.node.id);
    }

    onDelete(event: Event): void {
        event.stopPropagation();
        this.deleteNode.emit(this.node);
    }

    getMethodClass(): string {
        const model = this.node.data as any;
        return model?.method?.toLowerCase() || '';
    }

    getMethodBadgeText(): string {
        const model = this.node.data as any;
        return model?.method || '';
    }
}
