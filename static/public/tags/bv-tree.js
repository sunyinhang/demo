define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-tree', {
        props: {
            entity: '',
            name: '',

            id: '',
            clazz: '',
            css: '',
            // 属性定义
            attr: '',
            // 类型，支持：menu-菜单
            type: '',
            // 初始化数据url
            url: {
                default: Const.url.tree.query
            },
            // 初始化method
            method: {
                default: 'post'
            },
            entityName: '',
            initParamList: '',
            orders: '',
            orderList: '',
            initEntity: '',
            // 是否显示图标
            icon: '',
            // 是否显示连接线
            line: '',
            // 是否允许选择
            check: '',
            // 根节点
            rootNode: '',

            // 函数，初始化完成调用
            /// onInit: '',
            // 函数，组装数据
            pack: ''
            // 函数，展开前
            /// beforeExpand: '',
            // 展开时
            /// onExpand: '',
            // 函数，点击触发
            /// onClick: ''
        },
        data: function() {
            return {
                innerEntity: this.entity,
                innerClass: this.clazz,
                innerStyle: this.css,
                innerAttr: this.attr || {},
                innerInitParamList: this.initParamList || {},
                innerOrderList: this.orderList,
                innerInitEntity: this.initEntity || {}
            };
        },
        beforeCreated: function () {
            this.localNodes = [];
            // 树的jquery对象
            this.localTree = '';
            this.localConfig = '';
        },
        created: function() {
            if (this.id) {
                this.innerAttr.id = this.id;
            }
            this.innerOrderList = util.transOrder(this.innerOrderList, this.orders);
        },
        mounted: function() {
            this.init();
        },
        methods: {
            init: function() {
                var callback = {};
                /*if (util.type(this.beforeExpand) === 'function') {
                    callback.beforeExpand = this.beforeExpand;
                }
                if (util.type(this.onExpand) === 'function') {
                    callback.onExpand = this.onExpand;
                }
                if (util.type(this.onClick) === 'function') {
                    callback.onClick = this.onClick;
                }*/
                var vm = this;
                callback.beforeExpand = function (treeId, treeNode) {
                    /*var menuTree = $.fn.zTree.getZTreeObj(treeId);
                    // 展开的所有节点，这是从父节点开始查找（也可以全文查找）
                    var expandedNodes = menuTree.getNodesByParam('open', true, treeNode.getParentNode());

                    for (var i = expandedNodes.length - 1; i >= 0; i--) {
                        var node = expandedNodes[i];
                        if (treeNode.id != node.id && node.level == treeNode.level && node.level !== 0) {
                            menuTree.expandNode(node, false);
                        }
                    }*/
                    vm.$emit('before-expand', treeId, treeNode);
                };
                callback.onExpand = function () {
                    vm.$emit('on-expand');
                };
                callback.onClick = function (e, treeId, treeNode) {
                    if (vm.type === 'menu' && treeNode.isParent) {
                        util.expand(treeId, treeNode);
                    }
                    vm.$emit('on-click', e, treeId, treeNode);
                };
                if (this.check) {
                    var vm = this;
                    callback.onCheck = function(event, treeId, treeNode) {
                        var checked = [];
                        var checkedNodes = vm.localTree.getCheckedNodes();
                        if (checkedNodes && checkedNodes.length > 0) {
                            for (var i=0; i<checkedNodes.length; i++) {
                                if (checkedNodes[i].entity) {
                                    checked = checked.concat(checkedNodes[i].entity);
                                }
                            }
                        }
                        if (vm.name) {
                            vm.innerEntity[vm.name] = checked;
                        }
                    };
                }
                this.localConfig = {
                    view: {
                        showIcon: this.icon,
                        showLine: this.line,
                        showTitle: false,
                        selectedMulti: !this.check,
                        dblClickExpand: false
                    },
                    check: {
                        enable: this.check
                    },
                    data: {
                        simpleData: {
                            enable: true
                        }
                    },
                    callback: callback
                };
                var data = {};
                /*if (vm.appCode) {
                    data.appCode = vm.appCode;
                }*/
                if (vm.entityName) {
                    data.entityName = vm.entityName;
                }
                if (!util.isEmpty(vm.innerInitParamList)) {
                    data.initParamList = vm.innerInitParamList;
                }
                if (!util.isEmpty(vm.innerInitEntity)) {
                    data = util.mix(data, vm.innerInitEntity);
                }
                if (vm.innerOrderList && vm.innerOrderList.length > 0) {
                    data.orderList = vm.innerOrderList;
                }
                if (util.type(vm.url) === 'string') {
                    if (util.endsWith(vm.url, ".json")) {
                        $.getJSON(vm.url, function(res) {
                            vm.dataInit(res);
                        });
                    } else if (vm.method) {
                        util[vm.method]({
                            url: vm.url,
                            //appCode: vm.appCode,
                            data: data,
                            success: function(res) {
                                if (util.data(res)) {
                                    vm.dataInit(util.data(res));
                                }
                            }
                        });
                    }
                } else if (util.type(vm.url) === 'array') {
                    vm.dataInit(vm.url);
                }
            },
            dataInit: function(data) {
                if (data) {
                    this.localNodes = [];
                    if (this.rootNode) {
                        this.localNodes.push(this.rootNode);
                    }
                    for (var i=0; i<data.length; i++) {
                        this.localNodes.push(this.pack.call(null, data[i]));
                    }
                    this.localTree = $.fn.zTree.init($(this.$el), this.localConfig, this.localNodes);

                    this.$emit('on-init', this);
                    /*if (util.type(this.onInit) === 'function') {
                        this.onInit.call(null, this);
                    }*/
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <ul class="ztree" :class="innerClass" :style="innerStyle" v-bind="innerAttr"></ul>
            */
        })
    });
});