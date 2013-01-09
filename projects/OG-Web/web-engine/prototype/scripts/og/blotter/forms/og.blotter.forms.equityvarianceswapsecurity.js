/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.equityvarianceswapsecurity',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui;
            if(config) {data = config; data.id = config.trade.uniqueId;}
            else {data = {security: {type: "EquityVarianceSwapSecurity", name: "EquityVarianceSwapSecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.otc_trade};}
            console.log(config);
            constructor.load = function () {
                constructor.title = 'Equity Varience Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.equity_variance_swap_tash',
                        extras: {notional: data.security.notional, region: data.security.regionId,
                            underlying: data.security.spotUnderlyingId, settlement: data.security.settlementDate, 
                            first: data.security.firstObservationDate, last: data.security.lastObservationDate,
                            annualization: data.security.annualizationFactor, strike: data.security.strike
                         },
                        processor: function (data) {
                            data.security.parameterizedAsVariance = 
                            og.blotter.util.get_checkbox("security.parameterizedAsVariance");
                        },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}}),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'security.observationFrequency',
                                value: data.security.observationFrequency, placeholder: 'Frequency'
                            })                               
                        ]
                    }),
                    new og.common.util.ui.Attributes({form: form, attributes: data.security.attributes})
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.settlementDate");
                    og.blotter.util.add_datetimepicker("security.lastObservationDate");
                    og.blotter.util.add_datetimepicker("security.firstObservationDate");
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.currency", data.security.currency);
                    og.blotter.util.check_checkbox("security.parameterizedAsVariance", 
                        data.security.parameterizedAsVariance);
                });
                form.on('form:submit', function (result){
                    og.api.rest.blotter.trades.put(result.data).pipe(/*console.log*/);
                });
            }; 
            constructor.load();
            constructor.submit = function () {
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});