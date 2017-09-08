define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-chart', {
        props: {
            width: '',
            height: '',
            css: {
                default: function () {
                    return {};
                }
            },
            // 支持bar
            type: '',
            labels: '',
            datasets: '',
            min: {
                default: 0
            },
            max: ''
        },
        data: function () {
            return {
                innerStyle: this.css,
                innerType: this.type,
                innerLabels: this.labels,
                innerDatasets: this.datasets
            }
        },
        /*watch: {
            innerLabels: {
                handler: function (val, oldVal) {
                    this.refresh();
                },
                deep: true
            },
            innerDatasets: {
                handler: function (val, oldVal) {
                    this.refresh();
                },
                deep: true
            }
        },*/
        beforeCreate: function () {
            this.localChart = '';
        },
        mounted: function () {
            if (this.width) {
                vue.set(this.innerStyle, 'width', this.width);
                // this.innerStyle.width = this.width;
            }
            if (this.height) {
                vue.set(this.innerStyle, 'height', this.height);
                // this.innerStyle.height = this.height;
            }
            this.refresh();
        },
        methods: {
            refresh: function() {
                if (this.innerDatasets && this.innerDatasets.length > 0) {
                    for (var i=0; i<this.innerDatasets.length; i++) {
                        var type = this.innerDatasets[i].type || this.innerType;
                        if (type === 'line') {
                            if (!this.innerDatasets[i].borderColor) {
                                this.innerDatasets[i].borderColor = Const.chart.backgroundColors[i];
                            }
                            if (util.type(this.innerDatasets[i].fill) === 'undefined') {
                                this.innerDatasets[i].fill = false;
                            }
                        } else if (type === 'pie' || type === 'doughnut') {
                            if (!this.innerDatasets[i].backgroundColor) {
                                this.innerDatasets[i].backgroundColor = Const.chart.backgroundColors;
                            }
                        } else  {
                            if (!this.innerDatasets[i].backgroundColor) {
                                this.innerDatasets[i].backgroundColor = Const.chart.backgroundColors[i];
                            }
                        }
                    }
                }
                if (!this.localChart) {
                    var ctx = $('canvas', this.$el)[0];
                    var options = {};
                    if (this.innerType != 'pie') {
                        options = {
                            scales: {
                                yAxes: [{
                                    ticks: {
                                        min: this.min,
                                        max: this.max
                                    }
                                }]
                            }
                        };
                    }
                    if (!options.legend) {
                        options.legend = {};
                    }
                    if (!options.legend.position) {
                        options.legend.position = 'left';
                    }
                    this.localChart = new Chart(ctx, {
                        type: this.innerType,
                        data: {
                            labels: this.innerLabels,
                            datasets: this.innerDatasets
                        },
                        options: options
                    });
                } else {
                    // this.localChart.type = this.innerType;
                    this.localChart.data.labels = this.innerLabels;
                    this.localChart.data.datasets = this.innerDatasets;
                }
                this.localChart.update();
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div :style="innerStyle"><canvas></canvas></div>
            */
        })
    });
});