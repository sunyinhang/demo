require(['vue', 'jquery', 'util'], function(vue, $, util) {
    vue.component('tab-applyQuota', {
        props: {
            active: {
                default: 'realName'
            }
        },
        data: function () {
            return {
                innerTabs: [
                    {
                        text: '实名',
                        clazz: 'realName'
                    },
                    {
                        text: '个人资料',
                        clazz: 'personal'
                    },
                    {
                        text: '人脸识别',
                        clazz: 'face'
                    },
                    {
                        text: '密码设置',
                        clazz: 'password'
                    }
                ]
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="tab-applyQuota">
                <ul>
                    <li v-for="el in innerTabs" :class="[{'active': active === el.clazz}, el.clazz]" v-text="el.text"></li>
                </ul>
            </div>
            */
        })
    });
});