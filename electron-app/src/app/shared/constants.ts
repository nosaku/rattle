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

export const APP_CONSTANTS = {
    APP_TITLE: 'Rattle',
    COPYRIGHT: '© 2025 nosaku. All rights reserved.',
    FILE_NAME: 'rattle.json',

    HTTP_METHODS: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'],

    HTTP_HEADERS: [
        'Accept', 'Accept-Charset', 'Accept-Encoding', 'Accept-Language',
        'Authorization', 'Cache-Control', 'Connection', 'Content-Length',
        'Content-Type', 'Cookie', 'Date', 'Host', 'Origin', 'Referer',
        'User-Agent', 'X-API-Key', 'X-Auth-Token', 'X-CSRF-Token',
        'Access-Control-Allow-Origin', 'Access-Control-Allow-Methods'
    ],

    AUTH_TYPES: ['OAuth2', 'Basic Auth', 'API Key'],

    GROUP_NAMES: {
        HISTORY: 'History',
        AUTH_CONFIGURATIONS: 'Auth configurations'
    }
};
