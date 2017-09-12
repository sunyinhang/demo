require(['jquery', 'util', 'Const', 'bvForm', 'async!amap', 'async!bmap'], function($, util, Const) {
    var vm = util.bind({
        container: 'checkLocation',
        data: {
            tags: {
                formKey1: 'checkLocationForm1',
                formKey2: 'checkLocationForm2'
            },
            formConfig1: {
                layout: 'inline',
                title: '高德',
                columns: [
                    {
                        head: '经度',
                        name: 'longitude'
                    },
                    {
                        head: '维度',
                        name: 'latitude'
                    },
                    {
                        head: '行政区',
                        name: 'areaCode'
                    },
                    {
                        head: '地址',
                        name: 'address'
                    }
                ]
            },
            formConfig2: {
                layout: 'inline',
                title: '百度',
                columns: [
                    {
                        head: '经度',
                        name: 'longitude'
                    },
                    {
                        head: '维度',
                        name: 'latitude'
                    },
                    {
                        head: '行政区',
                        name: 'areaCode'
                    },
                    {
                        head: '地址',
                        name: 'address'
                    }
                ]
            }
        },
        mounted: function () {
            var vm = this;
            app.getCurrentPosition('a', function (result) {
                util.refresh({
                    vm: util.vm(vm, vm.tags.formKey1),
                    entity: {
                        areaCode: result.areaCode,
                        longitude: result.longitude,
                        latitude: result.latitude,
                        address: result.address
                    }
                });
            });
            app.getCurrentPosition('b', function (result) {
                util.refresh({
                    vm: util.vm(vm, vm.tags.formKey2),
                    entity: {
                        areaCode: result.areaCode,
                        longitude: result.longitude,
                        latitude: result.latitude,
                        address: result.address
                    }
                });
            });
        }
    });
});