define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-table', {
        props: {
            name: '',
            // 标题
            title: {
                default: '信息列表'
            },
            // 是否允许折叠
            collapse: {
                default: false
            },
            layout: '',
            // 是否固定表头
            fixed: {
                default: true
            },
            // tableHeight: 0,
            // 容器class
            clazz: '',
            css: '',
            containerClass: '',
            // 每页显示数据条数
            limit: {
                default: 10
            },
            // 显示类型default-默认sub-子表
            type: {
                default: 'default'
            },

            // 是否自动加载
            loadType: {
                default: 'auto'
            },
            // 是否分页
            pagination: {
                default: true
            },
            // 选择框false-无checkbox-复选框radio-单选按钮
            select: {
                default: 'false'
            },
            // 序号false-无page-当前页db-全局
            linenumber: {
                default: 'false'
            },
            // default-自动page-当前页过滤false-不过滤db-后台数据库过滤
            filterType: {
                default: 'default'
            },
            // 查询方式default-模糊查询、精确查询filter-只支持模糊查询filterMore-只支持精确查询
            filterLayout: {
                default: 'default'
            },
            // 模糊查询字段来源：default-默认，数据库全部字段；filter-页面定义的filter不为false的字段；filterMore-等同于精确查询
            filterColumnSource: {
                default: 'default'
            },
            // 可设置为'false'
            limitFilterDefaultOption: {
                default: '请选择限制条件'
            },

            // 查询数据url
            url: '',
            filterUrl: '',
            filterMoreUrl: '',
            // 列表字段定义
            entityName: '',
            keys: '',
            // 格式:name type,name type
            orders: '',
            // 固定数据
            initRows: '',
            initParam: '',
            initParamList: {
                default: function () {
                    return [];
                }
            },
            columns: {
                default: function () {
                    return [];
                }
            },
            // 操作按钮定义
            operates: {
                default: function () {
                    return [];
                }
            },
            // 是否支持行内编辑
            editable: {
                default: false
            },

            chooseDesc: '',
            chooseShow: ''
        },
        data: function () {
            return {
                innerName: this.name,
                innerLayout: this.layout,
                innerFixed: this.fixed,
                innerClass: this.clazz,
                innerStyle: this.css,
                innerContainerClass: this.containerClass,
                innerLimit: this.limit,
                innerUrl: this.url,
                innerFilterUrl: this.filterUrl,
                innerFilterMoreUrl: this.filterMoreUrl,
                innerFilterType: this.filterType,
                innerInitRows: this.initRows,
                innerInitParamList: this.initParamList,
                innerColumns: this.columns,
                innerChooseDesc: this.chooseDesc,
                innerChooseResult: [],
                innerDataInited: false,
                innerRows: [],
                innerCheckedLength: 0,
                // 当前查询是模糊查询还是精确查询
                innerFilterMore: false,
                // 排序[{name: '', sort: ''}, ...]
                innerOrderList: [],
                // 总页数
                innerTotalPage: 0,
                // 总条数
                innerRowCount: 0,
                // 列数colspan
                innerColumnNum: 0,
                // 允许查询的字段定义
                innerFilterColumns: [],
                // 限定查询字段定义
                innerLimitFilterColumns: [],
                innerLimitFilterColumnName: '',
                // 精确查询条件定义
                innerFilterEntity: '',
                // 用户具备权限的按钮
                innerAuthedOperates: [],
                // 当前页码
                innerCurrentPage: 1,
                // 当前页开始序号
                innerOffset: 0,
                innerVisible: true,
                innerWidth: {}
            };
        },
        beforeCreate: function () {
            // 主键字段名-自动根据$columns获取
            // 支持复合主键
            this.localKeys = [];
            // 选中数据
            this.localBodyChecked = [];
            this.innerInited = false;
            // 模糊查询-查询条件
            this.localFilter = '';
            // 精确查询-查询条件
            this.localQuery = [];
            // 排序字段名
            this.localOrderName = null;
            // 排序方式asc desc
            this.localOrderType = null;
            // 是否全选
            this.localHeadChecked = false;
            // 加载进度
            this.localProgress = 0;
            // 模糊查询的字段定义，逗号分开
            this.localFilterColumnNames = '';
            // 当前登录用户具有权限的按钮定义
            this.localUserOperates = [];
            // 行内编辑新增时添加的行数据
            this.localInitRow = {
                _insert: true
            };
            this.localFilterEntity = {};
        },
        mounted: function() {
            if (!this.innerName && this.$vnode.key) {
                this.innerName = this.$vnode.key;
            }
            if (this.filterLayout === 'filterMore') {
                this.innerFilterMore = true;
            }
            this.innerContainerClass = this.type ? 'bv-table-' + this.type : '';
            // TODO:
            this.localUserOperates = util.userOperates($(this.$el));

            if (this.type === 'sub') {
                this.innerLayout = 'body';
            }
            if (!this.innerLayout) {
                this.innerLayout = util.layout($(this.$el));
            }
            if ((this.type && this.type === 'sub') || this.innerLayout === 'modal') {
                this.innerFixed = false;
            }

            if (this.type === 'choose') {
                if (!this.innerChooseDesc && this.keys) {
                    this.innerChooseDesc = this.keys;
                }
            }

            if (this.innerFilterType === 'default') {
                if (this.pagination && !this.innerInitRows) {
                    this.innerFilterType = 'db';
                } else {
                    this.innerFilterType = 'page';
                }
            }
            if (!this.innerUrl && !this.innerInitRows) {
                if (this.pagination) {
                    this.innerUrl = Const.url.table.page;
                } else {
                    this.innerUrl = Const.url.table.select;
                }
            }
            if (!this.innerFilterUrl) {
                this.innerFilterUrl = this.innerUrl;
            }
            if (!this.innerFilterMoreUrl) {
                this.innerFilterMoreUrl = this.innerUrl;
            }
            // 从localStroage获取页码设置
            var limit = util.storage('limit', undefined, this.name);
            if (limit) {
                limit = util.toNumber(limit);
                if (limit > 0 && limit <= 500) {
                    this.innerLimit = limit;
                }
            } else if (this.type && this.type === 'sub') {
                this.innerLimit = 5;
            }

            if (this.keys) {
                this.localKeys = util.replaceAll(this.keys, ' ', '').split(',');
            }

            // 根据字段定义取出主键
            if (!this.localKeys || this.localKeys.length === 0) {
                this.localKeys = [];
                for (var i = 0; i < this.innerColumns.length; i++) {
                    var column = this.innerColumns[i];
                    if (column.type && column.type === 'id') {
                        this.localKeys.push(column.name);
                    }
                }
            }

            if (!util.isEmpty(this.initParam)) {
                for (var p in this.initParam) {
                    this.innerInitParamList.push({
                        name: p,
                        operate: '=',
                        value: this.initParam[p]
                    });
                }
            }

            // var localFilterEntity = {};
            for (var i = 0; i < this.innerColumns.length; i++) {
                var column = this.innerColumns[i];

                if (column.width) {
                    if (Const.width[column.width]) {
                        column.width = Const.width[column.width];
                    }
                }
                // 初始化select查询
                if (column.format && column.format === 'dict') {
                    if (column.config.preset && !column.config.code) {
                        column.config.code = Const.init.preset[column.config.preset].code;
                        column.config.desc = Const.init.preset[column.config.preset].desc;
                    }
                    if (!column.config.choose && (column.config.entityName || column.config.url)) {
                        if (column.config.entityName) {
                            var columnNames = column.config.code;
                            if (util.isEmpty(column.config.trans)) {
                                if (column.config.desc) {
                                    columnNames += ',' + column.config.desc;
                                }
                            }
                            util.post({
                                url: Const.url.select.query,
                                async: false,
                                data: {
                                    entityName: column.config.entityName,
                                    columns: columnNames,
                                    initParamList: column.config.initParamList,
                                    orderList: column.config.orderList
                                },
                                success: function(data) {
                                    column.config.choose = util.data(data);
                                }
                            });
                        } else if (column.config.url) {
                            util.post({
                                url: column.config.url,
                                async: false,
                                data: {
                                    initParamList: column.config.initParamList,
                                    orderList: column.config.orderList
                                },
                                success: function(data) {
                                    column.config.choose = util.data(data);
                                }
                            });
                        }
                    }
                    var result = util.transOptions({
                        type: 'select',
                        initOptions: column.config.choose,
                        preset: column.config.preset,
                        code: column.config.code,
                        desc: column.config.desc
                    });
                    if (result && result.options) {
                        /// column.config.preset = 'default';
                        column.config.choose = result.options;
                    }
                } else if (column.href && column.href.type === 'sub') {
                    if (!column.config) {
                        column.config = {};
                    }
                    /// column.config.type = 'a';
                }

                // 取出精确查询字段
                // column.filter column.config
                if (column.filter === true || column.filter === 'like') {
                    column.filter = {
                        type: 'textfield',
                        operate: 'like'
                    };
                } else if (column.filter === '=') {
                    column.filter = {
                        type: 'textfield',
                        operate: '='
                    };
                } else if (column.filter === 'select') {
                    column.filter = {
                        type: 'select',
                        operate: '='
                    };
                } else if (column.filter === 'radio') {
                    column.filter = {
                        type: 'radio',
                        operate: '='
                    };
                } else if (column.filter === 'checkbox') {
                    column.filter = {
                        type: 'checkbox',
                        operate: '='
                    };
                } else if (column.filter === 'auto') {
                    column.filter = {
                        type: 'auto',
                        operate: '='
                    };
                } else if (column.filter === 'between') {
                    column.filter = {
                        operate: 'between'
                    };
                }
                if (column.filter && column.filter !== 'false') {
                    if (column.filter.operate !== false && column.filter.operate !== 'false') {
                        if (!column.config) {
                            column.config = {};
                        }
                        if (!column.filterConfig) {
                            column.filterConfig = {};
                        }
                        if (!column.filterLayout) {
                            column.filterLayout = {};
                        }
                        if (!column.filter.type) {
                            column.filter.type = 'textfield';
                        }
                        if (column.filter.operate && column.filter.operate === 'between') {
                            if (this.innerLayout === 'modal') {
                                column.filterLayout.containerClass = 'col-md-12 col-lg-8';
                            } else {
                                column.filterLayout.containerClass = 'col-sm-12 col-md-8 col-lg-6';
                            }
                            column.filterLayout.labelClass = 'col-sm-2 col-sm-2y col-md-2';
                            column.filterLayout.tagClass = 'col-sm-10 col-sm-10x col-md-10';
                            column.filterConfig.start = column.name + 'Start';
                            column.filterConfig.end = column.name + 'End';
                            this.localFilterEntity[column.filterConfig.start] = null;
                            this.localFilterEntity[column.filterConfig.end] = null;

                            column.config.operate = column.filter.operate;
                            column.filterLayout.for = column.filterConfig.start;

                            if (column.config.period) {
                                column.filterConfig.range = column.name + 'Range';
                                this.localFilterEntity[column.filterConfig.range] = null;
                            }

                            column.filterConfig.type = 'bv-' + column.filter.type;
                            column.filterConfig.tagName = 'bv-between';
                        } else {
                            if (this.innerLayout === 'modal') {
                                if (column.filter.cols === 6) {
                                    column.filterLayout.containerClass = 'col-md-6';
                                    column.filterLayout.labelClass = 'col-sm-2 col-sm-2y col-md-2';
                                    column.filterLayout.tagClass = 'col-sm-10 col-sm-10x col-md-10';
                                } else {
                                    column.filterLayout.containerClass = 'col-sm-12 col-md-6 col-lg-4';
                                    column.filterLayout.labelClass = 'col-sm-4';
                                    column.filterLayout.tagClass = 'col-sm-8';
                                }
                            } else {
                                if (column.filter.cols === 6) {
                                    column.filterLayout.containerClass = 'col-md-6';
                                    column.filterLayout.labelClass = 'col-sm-2 col-sm-2y col-md-2';
                                    column.filterLayout.tagClass = 'col-sm-10 col-sm-10x col-md-10';
                                } else {
                                    column.filterLayout.containerClass = 'col-sm-6 col-md-4 col-lg-3';
                                    column.filterLayout.labelClass = 'col-sm-4';
                                    column.filterLayout.tagClass = 'col-sm-8';
                                }
                            }
                            this.localFilterEntity[column.name + 'Filter'] = null;

                            column.filterLayout.for = column.name + 'Filter';
                            column.filterConfig.tagName = 'bv-' + column.filter.type;
                            column.filterConfig.type = 'bv-' + column.filter.type;
                        }
                        if (!column.filterConfig.type) {
                            column.filterConfig.type = 'bv-textfield';
                        }
                        if (column.name) {
                            column.filterConfig.name = column.name + 'Filter';
                        }
                        column.filterConfig.from = 'filter';

                        this.innerFilterColumns.push(column);
                        if (this.filterColumnSource === 'filterMore') {
                            if (this.localFilterColumnNames) {
                                this.localFilterColumnNames += ',';
                            }
                            this.localFilterColumnNames += column.name;
                        }
                    }
                    // innerLimitFilterColumns
                    if (column.filter.limit && column.filter.limit !== 'false') {
                        this.innerLimitFilterColumns.push({
                            name: column.name,
                            head: column.head,
                            operate: column.filter.limit
                        });
                    }
                }
                if (this.filterColumnSource === 'filter' && (!column.filter || column.filter !== 'false')) {
                    if (this.localFilterColumnNames) {
                        this.localFilterColumnNames += ',';
                    }
                    this.localFilterColumnNames += column.name;
                }

                // 处理行内编辑
                if (this.editable && (!column.type || column.type !== 'operate')) {
                    if (!column.editConfig) {
                        column.editConfig = {};
                    }
                    if (!column.edit) {
                        column.edit = {};
                    }
                    if (!column.type) {
                        column.type = 'textfield';
                    }
                    if (column.name) {
                        column.editConfig.name = column.name;
                    }

                    // 行内编辑
                    this.localInitRow[column.name] = null;
                } else if (!column.type) {
                    if (column.href) {
                        column.type = 'href';
                    } else {
                        column.type = 'static';
                    }
                }

                // title处理
                if (!column.title) {
                    column.title = '';
                }
                if (column.title === true) {
                    column.title = 'true';
                }

                this.innerColumns[i] = column;
            }
            this.innerFilterEntity = this.localFilterEntity;

            if (!util.isEmpty(this.innerLimitFilterColumns)) {
                if (this.innerLimitFilterColumns.length > 1 && !util.isEmpty(this.limitFilterDefaultOption) && this.limitFilterDefaultOption !== 'false') {
                    this.innerLimitFilterColumns.splice(0, 0, {
                        name: '',
                        head: this.limitFilterDefaultOption
                    });
                }
                if (this.innerLimitFilterColumns.length === 1) {
                    this.innerLimitFilterColumnName = this.innerLimitFilterColumns[0].name;
                }
            }

            // 计算总列数
            this.innerColumnNum = this.innerColumns.length;
            if (this.select === 'checkbox' || this.select === 'radio') {
                this.innerColumnNum++;
            }
            if (this.linenumber !== 'false') {
                this.innerColumnNum++;
            }

            // 判断按钮权限
            for (var i=0; i<this.operates.length; i++) {
                var operate = this.operates[i];
                if (operate.auth) {
                    if (this.localUserOperates && this.localUserOperates.length > 0) {
                        // 需要判断按钮权限
                        for (var j=0; j<this.localUserOperates.length; j++) {
                            var userOperate = this.localUserOperates[j];
                            if (operate.id === userOperate.buttonId) {
                                operate.text = userOperate.buttonName;
                                this.innerAuthedOperates.push(operate);
                                break;
                            }
                        }
                    }
                } else {
                    this.innerAuthedOperates.push(operate);
                }
            }
            // 处理默认排序
            this.innerOrderList = util.transOrder(this.innerOrderList, this.orders);
            this.innerWidth = {
                checkbox: Const.width.checkbox,
                radio: Const.width.radio,
                linenumber: Const.width.linenumber
            };

            var vm = this;
            if (this.loadType === 'auto' && (!this.type || this.type !== 'sub')) {
                this.jumpTo();
                util.tooltip($(this.$el), 'data-title');
            } else if (this.limitFilterDefaultOption !== 'false' && this.loadType === 'false') {
                setTimeout(function() {
                    vm.innerRowCount = 0;
                    vm.innerRows = [];
                }, 300);
            }
//                root.$define[this.name] = this;

            //$.fn.keySwitch();

            var $container = $(this.$el);
            if (this.innerFixed) {
                $container.closest('.bv-content').scroll(function() {
                    var top = $('.bv-table-thead', $container).position().top;
                    if (top <= 0) {
                        $('.bv-table-thead', $container).css('margin-top', -1 * top);
                    } else {
                        $('.bv-table-thead', $container).css('margin-top', 0);
                    }
                    // promptTopPosition += field.closest('.bv-content').getNiceScroll()[0].newscrolly;
                });
                $('.bv-table-body', $container).scroll(function() {
                    $('.bv-table-thead', $container).css('left', -1 * $(this).scrollLeft());
                });
            }
            if (this.innerFixed) {
                $(window).resize(function() {
                    if (util.isCurrent($(vm.$el))) {
                        vm.calc();
                        vm.calcHeight('resize');
                    }
                });
            }
            vm.calcHeight('init');
        },
        // body数据加载完成调用
        updated: function() {
            this.innerOffset = (this.innerCurrentPage - 1) * this.innerLimit;
            if ($(this.$el).length === 1) {
                // 生成bootstrap的tooltip
                util.tooltip($(this.$el), 'data-title');
//                    this.tableScroll();

                // 计算固定表头宽度
                if (this.innerFixed) {
                    this.calc();
                }
            }
        },
        methods: {
            calc: function() {
                var $container = $(this.$el);
                $('.bv-table-thead thead tr', $container).width($('.bv-table-tbody thead tr', $container).width());
                $('.bv-table-thead thead th:not(.fixed)', $container).each(function(index, element) {
                    $(element).width($('.bv-table-tbody thead th:not(.fixed)', $container).eq(index).width());
                    // $('.bv-table-tbody thead th:not(.fixed)', $container).eq(index).width($(element).width());
                });
            },
            calcHeight: function(from) {
                if (this.innerFixed) {
                    var toolbarHeight = $('.bv-table-toolbar', this.$el).outerHeight(true);
                    var filterHeight = $('.bv-table-filter', this.$el).outerHeight(true);
                    if (this.innerFilterMore) {
                        filterHeight += $('.bv-table-filter-more', this.$el).outerHeight(true) + 4;
                    }
                    if (this.innerLayout === 'body' && $(this.$el).closest('.bv-content').length === 1) {
                        /*if (!this.tableHeight) {
                         this.tableHeight = $(this.$el).closest('.bv-content').height() - 5;
                         }*/
                        var tableContainerHeight = $(this.$el).closest('.bv-content').height() - 5 - ($(this.$el).offset().top - $(this.$el).closest('.bv-content').offset().top);
                        /// $('.bv-table-container', this.$el).css('max-height', tableContainerHeight);

                        var footerHeight = 0;
                        if (this.pagination) {
                            footerHeight = $('.bv-table-footer', this.$el).outerHeight(true);
                            // extraHeight += 25;
                        }
                        var tableBodyHeight = tableContainerHeight - toolbarHeight - filterHeight - footerHeight;
                        $('.bv-table-body', this.$el).css('max-height', tableBodyHeight);
                    }

                    // this.tableTop = toolbarHeight + filterHeight;
                    /// $('.bv-table-thead', this.$el).css('top', toolbarHeight + filterHeight);
                    $('.bv-table-thead', this.$el).width($('.bv-table-tbody', this.$el).width());
                    $('.bv-table-thead', this.$el).css('top', $('.bv-table-tbody', this.$el).position.top);
                    if (from && from === 'init') {
                        $('.bv-table-thead', this.$el).show();
                    }
                }
            },
            isButtonVisible: function(data) {
                if (!data || data.show === false) {
                    return false;
                }
                if (data.show === 'none') {
                    return this.innerCheckedLength === 0;
                } else if (data.show === 'one') {
                    return this.innerCheckedLength === 1;
                } else if (data.show === 'oneOrMore') {
                    return this.innerCheckedLength >= 1;
                } else if (data.show === 'import') {
                    return data.url;
                } else if (data.show === 'export') {
                    return data.url;
                } else if (data.show === 'return') {
                    return data.url || util.type(data.click) === 'function';
                } else if (util.type(data.show) === 'function') {
                    return data.show.call(null, util.selected(this));
                } else if (util.type(data.check) === 'function') {
                    return data.check.call(this);
                } else if (!data.show) {
                    return true;
                }
                return true;
            },
            jumpTo: function(page, event) {
                if (event) {
                    $(event.target).tooltip('hide');
                }
                this.hideSub();
                this.fill(page);
            },
            initChecked: function() {
                this.onHeadCheck(undefined, false, true);
                this.localBodyChecked = [];
                this.innerCheckedLength = 0;
                this.innerTotalPage = Math.ceil(this.innerRowCount / this.innerLimit);
                $('tbody :checkbox, tbody :radio', $(this.$el).closest('.bv-table-container')).prop('checked', false);

                if (this.type === 'choose' && this.innerChooseResult.length > 0 && this.innerRows.length > 0) {
                    for (var i=0; i<this.innerRows.length; i++) {
                        for (var j=0; j<this.innerChooseResult.length; j++) {
                            if (this.innerChooseResult[j].code === this.innerRows[i][this.keys]) {
                                this.onBodyCheck(undefined, i);
                                break;
                            }
                        }
                    }
                }
                this.innerCheckedLength = this.localBodyChecked.length;
            },
            refresh: function(event, initParamList) {
                this.hideSub();
                if (initParamList) {
                    this.innerInitParamList = initParamList;
                }
                if (this.filterLayout === 'filterMore') {
                    this.doFilterMore();
                } else {
                    this.fill('refresh');
                }
            },
            progressing: function(t, isFail) {
                var $progress = $('.bv-progress-container', this.$el);
                if ($progress.length === 1) {
                    if (!t) {
                        this.localProgress = 0;
                        $('.progress', $progress).hide();
                        $('.progress', $progress).removeClass('error');
                        $('.progress-bar', $progress).css('width', '0');
                        $('.progress-bar', $progress).removeClass('progress-bar-success progress-bar-danger');
                        setTimeout(function() {
                            $('.progress', $progress).show();
                        }, 50);
                        var vm = this;
                        return setInterval(function() {
                            if (vm.localProgress <= 60) {
                                vm.localProgress += 20;
                            } else if (vm.localProgress <= 70) {
                                vm.localProgress += 10;
                            } else if (vm.localProgress <= 80) {
                                vm.localProgress += 5;
                            } else if (vm.localProgress <= 90) {
                                vm.localProgress += 2;
                            } else if (vm.localProgress <= 95) {
                                vm.localProgress += 1;
                            } else if (vm.localProgress <= 98) {
                                vm.localProgress += 0.2;
                            } else if (vm.localProgress <= 99) {
                                vm.localProgress += 0.1;
                            } else if (vm.localProgress <= 99.5) {
                                vm.localProgress += 0.01;
                            }
                            $('.progress-bar', $progress).css('width', vm.localProgress + '%');
                        }, 100);
                    } else {
                        this.localProgress = 100;
                        this.initChecked();
                        var vm = this;
                        setTimeout(function() {
                            clearInterval(t);
                            $('.progress-bar', $progress).css('width', vm.localProgress + '%');
                            if (isFail) {
                                $('.progress-bar', $progress).addClass('progress-bar-danger');
                                $('.progress', $progress).addClass('error');
                            } else {
                                vm.innerInited = true;
                                $('.progress-bar', $progress).addClass('progress-bar-success');
                            }
                        }, 200);
                    }
                }
            },
            // table加载数据
            // table用
            fill: function(page) {
                // 加上后页面刷新显示效果不太合理
                /// this.innerRowCount = 0;
                /// this.innerRows.removeAll();
                // 去除自定义标签的缓存 name + '-' + el.name + '-td' + '-' + i
                /*if (Const.global[this.name] && Const.global[this.name].length > 0) {
                    util.clearVmCache(Const.global[this.name]);
                 Const.global[this.name] = [];
                }*/
                if (this.innerInitRows) {
                    if (util.type(this.innerInitRows) === 'object') {
                        this.innerRowCount = 1;
                        this.innerRows = [];
                        this.innerRows.push(this.innerInitRows);
                    } else if (util.type(this.innerInitRows) === 'array') {
                        this.innerRows = [];
                        for (var i=0; i<this.innerInitRows.length; i++) {
                            this.innerRows.push(this.innerInitRows[i]);
                        }
                        this.innerRowCount = this.innerRows.length;
                    }
                    //this.innerInited = true;
                    this.initChecked();
                    var t = this.progressing();
                    this.progressing(t);
                    this.innerDataInited = true;
                    return;
                }
                var vm = this;
                var refresh = false;
                if (!page) {
                    page = 1;
                }
                if (page === 'refresh') {
                    refresh = true;
                    page = vm.innerCurrentPage;
                } else if (page === 'limit' || page === 'order' || page === 'filter') {
                    refresh = true;
                    vm.innerCurrentPage = 1;
                    page = 1;
                }
                var url = vm.innerUrl;
                if (!vm.innerInited || !vm.pagination || refresh || page !== vm.innerCurrentPage) {
                    var data = {};
                    if (vm.entityName) {
                        data.entityName = vm.entityName;
                    }
                    if (vm.innerInitParamList) {
                        data.initParamList = vm.innerInitParamList;
                    }
                    if (!vm.innerFilterMore && vm.localFilter) {
                        if (vm.innerLimitFilterColumnName) {
                            var $limitFilter = {
                                name: vm.innerLimitFilterColumnName
                            };
                            for (var i=0; i<vm.innerLimitFilterColumns.length; i++) {
                                if (vm.innerLimitFilterColumns[i].name === vm.innerLimitFilterColumnName) {
                                    $limitFilter.operate = vm.innerLimitFilterColumns[i].operate;
                                }
                            }
                            if ($limitFilter.operate === 'like') {
                                $limitFilter.value = vm.localFilter;
                            } else {
                                $limitFilter.value = vm.localFilter;
                            }
                            data.filterMoreList = [$limitFilter];
                            url = vm.innerFilterMoreUrl;
                        } else {
                            if (vm.localFilterColumnNames) {
                                data.columns = vm.localFilterColumnNames;
                            }
                            data.filter = {
                                value: vm.localFilter
                            };
                            url = vm.innerFilterUrl;
                        }
                    }
                    if (vm.innerFilterMore && vm.localQuery && vm.localQuery.length > 0) {
                        data.filterMoreList = vm.localQuery;
                        url = vm.innerFilterMoreUrl;
                    }
                    if (vm.innerOrderList && vm.innerOrderList.length > 0 && !util.isEmpty(vm.innerOrderList[0])) {
                        data.orderList = vm.innerOrderList;
                    }
                    if (vm.pagination) {
                        switch (page) {
                            case 'first':
                                vm.innerCurrentPage = 1;
                                break;
                            case 'last':
                                vm.innerCurrentPage = vm.innerTotalPage;
                                break;
                            case 'next':
                                vm.innerCurrentPage++;
                                if (vm.innerCurrentPage > vm.innerTotalPage) {
                                    vm.innerCurrentPage = vm.innerTotalPage;
                                }
                                break;
                            case 'prev':
                                vm.innerCurrentPage--;
                                if (vm.innerCurrentPage < 1) {
                                    vm.innerCurrentPage = 1;
                                }
                                break;
                            default:
                                vm.innerCurrentPage = page;
                                break;
                        }
                        ///                    vm.spin();
                        var t = vm.progressing();
                        data.offset = util.offset(vm.innerCurrentPage, vm.innerLimit);
                        data.limit = vm.innerLimit;
                        // vm.innerRowCount = -1;
                        /// util.clearVmCache(vm.name + '-column-%');
                        vm.innerDataInited = false;
                        util.post({
                            url: url,
                            data: data,
                            success: function(res) {
                                if (util.success(res)) {
                                    var data = util.data(res);
                                    //vm.innerRows.removeAll();
                                    vm.innerRows = data.data;
                                    vm.innerRowCount = data.count;
                                    vm.progressing(t);
                                    vm.innerDataInited = true;
                                }
                            },
                            error: function() {
//                                vm.tableError = 'error';
                                vm.progressing(t, true);
                            }
                        });
                    } else {
                        var t = vm.progressing();
                        /// util.clearVmCache(vm.name + '-column-%');
                        vm.innerDataInited = false;
                        util.post({
                            url: url,
                            data: data,
                            success: function(res) {
                                if (util.success(res)) {
                                    var data = util.data(res);
                                    // vm.innerRows.removeAll();
                                    vm.innerRows = data;
                                    vm.innerRowCount = data.length;
                                    vm.progressing(t);
                                    vm.innerDataInited = true;
                                }
                            },
                            error: function() {
//                                vm.tableError = 'error';
                                vm.progressing(t, true);
                            }
                        });
                    }
                }
            },
            toImport: function(event, config) {
                util.modal({
                    url: config.page || Const.url.template.imports,
                    data: {
                        url: config.url,
                        template: config.template,
                        async: config.async
                    },
                    refresh: true,
                    vm: this.name
                });
            },
            toExport: function(event, config) {
                var data = {};
                if (this.entityName) {
                    data.entityName = this.entityName;
                }
                if (this.innerInitParamList) {
                    data.initParamList = this.innerInitParamList;
                }
                if (!this.innerFilterMore && this.localFilter) {
                    data.filter = {
                        value: this.localFilter
                    };
                }
                this.filterMoreData();
                if (this.innerFilterMore && this.localQuery && this.localQuery.length > 0) {
                    data.filterMoreList = this.localQuery;
                }
                if (this.innerOrderList && this.innerOrderList.length > 0) {
                    data.orderList = this.innerOrderList;
                }
                if (config.url) {
                    data.url = config.url;
                }
                if (config.template) {
                    data.template = config.template;
                }
                util.modal({
                    url: config.page || Const.url.template.exports,
                    data: data
                });
            },
            columnFormat: function(row, column) {
                if (util.type(column.format) === 'function') {
                    return column.format.call(null, row);
                }
                return util.format(util.trans(row[column.name], column.config), column.format);
            },
            columnTitle: function(row, column) {
                if (column.title) {
                    if (column.title === 'true') {
                        return row[column.name];
                    } else if (util.startsWith(column.title, '#')) {
                        return row[column.title.substring(1)] || '';
                    } else {
                        return column.title;
                    }
                }
                return '';
            },
            columnAttr: function(row, column, index) {
                var result = {};

                if (util.type(index) !== 'undefined') {
                    result['data-index'] = index;
                }
                result['data-name'] = column.name;
                result['data-value'] = row[column.name];
                var title = this.columnTitle(row, column);
                if (title) {
                    result.title = title;
                    /// result['data-title'] = title;
                    /// result['data-original-title'] = title;
                }
                /*var param = {
                };*/
                if (!util.isEmpty(column.href)) {
                    /// attr.type = 'a';
                    result.href = column.href;
                    result.click = this.href;
                    /// result.parent = this;
                } else if (column.type && column.type === 'operate') {
                    result.click = this.click;
                    result.operates = column.operates;
                }
                /*if (!util.isEmpty(column.sub)) {
                    param.sub = column.sub;
                    param.icon = 'icon-more';
                }*/
                /// param.$vm = this;

                if (!this.editable) {
                    // row[column.name + 'Static'] = this.columnFormat(row, column);
                    result['text'] = this.columnFormat(row, column);
                }

                result.name = column.name;
                /*if (!util.isEmpty(param)) {
                    result.param = param;
                }*/
                return result;
                // return attr;
            },
            onHeadClick: function(column, event) {
                var $head = $(event.target);
                if (!$head.hasClass('bv-order')) {
                    $head = $head.closest('th');
                }
                this.localBodyChecked = [];
                this.innerCheckedLength = 0;
                if ($head.hasClass('bv-order')) {
                    this.hideSub();
                    if ($head.hasClass('bv-order-asc')) {
                        this.localOrderType = 'desc';
                    } else if ($head.hasClass('bv-order-desc')) {
                        this.localOrderType = 'asc';
                    } else {
                        this.localOrderType = 'asc';
                    }
                    this.localOrderName = column.name;
                    this.innerOrderList = [{
                        name: this.localOrderName,
                        sort: this.localOrderType
                    }];
                    if (this.pagination) {
                        this.fill('order');
                    } else {
                        var orderName = this.localOrderName;
                        var orderType = this.localOrderType;

                        this.innerRows = util.orderBy(this.innerRows, orderName, !orderType || orderType === 'asc' ? 1 : -1);
                    }
                }
            },
            // event：事件，暂时不用
            // checked：表头是否选中
            // updateBody：表头选中状态不更新body
            onHeadCheck: function(event, checked, updateBody) {
                var $header = $('thead tr :checkbox', this.$el);
                if (util.type(checked) === 'undefined') {
                    // 点击触发
                    if (util.checked($header)) {
                        this.localHeadChecked = true;
                    } else {
                        this.localHeadChecked = false;
                    }
                } else {
                    // 手工触发
                    if (checked) {
                        this.localHeadChecked = true;
                        util.checked($header, true);
                    } else {
                        this.localHeadChecked = false;
                        util.checked($header, false);
                    }
                }
                if (updateBody) {
                    if (this.localHeadChecked) {
                        util.checked($('tbody :checkbox', this.$el), true);
                        this.localBodyChecked = [];
                        for (var i=0; i<this.innerRows.length; i++) {
                            this.localBodyChecked.push(i);
                        }
                    } else {
                        util.checked($('tbody :checkbox', this.$el), false);
                        this.localBodyChecked = [];
                    }
                    this.innerCheckedLength = this.localBodyChecked.length;
                }
                if (event) {
                    if (this.type === 'choose') {
                        this._chooseCompare();
                    }
                }
            },
            onBodyCheck: function(event, index) {
                var $check;
                if (util.type(index) !== 'undefined') {
                    $check = $(':checkbox', $('.bv-table-tbody tbody tr', this.$el).eq(index));
                    util.checked($check, true);
                } else {
                    $check = $(event.target);
                }
                var $tr = $check.closest('tr');

                // var $tr = $(event.target).closest('tr');
                if (this.select === 'checkbox') {
                    // var index = $tr.attr('data-index');
                    if (util.checked($check)) {
                        this.localBodyChecked.push(util.toNumber($tr.attr('data-index')));
                    } else {
                        var i = util.index(this.localBodyChecked, util.toNumber($tr.attr('data-index')));
                        if (i >= 0) {
                            this.localBodyChecked.splice(i, 1);
                        }
                    }
                    if (this.localBodyChecked.length === this.innerRows.length) {
                        this.onHeadCheck(undefined, true);
                    } else if (this.localBodyChecked.length === 0) {
                        this.onHeadCheck(undefined, false);
                    } else {
                        this.onHeadCheck(undefined, false);
                    }
                    this.innerCheckedLength = this.localBodyChecked.length;
                } else if (this.select === 'radio') {
                    this.localBodyChecked = [util.toNumber($tr.attr('data-index'))];
                    this.innerCheckedLength = this.localBodyChecked.length;
                }

                if (event) {
                    if (this.type === 'choose') {
                        this._chooseCompare();
                    }
                }
            },
            href: function(event, row, column) {
                if (!row) {
                    /// TODO: 待确认
                    var index = util.toNumber($(event.target).closest('tr').attr('data-index'));
                    if (!util.isEmpty(index) && index >= 0) {
                        row = this.innerRows[index];
                    }
                }
                if (!column) {
                    var name = $(event.target).attr('data-name');
                    if (name) {
                        for (var i=0; i<this.innerColumns.length; i++) {
                            if (name === this.innerColumns[i].name) {
                                column = this.innerColumns[i];
                            }
                        }
                    }
                }
                if (util.type(column.href) === 'object') {
                    if (column.href.type) {
                        if (column.href.type === 'sub') {
                            if (column.href.config) {
                                this.triggerSub(event, column.href.config);
                            }
                        }
                    }
                } else if (util.type(column.href) === 'function') {
                    if (this.localKeys.length === 1) {
                        column.href.call(null, event, this.name, row[this.localKeys[0]], row, column);
                    } else {
                        var idValues = {};
                        for (var i=0; i<this.localKeys.length; i++) {
                            idValues[this.localKeys[i]] = row[this.localKeys[i]];
                        }
                        column.href.call(null, event, this.name, idValues, row, column);
                    }
                }
            },
            doFilter: function(event) {
                this.hideSub();
                if (this.innerFilterType === 'page' && this.loadType !== 'false') {
                    this.onHeadCheck(undefined, false, true);
                    this.localBodyChecked = [];
                    this.innerCheckedLength = 0;
                    // 当前页查询
                    $('tbody tr', $(this.$el)).hide().filter(":contains('" + this.localFilter + "')").show();
                } else {
                    // 后台查询
                    this.fill('filter');
                }
            },
            doFilterMore: function(event) {
                if (event && event.which === 13) {
                    event.preventDefault();
                    return;
                }
                if (!util.validate($('#queryForm', this.$el))) {
                    return;
                }
                this.hideSub();
                this.filterMoreData();
            },
            filterMoreData: function() {
                this.localQuery = [];

                for (var i=0; i<this.innerFilterColumns.length; i++) {
                    var column = this.innerFilterColumns[i];
                    if (this.filterCheck(column, 'default')) {
                        if (this.innerFilterEntity[column.name + 'Filter']) {
                            var operate = column.filter.operate;
                            if (!operate) {
                                operate = 'like';
                            } else if (util.type(column.filter.operate) === 'function') {
                                operate = column.filter.operate.call(null, this.$name, this.innerFilterEntity);
                            }
                            var value = this.innerFilterEntity[column.name + 'Filter'];
                            if (util.type(column.filter.value) === 'function') {
                                value = column.filter.value.call(null, this.$name, this.innerFilterEntity);
                            }
                            this.localQuery.push({
                                name: column.name,
                                operate: operate,
                                value: value,
                                format: column.config.format || ''
                            });
                        }
                    } else if (this.filterCheck(column, 'between')) {
                        if (this.innerFilterEntity[column.filterConfig.start]) {
                            var valueStart = this.innerFilterEntity[column.filterConfig.start];
                            if (column.format === 'timestamp') {
                                valueStart = Date.parse(new Date(valueStart));
                            }
                            this.localQuery.push({
                                name: column.name,
                                operate: '>=',
                                value: valueStart,
                                format: column.config.format || ''
                            });
                        }
                        if (this.innerFilterEntity[column.filterConfig.end]) {
                            var valueEnd = this.innerFilterEntity[column.filterConfig.end];
                            if (column.format === 'timestamp') {
                                valueEnd = Date.parse(new Date(valueEnd));
                            }
                            this.localQuery.push({
                                name: column.name,
                                operate: '<=',
                                value: valueEnd,
                                format: column.config.format || ''
                            });
                        }
                    }
                }

                var vm = this;
                if (this.innerFilterType === 'page' && this.loadType !== 'false') {
                    if (vm.localQuery.length === 0) {
                        $("tbody tr", this.$el).show();
                    } else {
                        $("tbody tr", this.$el).hide().filter(function() {
                            var match = true;
                            for (var i=0; i<vm.localQuery.length; i++) {
                                var $element = $("[data-name='" + vm.localQuery[i].name + "']", $(this));
                                if (vm.localQuery[i].operate === 'like') {
                                    if ($element.text().indexOf(vm.localQuery[i].value) < 0 && $element.attr('data-value') != vm.localQuery[i].value) {
                                        match = false;
                                    }
                                } else {
                                    if ($element.text() !== vm.localQuery[i].value && $element.attr('data-value') != vm.localQuery[i].value) {
                                        match = false;
                                    }
                                }
                            }
                            return match;
                        }).show();
                    }
                } else {
                    this.fill('filter');
                }
            },
            initFilterMore: function(event, type) {
                if (type && type !== 'reset') {
                    this.innerFilterMore = !this.innerFilterMore;
                }
                if (this.innerFilterMore) {
                    // 显示精确查询
                    $('.bv-table-filter .bv-left', this.$el).hide();
                    $('.bv-table-filter-more', this.$el).show();
                    $('.bv-table-filter .bv-right #filterToggleButton i', this.$el).removeClass('icon-more').addClass('icon-less');
                    $('.bv-table-filter .bv-right #filterToggleButton span', this.$el).text('模糊查询');
                } else {
                    // 显示模糊查询
                    $('.bv-table-filter-more', this.$el).hide();
                    $('.bv-table-filter .bv-left', this.$el).show();
                    $('.bv-table-filter .bv-right #filterToggleButton i', this.$el).removeClass('icon-less').addClass('icon-more');
                    $('.bv-table-filter .bv-right #filterToggleButton span', this.$el).text('精确查询');
                }
                this.localFilter = '';
                $('#filter', this.$el).val('');
                this.innerFilterEntity = this.localFilterEntity;

                if (type && type === 'reset') {
                    this.doFilterMore();
                }

                this.calcHeight('filter');
            },
            initOrder: function(name) {
                if (this.innerOrderList && this.innerOrderList.length > 0) {
                    for (var i=0; i<this.innerOrderList.length; i++) {
                        if (name === this.innerOrderList[i].name) {
                            return !this.innerOrderList[i].sort || this.innerOrderList[i].sort === 'asc' ? 'bv-order-asc' : 'bv-order-desc';
                        }
                    }
                }
            },
            columnShowOrHide: function(column) {
                if (util.isTrue(column.hide)) {
                    return false;
                }
                return util.isTrue(column.show, true);
            },
            filterName: function(el, type, name) {
                if (this.filterCheck(el, type)) {
                    return name;
                }
                return name + '-' + type;
            },
            filterCheck: function(el, type) {
                if (type === 'default') {
                    return !el.filter.operate || el.filter.operate === 'like' || el.filter.operate === '='
                        || el.filter.operate === '>' || el.filter.operate === '>='
                        || el.filter.operate === '<' || el.filter.operate === '<='
                        || el.filter.operate === 'in' || el.filter.operate === 'is' || el.filter.operate === 'is not'
                        || el.filter.operate === 'custom' || util.type(el.filter.operate) === 'function';
                    // return !el.filter.operate || el.filter.operate === 'like' || el.filter.operate === '=' || el.filter.operate === 'select';
                } else if (type === 'between') {
                    return el.filter.operate === 'between';
                }
                if (type === 'text') {
                    return !el.filter.type || el.filter.type === 'text';
                }
                return el.filter.type === type;
            },
            pressOnFilter: function(event) {
                if (event.which === 13) {
                    event.preventDefault();
                    this.doFilter();
                }
                this.localFilter = $(event.target).val();
            },
            pressOnLimit: function(event) {
                if (event.which === 13) {
                    event.preventDefault();
                    $(event.target).tooltip('hide');
                    this.setLimit(event);
                }
            },
            setLimit: function(event) {
                var $target = $('.bv-page-limit input', this.$el);
                var v = $target.val();
                if (!util.check(v, /^\+?[1-9][0-9]*$/)) {
                    v = 10;
                    $target.val(v);
                }
                if (util.toNumber(v) > 500) {
                    this.innerLimit = 500;
                } else {
                    this.innerLimit = util.toNumber(v);
                }
                this.hideSub();
                // 保存每页显示数到localStorage
                util.storage('limit', this.innerLimit, this.name);
                this.fill('limit');
            },
            pressOnPage: function(event) {
                if (event.which === 13) {
                    event.preventDefault();
                    $(event.target).tooltip('hide');
                    this.setPage(event);
                }
            },
            setPage: function(event) {
                var $target = $('.bv-page-current input', this.$el);
                var v = $target.val();
                if (!util.check(v, /^\+?[1-9][0-9]*$/)) {
                    v = 1;
                    $target.val(v);
                }
                v = util.toNumber(v);
                if (v > this.innerTotalPage) {
                    v = this.innerTotalPage;
                    $target.val(v);
                }
                this.hideSub();
                this.fill(v);
            },
            /*            tableScroll: function() {
             util.scroll($(this.$el).closest('.bv-content'));
             // util.scroll($('.bv-table-body', this.$el));
             },*/
            triggerSub: function(event, sub) {
                var $trigger = $(event.target);
                if ($trigger.is('i')) {
                    $trigger = $trigger.closest('a');
                }
                if ($('i', $trigger).hasClass('icon-more')) {
                    $('i.icon-less', $trigger.closest('table')).removeClass('icon-less').addClass('icon-more');
                    $('<tr class="bv-table-sub-container-tr"><td colspan="' + this.innerColumnNum + '"></td></tr>').insertAfter($trigger.closest('tr'));
                    this.hideSub();
                    $('#' + sub.id + '-container').show().appendTo($('td', $trigger.closest('tr').next()));
                    var subVm = util.vm(this.$parent, sub.id);
                    if (subVm && sub.initParamList) {
                        var initParamList = sub.initParamList;
                        for (var i=0; i<initParamList.length; i++) {
                            var param = initParamList[i];
                            var index = $trigger.closest('tr').attr('data-index');
                            if (param.name && param.from) {
                                Const.global.currentTableRow = util.clone(this.innerRows[index]);   // util.find(this.innerRows, this.id, idValue);
                                if (Const.global.currentTableRow) {
                                    param.value = Const.global.currentTableRow[param.from];
                                    initParamList[i] = param;
                                }
                            }
                        }
                        subVm.refresh(undefined, initParamList);
                    }
                    $('i', $trigger).removeClass('icon-more').addClass('icon-less');
                } else {
                    $('i', $trigger).removeClass('icon-less').addClass('icon-more');
                    $('#' + sub.id + '-container').hide().insertAfter($trigger.closest('.bv-table-container'));
                    $trigger.closest('tr').next().remove();
                }

                this.calcHeight('triggerSub');
//                this.tableScroll();
            },
            hideSub: function() {
                if (!this.type || this.type !== 'sub') {
                    var $container = this.$el;
                    $('.bv-table-sub', $container).each(function() {
                        var $tr = $(this).closest('tr');
                        // TODO:
                        $(this).closest('.bv-table').hide().insertAfter($container);
                        $tr.remove();
                    });
                    $('table i.icon-less', $container).removeClass('icon-less').addClass('icon-more');

                    this.calcHeight('hideSub');
//                    this.tableScroll();
                }
            },
            click: function(event, operate, position) {
                var selected = [];
                if (position === 'head') {
                    // 表头按钮
                    if (operate && util.type(operate.check) === 'function') {
                        selected = this.innerRows;
                    } else {
                        selected = util.selected(this);
                    }
                } else if (position === 'body') {
                    // body按钮
                    var index = util.toNumber($(event.target).closest('tr').attr('data-index'));
                    if (!util.isEmpty(index) && index >= 0) {
                        selected.push(this.innerRows[index]);
                    }
                }
                if (util.type(operate.click) === 'function') {
                    operate.click.call(this, event, this.name, this.entityName, selected);
                } else if (operate.type) {
                    if (operate.type === 'insert') {
                        if (this.editable) {
                            this.innerRows.push(this.localInitRow);
                        } else {
                            if (operate.url) {
                                if (operate.prepare && util.type(operate.prepare) === 'function') {
                                    operate.prepare.call(null);
                                }
                                util.modal({
                                    url: operate.url,
                                    refresh: true,
                                    vm: this
                                });
                            }
                        }
                    } else if (operate.type === 'update') {
                        if (operate.url) {
                            if (operate.prepare && util.type(operate.prepare) === 'function') {
                                operate.prepare.call(null);
                            }
                            util.modal({
                                url: util.mix(operate.url, {
                                    type: 'update'
                                }),
                                data: (selected && selected.length > 0) ? selected[0] : (this.innerRows && this.innerRows.length > 0 ? this.innerRows[0] : {}),
                                refresh: true,
                                vm: this
                            });
                        }
                    } else if (operate.type === 'save') {
                        if (this.editable) {
                            // TODO: 保存
                            // console.log(this.innerRows);
                        }
                    } else if (operate.type === 'delete') {
                        util.confirm({
                            type: 'delete',
                            url: operate.url || '',
                            vm: this
                        });
                    } else if (operate.type === 'redirect') {
                        if (operate.url) {
                            util.redirect();
                        }
                    } else if (operate.type === 'post') {
                        if (operate.url) {
                            // post方式调用url，参数为当前选中数据第一条对应的id
                            util.post({
                                url: operate.url,
                                data: util.id(this.localKeys, selected[0]),
                                refresh: true,
                                vm: this
                            });
                        }
                    } else if (operate.type === 'sub') {
                        if (operate.config) {
                            this.triggerSub(event, operate.config);
                        }
                    }
                } else if (operate.show === 'return') {
                    if (operate.url) {
                        util.redirect(operate.url, 'body');
                    }
                }
            },
            isRequired: function(validate, attr) {
                return util.isRequired(validate, attr);
            },
            confirmChoose: function(event) {
                var codes = '';
                var descs = '';
                for (var i=0; i<this.innerChooseResult.length; i++) {
                    if (codes) {
                        codes += ',';
                        descs += ',';
                    }
                    codes += this.innerChooseResult[i].code;
                    descs += this.innerChooseResult[i].desc;
                }
                this.$emit('on-choose', this.name, {
                    codes: codes,
                    descs: descs
                });
                util.modal('hide');
            },
            _chooseCompare: function() {
                if (this.innerRows && this.innerRows.length > 0 && this.innerChooseResult && this.innerChooseResult.length > 0) {
                    for (var i=0; i<this.innerRows.length; i++) {
                        var index = util.index(this.innerChooseResult, this.innerRows[i][this.keys], 'code');
                        if (index >= 0) {
                            this.innerChooseResult.splice(index, 1);
                        }
                    }
                }
                // 处理本页选择
                if (this.localBodyChecked && this.localBodyChecked.length > 0) {
                    for (var i=0; i<this.localBodyChecked.length; i++) {
                        var row = this.innerRows[this.localBodyChecked[i]];
                        if (this.innerChooseResult && this.innerChooseResult.length > 0) {
                            var found = false;
                            for (var j=0; j<this.innerChooseResult.length; j++) {
                                if (this.innerChooseResult[j][this.keys] === row[this.keys]) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                this.innerChooseResult.push({
                                    code: row[this.keys],
                                    desc: row[this.innerChooseDesc],
                                    show: util.type(this.chooseShow) === 'function' ? this.chooseShow.call(null, row) : row[this.innerChooseDesc]
                                });
                            }
                        } else {
                            this.innerChooseResult.push({
                                code: row[this.keys],
                                desc: row[this.innerChooseDesc],
                                show: util.type(this.chooseShow) === 'function' ? this.chooseShow.call(null, row) : row[this.innerChooseDesc]
                            });
                        }
                    }
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="bv-table" :class="[{'modal-dialog': innerLayout === 'modal', 'modal-lg': innerLayout === 'modal', 'table-collapse': collapse}, innerContainerClass]" :style="innerStyle" v-bind="{id: innerName + '-container'}">
                <div :class="{'modal-content': innerLayout === 'modal'}">
                    <div class="modal-header no-line" v-if="innerLayout === 'modal'">
                        <button type="button" class="close" data-dismiss="modal">
                            &times;
                        </button>
                    </div>
                    <div :class="[{'modal-body': innerLayout === 'modal'}, innerClass]" v-bind="{id: innerName}" class="bv-table-container">
                        <div class="bv-table-toolbar">
                            <div class="bv-table-title">
                                <a href="javascript:;" @click="innerVisible = !innerVisible" v-if="collapse"><i class="iconfont" :class="innerVisible ? 'icon-more' : 'icon-gt'"></i><span v-text="title"></span></a>
                                <span v-if="!collapse"><i class="iconfont icon-menu"></i>{{title}}</span>
                            </div>
                            <div class="bv-table-operate">
                                <button type="button" class="btn btn-default" v-if="innerAuthedOperates.length > 0 && el.show !== 'import' && el.show !== 'export' && el.text"
                                        v-for="el in innerAuthedOperates" v-bind="{id: el.id}"
                                        v-show="isButtonVisible(el)" @click="click($event, el, 'head')">
                                    <i class="iconfont" v-if="el.icon" :class="el.icon"></i>{{el.text}}
                                </button>
                            </div>
                        </div>
                        <div class="bv-table-filter" v-if="innerFilterType !== 'false'" v-show="innerVisible">
                            <div class="bv-left">
                                <div class="input-group" v-show="!innerFilterMore" v-if="filterLayout !== 'filterMore'">
                                    <div class="input-group-addon" v-if="innerLimitFilterColumns.length > 0">
                                        <select class="form-control" v-model="innerLimitFilterColumnName">
                                            <option v-for="limitColumn in innerLimitFilterColumns" v-bind="{value: limitColumn.name}">{{limitColumn.head}}</option>
                                        </select>
                                    </div>
                                    <input type="text" id="filter" class="form-control" placeholder="请输入查询条件" @keyup="pressOnFilter($event)" />
                                    <span class="input-group-addon">
                                        <i class="bv-icon-button iconfont icon-query" @click="doFilter($event)"></i>
                                    </span>
                                </div>
                            </div>
                            <div class="bv-right">
                                <button type="button" id="filterToggleButton" class="btn btn-link" v-if="innerFilterColumns.length > 0 && filterLayout === 'default'"
                                        @click="initFilterMore($event, 'change')">
                                    <i class="iconfont" :class="innerFilterMore ? 'icon-less' : 'icon-more'"></i>
                                    <span v-text="innerFilterMore ? '模糊查询' : '精确查询'"></span>
                                </button>
                                <button type="button" class="btn btn-link" v-for="el in innerAuthedOperates" v-bind="{id: el.id}"
                                        v-if="el.show === 'import'" v-show="isButtonVisible(el)" @click="toImport($event, el)">
                                    <i class="iconfont" v-if="el.icon" :class="el.icon"></i>{{el.text}}
                                </button>
                                <button type="button" class="btn btn-link" v-for="el in innerAuthedOperates" v-bind="{id: el.id}"
                                        v-if="el.show === 'export'" v-show="isButtonVisible(el)" @click="toExport($event, el)">
                                    <i class="iconfont" v-if="el.icon" :class="el.icon"></i>{{el.text}}
                                </button>
                                <button type="button" class="btn btn-link" @click="refresh($event)"><i class="iconfont icon-refresh"></i>刷新</button>
                            </div>
                        </div>
                        <div class="bv-table-filter-more" v-show="innerVisible && innerFilterMore" v-if="filterLayout !== 'filter' && innerFilterColumns.length > 0">
                            <form id="queryForm" class="form-horizontal">
                                <div>
                                    <div v-for="(el, index) in innerFilterColumns" class="bv-filter-container" :class="el.filterLayout.containerClass">
                                        <label class="control-label" :class="el.filterLayout.labelClass" :for="el.filterLayout.for">
                                            <i class="required iconfont icon-required" v-if="isRequired(el.config.validate, el.config.attr)"></i>{{el.head}}：
                                        </label>
                                        <div :class="el.filterLayout.tagClass">
                                            <component :is="el.filterConfig.tagName" :key="innerName + '-' + el.name + '-filter' + '-' + index" :entity="innerFilterEntity" v-bind="[el.config, el.filterConfig]"></component>
                                        </div>
                                    </div>
                                    <div class="col-sm-6 col-md-4 col-lg-3 bv-filter-container">
                                        <button type="button" class="btn btn-primary" @click="doFilterMore($event)"><i class="iconfont icon-query"></i>查询</button>
                                        <button type="reset" class="btn btn-default" @click="initFilterMore($event, 'reset')"><i class="iconfont icon-init"></i>重置</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div class="table-responsive" v-show="innerVisible">
                            <div class="bv-table-body" :class="innerFixed && 'bv-table-fixed'">
                                <table class="table table-striped table-hover bv-table-thead" style="display:none;" v-if="innerFixed">
                                    <thead>
                                    <tr class="bv-table-thead-tr">
                                        <th v-if="select === 'checkbox'" class="center" :style="{width: innerWidth.checkbox}">
                                            <input type="checkbox" @click="onHeadCheck($event, undefined, true)" />
                                        </th>
                                         <th v-if="select === 'radio'" class="center" :style="{width: innerWidth.radio}">
                                            <span>选择</span>
                                         </th>
                                        <th v-if="linenumber !== 'false'" class="center" :style="{width: innerWidth.linenumber}">
                                            <span>序号</span>
                                        </th>
                                        <th v-for="el in innerColumns"
                                                v-show="columnShowOrHide(el)"
                                                :class="[el.align || '', {'fixed': el.width}, {'bv-order': (!el.type || el.type !== 'operate') && (!el.order || el.order !== 'false')}, initOrder(el.name)]"
                                                :style="{'min-width': el.width || 0}"
                                                @click="(!el.order || el.order !== 'false') && innerDataInited && onHeadClick(el, $event)">
                                            <span>{{el.head}}</span><i class="iconfont icon-asc"></i><i class="iconfont icon-desc"></i>
                                        </th>
                                    </tr>
                                    <tr class="bv-progress-container">
                                        <th v-bind="{colspan: innerColumnNum}">
                                            <div class="progress bv-progress-line">
                                                <div class="progress-bar">
                                                </div>
                                            </div>
                                        </th>
                                    </tr>
                                    </thead>
                                </table>
                                <table class="table table-striped table-hover bv-table-tbody">
                                    <thead>
                                    <tr>
                                        <th v-if="select === 'checkbox'" class="center" :style="{width: innerWidth.checkbox}">
                                            <input type="checkbox" @click="onHeadCheck($event, undefined, true)" />
                                        </th>
                                         <th v-if="select === 'radio'" class="center" :style="{width: innerWidth.radio}">
                                            <span>选择</span>
                                         </th>
                                        <th v-if="linenumber !== 'false'" class="center" :style="{width: innerWidth.linenumber}">
                                            <span>序号</span>
                                        </th>
                                        <th v-for="el in innerColumns"
                                            v-show="columnShowOrHide(el)"
                                            :class="[el.align || '', {'fixed': el.width}, {'bv-order': (!el.type || el.type !== 'operate') && (!el.order || el.order !== 'false')}, initOrder(el.name)]"
                                            :style="{'width': el.width || 0}"
                                            @click="(!el.order || el.order !== 'false') && innerDataInited && onHeadClick(el, $event)">
                                            <span>{{el.head}}</span><i class="iconfont icon-asc"></i><i class="iconfont icon-desc"></i>
                                        </th>
                                    </tr>
                                    <tr class="bv-progress-container" v-if="!innerFixed">
                                        <th v-bind="{colspan: innerColumnNum}">
                                            <div class="progress bv-progress-line" style="display:none">
                                                <div class="progress-bar">
                                                </div>
                                            </div>
                                        </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr v-for="(tr, i) in innerRows" :class="{'bv-odd': i%2 === 0, 'bv-even': i%2 !== 0}" v-bind="{'data-index': i}">
                                        <td v-if="select === 'checkbox'" class="center">
                                            <input type="checkbox" v-bind="{value: i}" @click="onBodyCheck($event)" />
                                        </td>
                                        <td v-if="select === 'radio'" class="center">
                                            <input type="radio" v-bind="{name: innerName + '-radio', value: i}" @click="onBodyCheck($event)" />
                                        </td>
                                        <td v-if="linenumber !== 'false'" class="center">
                                            <span>{{linenumber === 'page' ? i + 1 : innerOffset + i + 1}}</span>
                                        </td>
                                        <td v-for="(el, j) in innerColumns" v-show="columnShowOrHide(el)" :class="el.align || ''">
                                            <component :is="'bv-' + el.type" :name="el.name" :from="'table'" :entity="tr" v-bind="[columnAttr(tr, el, i), el.config, el.editConfig]"></component>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                            <div class="bv-table-footer" v-if="pagination">
                                <div class="bv-page-action">
                                    <span><i class="iconfont icon-first" data-title="第一页" :class="{'invalid' : innerCurrentPage === 1}" @click="innerCurrentPage !== 1 && jumpTo('first', $event)"></i></span>
                                    <span><i class="iconfont icon-prev" data-title="上一页" :class="{'invalid' : innerCurrentPage === 1}" @click="innerCurrentPage !== 1 && jumpTo('prev', $event)"></i></span>
                                    <span><i class="iconfont icon-next" data-title="下一页" :class="{'invalid' : innerCurrentPage === innerTotalPage || innerTotalPage === 0}" @click="innerCurrentPage !== innerTotalPage && innerTotalPage !== 0 && jumpTo('next', $event)"></i></span>
                                    <span><i class="iconfont icon-last" data-title="最后一页" :class="{'invalid' : innerCurrentPage === innerTotalPage || innerTotalPage === 0}" @click="innerCurrentPage !== innerTotalPage && innerTotalPage !== 0 && jumpTo('last', $event)"></i></span>
                                </div>
                                <div class="bv-page-detail">总页数{{innerTotalPage}}，总记录数{{innerRowCount}}</div>
                                <div class="bv-page-limit">
                                    <span class="bv-label">每页记录数：</span>
                                    <div class="input-group">
                                        <input type="text" maxlength="4" data-title="请输入每页记录数" v-bind="{value: innerLimit}" @keypress="pressOnLimit($event)" />
                                        <span class="input-group-addon">
                                            <i class="bv-icon-button iconfont icon-ok" @click="setLimit($event)"></i>
                                        </span>
                                    </div>
                                </div>
                                <div class="bv-page-current">
                                    <span class="bv-label">当前页：</span>
                                    <div class="input-group">
                                        <input type="text" maxlength="6" data-title="请输入页码" v-bind="{value: innerCurrentPage}" @keypress="pressOnPage($event)" />
                                        <span class="input-group-addon">
                                            <i class="bv-icon-button iconfont icon-ok" @click="setPage($event)"></i>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer" v-if="type === 'choose'">
                        <div style="width: 100%; text-align: left;"><label>当前已选：</label><span v-for="(el, $index) in innerChooseResult"><span v-if="$index > 0">,</span>{{el.show}}</span></div>
                         <button type="button" class="btn btn-primary" @click="confirmChoose($event)" v-if="innerLayout === 'modal'">
                            <i class="iconfont icon-ok"></i>确定选择
                         </button>
                         <button type="button" class="btn btn-default" data-dismiss="modal" v-if="innerLayout === 'modal'">
                            <i class="iconfont icon-cancel"></i>关闭
                         </button>
                    </div>
                </div>
             </div>
             */
        })
    });
});