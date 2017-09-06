define([
    'vue',
    'jquery',
    'util',
    'Const'
], function (vue, $, util, Const) {
    vue.component('bv-modal', {
        props: {
            size: '',
            title: ''
        },
        data: function () {
            return {
                innerSize: this.size
            }
        },
        created: function () {
            if (this.innerSize) {
                this.innerSize = 'modal-' + this.innerSize;
            }
        },
        /****** 模板定义 ******/
        template: util.heredoc(function() {
            /*!
            <div class="modal-dialog bv-modal" :class="innerSize">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">
                            &times;
                        </button>
                        <h4 class="modal-title" v-text="title"></h4>
                    </div>
                    <form class="form-horizontal">
                        <div class="modal-body">
                            <slot name="body"></slot>
                        </div>
                        <div class="modal-footer">
                            <slot name="footer"></slot>
                        </div>
                    </form>
                </div>
            </div>
             */
        })
    });
});