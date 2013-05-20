/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.RecordTable',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var module = this,
            actions = {
                row: [
                    {name: 'edit'},
                    {name: 'save'},
                    {name: 'delete'},
                    {name: 'revert'}
                ],
                cell: [ {name: 'edit'} ],
                rows: [ {name: 'add'} ]
            },
            mock = {
                container: '.OG-layout-admin-details-center .ui-layout-content',
                data: { // TODO AG: data structure is a WIP, heavy review needed.
                    headers: [
                        {id: 'c1', text: 'foo', type:'string'},
                        {id: 'c2', text: 'bar', type:'string'},
                        {id: 'c3', text: 'baz', type:'string'},
                        {id: 'c4', text: 'boo', type:'string'}
                    ],
                    rows: [
                        {
                            id: 'c1',
                            cells: [
                                {text: 'Foo Cell 1'},
                                {text: 'Foo Cell 2'}
                            ]
                        },
                        {
                            id: 'c2',
                            cells: [
                                {text: 'Bar Cell 1' },
                                {text: 'Bar Cell 2'}
                            ]
                        },
                        {
                            id: 'c3',
                            cells: [
                                {text: 'Baz Cell 1' },
                                {text: 'Baz Cell 2'}
                            ]
                        },
                        {
                            id: 'c4',
                            cells: [
                                {text: 'Boo Cell 1' },
                                {text: 'Boo Cell 2'}
                            ]
                        }
                    ]
                }
            };

        var RecordTable = function (conf) {
            // if (!config) og.dev.warn('og.common.gadgets.RecordTable: Missing param [config] to constructor.');

            var config = conf || mock, cols = config.data.cols, rows = config.data.rows,
                container = $(config.container), headers, form;

            form = new og.common.util.ui.Form({
                data: {},
                module: 'og.views.gadgets.recordtable.recordtable_tash',
                selector: '.' + config.container
            });
            form.children.push(
                new form.Block({module: 'og.views.gadgets.recordtable.header_tash', extras: config.headers}),
                new form.Block({module: 'og.views.gadgets.recordtable.footer_tash'}),
                new form.Block({module: 'og.views.gadgets.recordtable.body_tash'})
            );
        };

        RecordTable.prototype = new og.common.util.ui.Block();
        return RecordTable;
    }
});