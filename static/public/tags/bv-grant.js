define([
    'vue',
    'jquery',
    'util'
], function (vue, $, util) {
    vue.component('bv-grant', {
        props: {
            entity: '',

            name: '',

            left: '',
            right: '',
            code: '',
            desc: '',
            label: '',
            /// id: '',
            options: ''
        },
        data: function () {
            return {
                innerEntity: this.entity,
                innerOptions: this.options || []
            }
        },
        beforeCreate: function () {
            this.localStopWatch = false;
        },
        created: function () {
            util.initDefault(this);
        },
        mounted: function() {
            var vm = this;
            $('#' + vm.left, vm.$el).multiselect({
                sort: false,
                afterMoveToRight: function(left, right, options) {
                    vm.setResult(left, right, options);
                },
                afterMoveToLeft: function(left, right, options) {
                    vm.setResult(left, right, options);
                }
            });

            vm.$watch('innerEntity.' + vm.left, function() {
                if (!vm.localStopWatch) {
                    var $m = $('#' + vm.left, vm.$el).data('crlcu.multiselect');
                    var v = vm.innerEntity[vm.left];
                    vm.initOptions($m.$left, v);
                    vm.innerOptions = vm.innerOptions.concat(util.clone(v));
                }
            });
            vm.$watch('innerEntity.' + vm.right, function() {
                if (!vm.localStopWatch) {
                    var $m = $('#' + vm.left, vm.$el).data('crlcu.multiselect');
                    var v = vm.innerEntity[vm.right];
                    vm.initOptions($m.$right, v);
                    vm.innerOptions = vm.innerOptions.concat(util.clone(v));
                }
            });
        },
        methods: {
            initOptions: function($element, options) {
                $('optgroup', $element).remove();
                $('option', $element).remove();
                if (options) {
                    var groups = [];
                    var seprates = [];

                    if (this.label) {
                        var currentGroup = '';
                        var currentSub = [];
                        for (var i=0; i<options.length; i++) {
                            var option = options[i];
                            if (option[this.label]) {
                                if (currentGroup && currentGroup !== option[this.label]) {
                                    groups.push({
                                        label: currentGroup,
                                        options: util.clone(currentSub)
                                    });
                                    currentSub = [];
                                }
                                currentGroup = option[this.label];
                                currentSub.push(option);

                                if (i === options.length - 1) {
                                    groups.push({
                                        label: option[this.label],
                                        options: util.clone(currentSub)
                                    });
                                    currentGroup = '';
                                    currentSub = [];
                                }
                            } else {
                                seprates.push(option);
                            }
                        }
                    } else {
                        seprates = options;
                    }

                    for (var i=0; i<groups.length; i++) {
                        var $optgroup = $('<optgroup label="' + groups[i].label + '"></optgroup>');
                        for (var j=0; j<groups[i].options.length; j++) {
                            $('<option value="' + groups[i].options[j][this.code] + '">' + groups[i].options[j][this.desc] + '</option>').appendTo($optgroup);
                        }
                        $optgroup.appendTo($element);
                    }
                    for (var i=0; i<seprates.length; i++) {
                        $('<option value="' + seprates[i][this.code] + '">' + options[i][this.desc] + '</option>').appendTo($element);
                    }
                }
            },
            setResult: function(left, right, options) {
                this.localStopWatch = true;
                var $leftOptions = $('option', left);
                var lefts = [];
                for (var i=0; i<$leftOptions.length; i++) {
                    var code = $($leftOptions[i]).val();

                    var index = util.index(this.innerOptions, code, this.code);
                    if (index >= 0) {
                        lefts.push(this.innerOptions[index]);
                    }
                }
                this.innerEntity[this.left] = lefts;

                var $rightOptions = $('option', right);
                var rights = [];
                for (var i=0; i<$rightOptions.length; i++) {
                    var code = $($rightOptions[i]).val();

                    var index = util.index(this.innerOptions, code, this.code);
                    if (index >= 0) {
                        rights.push(this.innerOptions[index]);
                    }
                }
                this.innerEntity[this.right] = rights;
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*
            <div class="bv-grant">
                <div class="col-sm-5">
                    <select class="form-control" multiple="multiple" :id="left"></select>
                </div>

                <div class="col-sm-2">
                    <button type="button" class="btn btn-default" :id="left + '_rightAll'"><i class="glyphicon glyphicon-forward"></i></button>
                    <button type="button" class="btn btn-default" :id="left + '_rightSelected'"><i class="glyphicon glyphicon-chevron-right"></i></button>
                    <button type="button" class="btn btn-default" :id="left + '_leftSelected'"><i class="glyphicon glyphicon-chevron-left"></i></button>
                    <button type="button" class="btn btn-default" :id="left + '_leftAll'"><i class="glyphicon glyphicon-backward"></i></button>
                </div>

                <div class="col-sm-5">
                    <select class="form-control" multiple="multiple" :id="left + '_to'"></select>
                </div>
            </div>
             */
        })
    });
});