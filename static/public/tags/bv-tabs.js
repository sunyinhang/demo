define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-tabs-pane', {
        props: {
            // 无实际意义，用于监听变动
            timestamp: '',
            target: '',
            prop: ''
        },
        watch: {
            timestamp: function (val, oldVal) {
                this.refresh();
            }
        },
        methods: {
            refresh: function () {
                Const.global.currentVm = this;
                var vm = this;
                util.replace($(vm.$el), vm.target, function(response, status, xhr) {
                    util.scroll($(vm.$el));
                    // 设置属性
                    if (vm.prop) {
                        $(vm.$el).data('tab-prop', vm.prop);
                    }
                });
            }
        },
        mounted: function () {
            this.refresh();
            /// util.cache(this);
        },
        template: util.heredoc(function() {
            /*!
            <div class="bv-content"></div>
            */
        })
    });
    vue.component('bv-tabs', {
        props: {
            clazz: '',
            // 属性定义
            attr: '',
            // 可设置为menu-表示系统菜单，inline-标签页body都在当前页面中
            type: {
                default: 'inline'
            },
            // 标签页不可关闭
            sticky: {
                default: false
            },
            // 初始标签页
            tabs: {
                default: function () {
                    return [];
                }
            },
            // 返回按钮
            returnUrl: ''
        },
        data: function() {
            return {
                innerClass: this.clazz,
                innerAttr: this.attr,
                // 是否翻页
                innerOverflow: false,
                // 当前页
                innerCurrentIndex: 0,
                // changed: false,
                // 标签页左边距，用于翻页
                innerMarginLeft: 0
            };
        },
        watch: {
            innerCurrentIndex: function (val, oldVal) {
                this.localCurrentSelected = this.tabs[this.innerCurrentIndex];
                this.calc();

                if (this.type === 'menu') {
                    // tab用，内部用
                    /*positionTitles: function() {
                        var titles = $('.bv-content[data-active="true"]').data('tab-prop') && $('.bv-content[data-active="true"]').data('tab-prop').titles;
                        if (this.type(titles) === 'string') {
                            return new Array({
                                text: titles
                            });
                        }
                        return titles;
                    },*/
                    var titles = this.localCurrentSelected.prop.titles; ///util.positionTitles();
                    if (util.type(titles) === 'string') {
                        titles = new Array({
                            text: titles
                        });
                    }
                    if (titles) {
                        root.position.titles = titles;
                    }
                    // 菜单
                    var treeNode = Const.global.menuTree.localTree.getNodeByParam('id', this.localCurrentSelected.menuId);
                    util.selectNode(treeNode);
                    // 调试信息
                    util.debug('菜单地址:', treeNode && treeNode.entity && treeNode.entity[Const.menu.url]);
                } else if (this.type === 'inline') {
                    $('.tab-content .tab-pane.active', this.$el).removeClass('active');
                    $('.tab-content .tab-pane[id=' + this.localCurrentSelected.target + ']', this.$el).addClass('active');
                }
            }
        },
        beforeCreate: function () {
            // 容器宽度
            this.localWidth = 0;
            // 去掉左右翻页后的容器宽度
            this.localContainerWidth = 0;
            // 标签页标题宽度和
            this.localNavWidth = 0;
            // 当前标签页左侧标题宽度和
            this.localLeftTabsWidth = 0;
            // 最后一个标签页标题宽度
            this.localLastTabsWidth = 0;
            // 当前选中标签页配置
            this.localCurrentSelected = null;
            // 当前选中标签页元素
            this.$localCurrentElement = null;
        },
        mounted: function() {
            this.$emit('on-init', this);
            this.localWidth = $(this.$el).outerWidth(true);

            if (this.type !== 'inline') {
                /// this.refresh();
                // util.loadTab(this, this.tabs[0]);
                this.calc();
                /*this.$watch('changed', function() {
                    this.calc();
                });*/

                var vm = this;
                $(window).resize(function () {
                    vm.localWidth = $(vm.$el).outerWidth(true);
                    vm.calc();
                });
            }
        },
        methods: {
            // 计算标签页宽度
            // 调用来源：初始加载，切换标签页，窗口大小改变
            calc: function() {
                var vm = this;
                vm.$nextTick(function () {
                    // 当前选中标签页对应li
                    vm.$localCurrentElement = $('[data-target=' + vm.tabs[vm.innerCurrentIndex].id + ']', vm.$el);
                    vm.localNavWidth = 0;
                    vm.localLeftTabsWidth = 0;
                    for (var i=0; i<vm.tabs.length; i++) {
                        var w = $('[data-target=' + vm.tabs[i].id + ']', vm.$el).outerWidth(true);
                        vm.localNavWidth += w;
                        if (i < vm.innerCurrentIndex) {
                            vm.localLeftTabsWidth += w;
                        }
                        if (i === vm.tabs.length - 1) {
                            vm.localLastTabsWidth = w;
                        }
                    }
                    vm.innerOverflow = vm.localNavWidth > vm.localWidth;

                    if (vm.innerOverflow) {
                        vm.localContainerWidth = vm.localWidth - 60;

                        var currentElementLeft = vm.$localCurrentElement.position().left;
                        var currentElementWidth = util.width(vm.$localCurrentElement);
                        var diff = vm.localLeftTabsWidth + currentElementWidth - vm.localContainerWidth;

                        if (diff > -1 * vm.innerMarginLeft) {
                            vm.innerMarginLeft = -1 * diff;
                        } else if (currentElementLeft < 0) {
                            vm.innerMarginLeft = -1 * vm.localLeftTabsWidth;
                        }

                    } else {
                        vm.innerMarginLeft = 0;
                        vm.localContainerWidth = vm.localWidth;
                    }
                });
            },
            prev: function() {
                var innerMarginLeft = this.innerMarginLeft + this.localContainerWidth;
                if (innerMarginLeft > 0) {
                    innerMarginLeft = 0;
                }
                this.innerMarginLeft = innerMarginLeft;
                /*if (this.innerMarginLeft > this.localContainerWidth - 80) {
                    this.innerMarginLeft -= this.localContainerWidth - 80;
                } else {
                    this.innerMarginLeft = 0;
                }*/
            },
            next: function() {
                var innerMarginLeft = this.innerMarginLeft - this.localContainerWidth;
                if (-1 * innerMarginLeft > this.localNavWidth - this.localContainerWidth + this.localLastTabsWidth) {
                    innerMarginLeft = this.localContainerWidth - this.localNavWidth;
                }
                this.innerMarginLeft = innerMarginLeft;
                /*if (this.innerMarginLeft + this.localContainerWidth * 2 - 80 >= this.localNavWidth) {
                    this.innerMarginLeft = this.localNavWidth - this.localContainerWidth;
                } else {
                    this.innerMarginLeft += this.localContainerWidth - 80;
                }*/
            },
            trigger: function(event, index) {
                if (index !== this.innerCurrentIndex) {
                    this.innerCurrentIndex = index;
                    // 触发resize重设table宽度
                    var click = this.tabs[this.innerCurrentIndex].click;
                    if (click && click === 'refresh') {
                        this.refresh();
                        // util.loadTab(this.tabs, this.tabs[this.innerCurrentIndex]);
                    }
                    // TODO: 是否需要?
                    // util.resize();
                }
            },
            refresh: function () {
                this.tabs[this.innerCurrentIndex].timestamp = new Date().getTime();
            },
            remove: function ($event, index) {
                // var $remove = $('#' + this.tabs[index].id, this.$el);
                // util.remove($remove);
                var child = this.$children[index];
                this.tabs.splice(index, 1);

                if (this.innerCurrentIndex >= index) {
                    this.innerCurrentIndex--;
                } else {
                    this.calc();
                }
                /// this.localCurrentSelected = this.tabs[this.innerCurrentIndex];
                /// this.calc();
            },
            removeAll: function () {
                /// this.localCurrentSelected = this.tabs[this.innerCurrentIndex];
                for (var i=this.tabs.length-1; i>=0; i--) {
                    var tab = this.tabs[i];
                    if (!this.sticky && !tab.sticky) {
                        /*var $remove = $('#' + tab.id, this.$element);
                        $('*', $remove).remove()
                        $remove.remove();*/
                        this.tabs.splice(i, 1);
                    }
                }
                this.innerCurrentIndex = 0;
            },
            doReturn: function() {
                if (this.returnUrl) {
                    util.redirect(this.returnUrl, 'body');
                }
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div :class="'bv-tabs-' + type" v-bind="innerAttr">
                <div class="bv-tabs-container" :class="[innerClass, {'bv-tabs-innerOverflow': innerOverflow}]">
                    <button class="btn btn-default prev" v-show="innerOverflow" @click="prev"><i class="iconfont icon-prev"></i></button>
                    <div class="bv-tabs-nav">
                        <ul class="nav nav-tabs" :style="{'margin-left': innerMarginLeft + 'px'}">
                            <li v-for="(el, index) in tabs" role="presentation" v-bind="{'data-target': el.id}" :class="index === innerCurrentIndex && 'active'">
                                <a href="javascript:;" @click="trigger($event, index)">{{el.text}}</a>
                                <button type="button" class="close" v-if="!sticky && !el.sticky" @click="remove($event, index)">
                                    ×
                                </button>
                            </li>
                        </ul>
                    </div>
                    <button class="btn btn-default next" v-show="innerOverflow" @click="next"><i class="next iconfont icon-next"></i></button>
                    <button class="btn btn-default return" v-if="returnUrl" @click="doReturn"><i class="iconfont icon-return"></i>返回</button>
                </div>
                <div class="tab-content" v-if="type !== 'inline'">
                    <component is="bv-tabs-pane" v-for="(el, index) in tabs" :key="el.id" v-bind="{id: el.id, 'data-active': index === innerCurrentIndex, timestamp: el.timestamp, target: el.target, prop: el.prop}" v-show="index === innerCurrentIndex"></component>
                </div>
                <div class="tab-content" v-if="type === 'inline'">
                    <slot name="tabContent"></slot>
                </div>
            </div>
            */
        })
    });
});