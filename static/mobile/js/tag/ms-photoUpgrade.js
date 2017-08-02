define(['avalon', 'jquery', 'util'], function(avalon, $, util){
	avalon.component('ms-PhotoUpgrade', {
		template: util.heredoc(function() {
			/*
            <div class="camera cameraFace" ms-attr="{id:@containerId}">
                <div class="camera-img cameraFace-img">
                    <img alt="" />
                </div>
                <div class="camera-file cameraFace-file">
                </div>
                <div class="path"><input type="hidden" id="path" ms-duplex="@path" ms-rules='{required:true}' data-required-message="请使用扫描功能上传信息"></div>
                <p>{{@file_title}}</p>
            </div>
			*/
		}),
		defaults: {
			url: '',
            containerId: '',
			defaultImage: 'images/camera.png',
			// imgPath: '',
			file_id: '',
			file_title: '',
			path: '',
			onFileUploaded: undefined,
			localShowImage: function(file_id) {
				util.loading();
				var vm = this;
				setTimeout(function() {
					if (util.showLocalImage(file_id) && util.checkFileSize(vm.containerId)) {
	                	if (vm.url) {
							util.upload({
								url: vm.url,
								data: new FormData($('#' + vm.containerId).closest('form')[0]),
								success: function(obj) {
									vm.onFileUploaded.call(null, obj);
								},
								error: function() {
									// util.clearLocalImage(file_id);
									vm.fileDomInit();
								},
								complete: function() {
									util.loading('close');
								}
							});
						} else {
	                        util.loading('close');
	                    }
					} else {
						util.loading('close');
					}
				}, 500);
			},
			onInit: function(e) {
			},
			onReady: function(e) {
				var vm = this;
				vm.fileDomInit(true);
				$(this.$element).on('change', 'input[type=file]', function() {
					vm.localShowImage(vm.file_id);
				});
				vm.$watch('defaultImage', function() {
					$('.camera-img img', this.$element).attr('src', this.defaultImage);
					vm.path = this.defaultImage;
				});
			},
			fileDomInit: function(init) {
				$('.camera-img img', this.$element).attr('src', this.defaultImage);
				// this.imgPath = this.defaultImage;
				$('.camera-file', this.$element).empty();
				$('.camera-file', this.$element).append('<input type="file" id="' + this.file_id + '" name="' + this.file_id + '" capture="camera" accept="image/*" />');
				if (!init) {
					this.path = '';
				}
			}
		}
	});
});
